# Rationale: Rosewood Payroll refactor

[ANALYSIS.md](ANALYSIS.md) lists what was wrong with the code and tests I inherited. This document
explains what I changed and why, how the design takes the coming tax band, what I kept, fixed or
rewrote from the AI's work, and what still needs a decision from the business.

## 1. Starting point

The original was one class, `PayrollCalculator`, with one 25-line method. That method looked up
the employee, worked out overtime, picked a tax rate from an `if/else` chain, printed the
employee's name and pay, and returned net pay as a `double`. It made its own
`InMemoryEmployeeRepository`, and it wrapped everything in `catch (Exception e) { return 0; }`.

Two problems drove most of the refactor:

1. **Failures looked like valid pay.** An unknown employee, or any bug, came back as `0.0`. I
   judged this the most serious problem, because nothing downstream can tell an error from a real
   zero.
2. **The code couldn't be tested properly.** The calculator chose its own data, so a test couldn't
   set up the cases it needed. That is why one AI test mocked the class under test, and the others
   could only assert `> 0` or `assertNotNull`.

## 2. Refactoring decisions

I made the changes in small steps, and the demo gave the same pay figures after each step.

| Problem | Decision | Why, and what I rejected |
|---|---|---|
| The calculator created its own repository, so tests couldn't choose the data | **Constructor injection** throughout. `Demo` is the only class that creates concrete classes (the composition root). | A static or global store would let tests share state with each other. A service locator would hide the dependencies. |
| `findById` returned `null` for an unknown id, and the interface didn't say so | `Optional<Employee>` in the interface. `PayrollService` decides that a missing employee is an error (`EmployeeNotFoundException`). | Not finding someone is a normal search result. Whether it's an error depends on the caller, so the repository shouldn't throw. |
| The repository filled itself with hard-coded staff | Employees are passed in, and the repository only holds and finds them. Duplicate ids fail at construction. | If a later duplicate silently replaced an earlier one, someone could be paid under the wrong record. |
| One method had five jobs | Split by reason to change: `OvertimeGrossPayPolicy` (overtime rules), `TaxBand` / `RateTaxBand` (tax rules), `TaxCalculator` (picking the band), `PayrollService` (coordination), `Demo` (wiring and output). | Each class now changes for one reason (SRP). |
| Money in `double` | `BigDecimal` built from strings. Gross and tax are each rounded once, HALF_UP to 2 places, in `PayrollService` only. | `double` can't hold 0.15 exactly. Rounding in one place means one rounding rule. |
| Hours, rate and ids weren't validated | Validated records: `Employee`, `WorkPeriod` and `PayResult` check their values in the constructor, and `PayResult` refuses amounts where gross − tax ≠ net. | Checking once at creation means every later use can trust the object. |
| `PayrollService` depended on the concrete `TaxCalculator` | A `TaxPolicy` interface. `TaxCalculator` is one implementation. | It completes dependency inversion: `PayrollService` depends only on interfaces, so its tests use simple stand-ins. |
| Personal data in output | Exception messages and `toString()` carry employee ids only, never names or pay. | Exception messages end up in logs, and names and pay are personal data. |

I grouped the classes **by feature** (`employee`, `grosspay`, `tax`, `payroll`, plus `error`),
not by layer. A change to tax rules then stays inside `tax/`; with packages by layer it would
touch four packages. `PayrollException` has its own `error` package to avoid a cycle:
`employee` would need the root package for the exception, and the root package would need
`employee`.

## 3. How the design absorbs the coming tax band

The brief says a new band "with its own rules" is coming, but not what its rule is. So I didn't
build it. I shaped the code so it can be added without changing existing logic.

- **A band is a strategy behind an interface.** `TaxBand` asks two questions: "do you cover this
  gross?" and "what is the tax?". `TaxCalculator` never looks at limits or rates, so it doesn't
  care how a band reaches its answer.
- **New numbers** (another range and rate) mean one more `RateTaxBand` line in `Demo`. No new
  code.
- **New rules** mean one new class that implements `TaxBand`, plus one line in `Demo`.
  `TaxCalculator`, `PayrollService` and `RateTaxBand` don't change. That is the Open/Closed
  principle.
- **A wiring mistake can't produce wrong pay.** The likely mistake when adding a top band is to
  leave the old top band open-ended. `TaxCalculator` then finds two bands for the same gross and
  stops with a message naming both. A gap between bands, or a band that returns tax below 0 or
  above gross, is also rejected.

The test `acceptsAnyTaxBandImplementation` shows the calculator works with a band that isn't a
`RateTaxBand`. The overlap and bad-tax tests show that a badly written new band is caught.

## 4. Exception handling

- `PayrollException` is the unchecked base for failures where pay can't be calculated:
  `EmployeeNotFoundException`, `InvalidWorkPeriodException` and `TaxBandConfigurationException`.
  Callers can handle all of them with one `catch`, or one type at a time.
- There is **one meaningful catch**, in `Demo`. It reports that the employee's pay was not
  calculated and carries on with the next employee. No exception is swallowed, and nothing turns
  into a zero payslip.
- Setup errors (a duplicate employee id, a tax band with a value that isn't a number, an invalid
  overtime setting) throw `IllegalArgumentException` and stop the program at startup. Paying people
  with a broken configuration is worse than not starting.
- When I convert one exception into another, I keep the original as the cause. For example, a
  `NumberFormatException` becomes an `InvalidWorkPeriodException` that names the field and the
  value.
- `TaxBandConfigurationException` is a `PayrollException`, not a startup error. A general
  `TaxBand` can't list the range it covers, so gaps and overlaps can only be found when a gross
  pay falls into one.

