package primeNumberCalculation;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

import java.util.concurrent.Callable;

import currentTime.CurrentTime;
import currentTime.FakeTime;
import currentTime.SystemCurrentTime;

/**
 * Module Used in Guice for PrimeNumberCalculator class.
 * 
 * @author weiluo
 * 
 */
public class PrimeNumberCalculationModule extends PrimeNumberCalculationHelperModule {

  /**
   * 
   * @param upperBound
   *          upperBound(excluded) for the prime number finding.
   * @param numThreads
   *          number of threads we want to use in "PrimeNumberCalculator".
   * @param minLengthForEachWorker
   *          the smallest size of the list of numbers a worker thread would
   *          handle.
   * @param fakeTimeOption
   *          "true" if we want to use fake time in the test, else "false".
   * @param fakeTimeStep
   *          it only matters if "fakeTimeOption" is "true". Used to construct a
   *          fake time object.
   */

  public PrimeNumberCalculationModule(
      int upperBound, 
      int numThreads, 
      int minLengthForEachWorker) {
    super(upperBound, numThreads, minLengthForEachWorker);
  }


  public PrimeNumberCalculationModule(int upperBound, int numThreads) {
    this(upperBound, numThreads, 1);
  }

  @Override
  protected void configure() { 
    super.configure();
    bind(CurrentTime.class).to(SystemCurrentTime.class);
  }
}
