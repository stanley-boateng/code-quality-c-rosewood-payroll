
# ANALYSIS: Rosewood Payroll

## Code smells and anti-patterns

1. **Magic numbers.** `40`(standard hours), `0.5`(overtime premium) `1000` / `5000`(band limits) and the different rates are hard-coded inside the method.
2. **Exception swallowing.** `catch(Exception) e {return 0;}` (line 32) catches every failure and turns it into a valid amount. The cause is lost and nothing is logged, hence the caller can't tell an error from a real zero. 
3. **Null is not documented in the interface.** `InMemoryEmployeeRepository.findById` returns `null` for an unknown id, because that is what `Map.get` does. The `EmployeeRepository` interface doesn't say so, either in its return type (plain `Employee`, not `Optional<Employee>`) or in documentation. The only record of it is a comment in one implementation. Another implementation could equally throw, return `null`, or return a placeholder, so callers can't rely on any particular not-found behaviour.
4. **No input validation.** Negative hours, zero rates and a `null` or blank employee id are all accepted.
5. **Long method.** One method does the lookup, gross pay, overtime, tax band, net pay and console output.

## SOLID violations

| Principle | Violation |
|---|---|
| **S** (single responsibility) | `PayrollCalculator` handles employee lookup, gross and overtime rules, tax-band policy and output. Overtime rules and tax rules change for different reasons but live in one method. |
| **O** (open/closed) | A new tax band, a new rate or a different overtime rule all mean editing the `if/else` chain. There is no pluggable `TaxPolicy` or `OvertimePolicy`. |
| **D** (dependency inversion) | `private final EmployeeRepository repository = new InMemoryEmployeeRepository();` (line 6). The class depends on the interface but creates the concrete class itself, so callers and tests can't supply another repository. This is the main cause of the testability problem. |
| **L / I** (Liskov substitution / interface segregation) | No real violation; the interface is small. The weak point is that the `findById` contract (null or exception?) is undefined, so implementations can't be substituted reliably. |

## AI-generated test that do not test anything
All references are to `PayrollCalculatorTest.java`.

### `calculatesOvertimePay` : tests Mockito, not the code

```java
PayrollCalculator calculator = mock(PayrollCalculator.class);
when(calculator.calculateNetPay("e2", 50, 30)).thenReturn(1402.5);
assertEquals(1402.5, calculator.calculateNetPay("e2", 50, 30));
```
It mocks the class being tested, tells it to return 1402.5, then checks if the mock return 1402.5. `PayrollCalculator` never runs, hence overtime lines are never covered.

### `returnsPayForAnEmployee`: an assertion that cannot fail

```java
double net = calculator.calculateNetPay("e1", 35, 25);
assertNotNull(net);
```
The assertion always passes whatever the result: 0, a negative number or `NaN`.

### `calculatesNetPay` : runs real code but is weak

```java
assertTrue(net > 0);
```
This runs the real code, but `> 0` accepts result with tax *added* instead of subtracted. It catches only "return 0". The expected value is 760.0 and should be asserted.

## The most serious problem

** The catch-all `catch (Exception) {return 0;}` in `calculateNetPay`. **

A failure that looks like a valid result is the worst outcome:

- An unknown id, bugs etc all come back as `0.0`. The caller can't tell the different issues apart.
- It also hides any future fault in the calculation

** The Dependency inversion violation. This is the reason the tests can't use controlled data, so the test suite can't be fixed properly until this is changed.