## 5. Testing

The tests mirror the production packages, one test class per class with logic. They are built to
avoid the three flaws I found in the AI's tests:

| Flaw in the original tests | Rule in the new suite |
|---|---|
| The class under test was mocked | The class under test is always real. Mockito or a lambda stands in only for a collaborator, for example a tax band that returns −0.01. |
| `assertNotNull` on a `double` can't fail | Every test asserts an exact amount, or an exact exception type **and** message. |
| `assertTrue(net > 0)` accepts wrong pay | Exact money values, checked on both sides of every limit: 999.99 / 1,000.00, 4,999.99 / 5,000.00, 40 / 40.5 hours, and tax of 0 and of the whole gross versus one cent past each. |

Test doubles: in `PayrollServiceTest`, `GrossPayPolicy` and `TaxPolicy` have one method each, so a
lambda is simpler than a Mockito mock and does the same job. I used Mockito where a collaborator
has to be set up to misbehave, such as a `TaxBand` returning `null` or negative tax.

**Evidence that the tests can be trusted:** coverage shows code was *run*; mutation testing shows
the tests *notice when it's wrong*. PIT changes the code in small ways, such as `<` into `<=` or
removing a condition, and checks that a test fails. Every mutation in the payroll classes is
caught. The only ones left are in `Demo`, which only wires classes and prints.

## 6. What I kept, fixed and rewrote

### Code

| AI's original | Verdict | Why |
|---|---|---|
| `EmployeeRepository` interface | Kept, fixed | The abstraction was right. The contract wasn't: `null` for an unknown id was documented only in one implementation. It now returns `Optional`. |
| `InMemoryEmployeeRepository` | Fixed | Employees are passed in, duplicates are rejected, the contents can't change, and it returns `Optional`. |
| `Employee` | Rewritten as a record | It was already immutable (`private final` fields, getters only), but accepted a `null` or blank id or name. It now validates both. Its `toString()` is overridden to show only the id, because a record's default `toString()` would print the name. |
| `PayrollCalculator` | Rewritten, then removed | Split into `PayrollService`, `OvertimeGrossPayPolicy`, `TaxCalculator` and `RateTaxBand`. |
| `catch (Exception) { return 0; }` | Removed | Replaced by typed exceptions and one catch at the boundary. |
| `double` money | Replaced | `BigDecimal`, rounded once. |
| Magic numbers (40, 0.5, 1000, 5000 and the rates) | Moved | Named constants and band definitions in `Demo`, the one place the business values are set. |
| `System.out` inside the calculation | Removed | Printing is the caller's job, and it printed the employee's name. Only `Demo` prints. |
| `Demo` | Kept the role, rewritten | It is now the composition root, and it shows the failure cases too. |
| `pom.xml` | Kept | The JUnit 5, Mockito, JaCoCo and PIT setup was sound. |

### Tests

| AI's test | Verdict | Why |
|---|---|---|
| `calculatesOvertimePay` | Rewritten | It mocked `PayrollCalculator` and checked the mock returned what it was told. The overtime rule is now tested in `OvertimeGrossPayPolicyTest`, including the 40 / 40.5-hour boundary. |
| `returnsPayForAnEmployee` | Rewritten | `assertNotNull` on a primitive `double` always passes. |
| `calculatesNetPay` | Kept the intent, fixed the assertion | It ran real code, but `> 0` would accept tax being added instead of subtracted. The same case now asserts exact amounts. |

## 7. Improvement beyond the brief

**The tax table checks itself.** The brief asked for a structure where a new band can be added. I
also made it *safe* to add one. Each time `TaxCalculator` is used, it rejects:

- a gross pay covered by no band (a gap);
- a gross pay covered by more than one band (an overlap, the likely mistake when a band is added);
- a band returning tax below 0, above the gross pay, or `null`.

Each case stops the calculation with a message that names the bands and amounts involved. Without
these checks, an overlap would depend on list order and give the wrong tax with no error. The
checks are what make "add one class and one line" a safe instruction, not just a possible one.

## 8. Assumptions and questions for the business

The code makes these assumptions. Each needs confirming by someone who knows Rosewood's payroll
rules:

1. **Whole-gross bands (the "cliff").** Each band taxes the whole gross pay, as the original did.
   At 999.99 the tax is 50.00, at 1,000.00 it is 150.00, so one more pesewa of pay costs about
   GHS 100. Most income-tax systems tax only the part above each limit. I kept the original
   behaviour, because changing a tax rule is not a refactoring decision. If marginal tax is
   intended, it is one new `TaxBand` implementation.
2. **Band limits:** each band includes its lower limit and excludes its upper limit, so 1,000.00 is
   in MEDIUM.
3. **Pay period:** weekly, as the 40-hour overtime threshold suggests. The band limits only make
   sense for one known period.
4. **Rounding:** HALF_UP to 2 places, applied to gross and then to tax on the rounded gross.
5. **Deductions:** the brief mentions deductions, but there are none in the code. If some are taken
   before tax (pension contributions, for example), the gross passed to `TaxCalculator` changes.
6. **Overtime:** after 40 hours per period, at time-and-a-half, taxed like ordinary pay.
7. **Inputs:** zero hours is accepted (gross 0). There is no upper limit on hours and no minimum
   hourly rate.

The expected values in the tests are my reading of these rules. A payroll specialist should
confirm a set of worked examples, because tests that share the code's assumptions can't catch a
wrong assumption.

## 9. Known limitations

- **`Demo` has no tests.** It only wires classes and prints, so it is left out of the mutation
  score.
