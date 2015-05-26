package dirSize;

import java.util.concurrent.Callable;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

import currentTime.CurrentTime;
import currentTime.FakeTime;
import currentTime.SystemCurrentTime;

/**
 * Module Used in Guice for DirectoryTotalSizeCalculator class.
 * 
 * @author weiluo
 * 
 */
public class DirectoryTotalSizeModule extends AbstractModule {
  final Integer numThreads; #################### they should be private ?
  final boolean fakeTimeOption;
  final long fakeTimeStep;

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
    if (numThreads <= 0) ########### use {}
      throw new IllegalArgumentException("The number of threads allowed to use should be positive");
    this.numThreads = numThreads;
    this.fakeTimeOption = fakeTimeOption;
    this.fakeTimeStep = fakeTimeStep;
  }

  public DirectoryTotalSizeModule(int numThreads) {
    this(numThreads, false, 0);
######## delete this empty line
  }

  public DirectoryTotalSizeModule() {
    this(1);
  }

  @Override
  protected void configure() {
######## delete this empty line
########## instead of using Names.named, I would prefer non-string annotation
    bind(Integer.class).annotatedWith(Names.named("numThreads")).toInstance(numThreads);
    if (fakeTimeOption) {
      bind(CurrentTime.class).toInstance(new FakeTime(fakeTimeStep));
    } else {
      bind(CurrentTime.class).to(SystemCurrentTime.class);
    }
    install(new FactoryModuleBuilder().implement(new TypeLiteral<Callable<Integer>>() {
    }, DirectoryTotalSizeCalculator.SizeCalculator.class).build(SizeCalculatorFactory.class));
####### change above to:
### install(new FactoryModuleBuilder()
        .implement(new TypeLiteral<Callable<Integer>>() {}, DirectoryTotalSizeCalculator.SizeCalculator.class)
        .build(SizeCalculatorFactory.class));
  }
}
