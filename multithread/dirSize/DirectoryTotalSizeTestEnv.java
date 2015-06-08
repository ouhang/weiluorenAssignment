package dirSize;

import java.util.concurrent.Callable;

import com.google.guiceberry.GuiceBerryModule;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import currentTime.CurrentTime;
import currentTime.FakeTime;
import currentTime.SystemCurrentTime;

public class DirectoryTotalSizeTestEnv extends AbstractModule {

  private Integer numThreads = 4;
  private Boolean fakeTimeOption = true;
  private Long fakeTimeStep = 4L;
  
  @Override
  protected void configure() {
    install(new GuiceBerryModule());
    bind(Integer.class).annotatedWith(DirectoryTotalSizeCalculator.NumThreads.class).toInstance(numThreads);
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
