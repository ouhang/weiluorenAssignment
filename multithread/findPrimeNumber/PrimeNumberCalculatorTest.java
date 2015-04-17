package findPrimeNumber;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized;

import com.google.inject.Guice;
import com.google.inject.Injector;

@RunWith(value = Parameterized.class)
public class PrimeNumberCalculatorTest {
  final int numThreads;
  final int minLengthForEachWorker;
  private final int fakeTimeStep;

  private List<Integer> correctPrimeLessThan500 = new ArrayList<Integer>(Arrays.asList(2, 3, 5, 7,
      11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103,
      107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197,
      199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277, 281, 283, 293, 307,
      311, 313, 317, 331, 337, 347, 349, 353, 359, 367, 373, 379, 383, 389, 397, 401, 409, 419,
      421, 431, 433, 439, 443, 449, 457, 461, 463, 467, 479, 487, 491, 499));
                                                    // Correct set of prime numbers less than 500.
  
  /**
   * Parameters are used to construct PrimeNumberCalculator and FakeTime
   * objects.
   * 
   */
  public PrimeNumberCalculatorTest(int numThreads, int minLengthForEachWorker, int fakeTimeStep) {
    this.numThreads = numThreads;
    this.minLengthForEachWorker = minLengthForEachWorker;
    this.fakeTimeStep = fakeTimeStep;
  }

  /**
   * Test the Prime Number Calculation.
   */
  @Test
  public void testPrimeNumberLessThan500() {
    Injector injector = Guice.createInjector(new PrimeNumberCalculationModule(500, numThreads,
        minLengthForEachWorker));
    PrimeNumberCalculator theCalculator = injector.getInstance(PrimeNumberCalculator.class);
    List<Integer> result = theCalculator.run();
    assertEquals(correctPrimeLessThan500, result);
  }

  /**
   * Test "getElapsedTime"
   */
  @Test
  public void testElapsedTime() throws IOException {
    Injector injector = Guice.createInjector(new PrimeNumberCalculationModule(500, numThreads,
        minLengthForEachWorker, true, fakeTimeStep));
    PrimeNumberCalculator theCalculator = injector.getInstance(PrimeNumberCalculator.class);
    List<Integer> result = theCalculator.run();
    assertEquals(correctPrimeLessThan500, result);
    assertEquals(fakeTimeStep, theCalculator.getElapsedTime());

  }

  /**
   * Provide parameters for the tests.
   * 
   */
  @Parameters(name = "{index}: numThreads:{0}, minLengthForEachWorker:{1}")
  public static Iterable<Object[]> data1() {
    return Arrays.asList(new Object[][] {
        { 1, 1, 4 },
        { 5, 1, 5 },
        { 10, 1, 10 },
        { 20, 1, 1 },
        { 5, 200, 25 },
        { 10, 200, 1 } 
        });
  }

}
