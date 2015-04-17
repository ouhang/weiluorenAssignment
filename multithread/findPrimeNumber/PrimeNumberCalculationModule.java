package findPrimeNumber;

import java.util.concurrent.Callable;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

import currentTime.CurrentTime;
import currentTime.FakeTime;
import currentTime.SystemCurrentTime;

/**
 * Module Used in Guice for PrimeNumberCalculator class.
 * 
 * @author weiluo
 * 
 */
public class PrimeNumberCalculationModule extends AbstractModule {
  private final int upperBound;
  private final int numThreads;
  private final int minLengthForEachWorker;
  private final boolean fakeTimeOption;
  private final int fakeTimeStep;

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
  public PrimeNumberCalculationModule(int upperBound, int numThreads, int minLengthForEachWorker,
      boolean fakeTimeOption, int fakeTimeStep) {
    this.upperBound = upperBound;
    this.numThreads = numThreads;
    this.minLengthForEachWorker = minLengthForEachWorker;
    this.fakeTimeOption = fakeTimeOption;
    this.fakeTimeStep = fakeTimeStep;
  }

  public PrimeNumberCalculationModule(int upperBound, int numThreads, int minLengthForEachWorker) {
    this(upperBound, numThreads, minLengthForEachWorker, false, 0);
  }

  public PrimeNumberCalculationModule(int upperBound, int numThreads) {
    this(upperBound, numThreads, 1);
  }

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().implement(new TypeLiteral<Callable<Integer>>() {
    }, PrimeNumberCalculator.WorkerThread.class).build(PrimeNumberWorkerFactory.class));
    bind(Integer.class).annotatedWith(Names.named("upperBound"))
        .toInstance(new Integer(upperBound));
    bind(Integer.class).annotatedWith(Names.named("numThreads"))
        .toInstance(new Integer(numThreads));
    bind(Integer.class).annotatedWith(Names.named("minLengthForEachWorker")).toInstance(
        new Integer(minLengthForEachWorker));
    if (fakeTimeOption) {
      bind(CurrentTime.class).toInstance(new FakeTime(fakeTimeStep));
    } else {
      bind(CurrentTime.class).to(SystemCurrentTime.class);
    }

  }
}
