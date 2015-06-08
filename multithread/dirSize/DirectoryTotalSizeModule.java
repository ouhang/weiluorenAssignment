package dirSize;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import currentTime.CurrentTime;
import currentTime.FakeTime;
import currentTime.SystemCurrentTime;

import java.util.concurrent.Callable;
/**
 * Module Used in Guice for DirectoryTotalSizeCalculator class.
 * 
 * @author weiluo
 * 
 */
public class DirectoryTotalSizeModule extends AbstractModule {
  final private Integer numThreads; 
  final private boolean fakeTimeOption;
  final private long fakeTimeStep;

  /**
   * 
   * @param numThreads
   *          number of threads we want to use in DirectoryTotalSizeCalculator.
   * @param fakeTimeOption
   *          "true" if we want to use fake time in the test, else "false".
   * @param fakeTimeStep
   *          it only matters if "fakeTimeOption" is "true". Used to construct a
   *          fake time object.
   */
  public DirectoryTotalSizeModule(int numThreads, boolean fakeTimeOption, long fakeTimeStep) {
    if (numThreads <= 0) {
      throw new IllegalArgumentException("The number of threads allowed to use should be positive");
    }
    this.numThreads = numThreads;
    this.fakeTimeOption = fakeTimeOption;
    this.fakeTimeStep = fakeTimeStep;
  }

  public DirectoryTotalSizeModule(int numThreads) {
    this(numThreads, false, 0);
  }

  public DirectoryTotalSizeModule() {
    this(1);
  }

  @Override
  protected void configure() {
    bind(Integer.class)
    .annotatedWith(DirectoryTotalSizeCalculator.NumThreads.class)
    .toInstance(numThreads);
    if (fakeTimeOption) {
      bind(CurrentTime.class).toInstance(new FakeTime(fakeTimeStep));
    } else {
      bind(CurrentTime.class).to(SystemCurrentTime.class);
    }
    install(new FactoryModuleBuilder()
    .implement(new TypeLiteral<Callable<Integer>>() {
    }, DirectoryTotalSizeCalculator.SizeCalculator.class)
    .build(SizeCalculatorFactory.class));
  }
}
