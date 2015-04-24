package primeNumberCalculation;

import java.util.concurrent.Callable;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

import currentTime.CurrentTime;
import currentTime.FakeTime;

public class PrimeNumberCalculationTestModule extends PrimeNumberCalculationHelperModule {
  private final long fakeTimeStep;

  public PrimeNumberCalculationTestModule(int upperBound, int numThreads, int minLengthForEachWorker, int fakeTimeStep) {
    super(upperBound, numThreads, minLengthForEachWorker);
    this.fakeTimeStep = fakeTimeStep; 
  }  
  @Override
  protected void configure() { 
    super.configure();
    bind(CurrentTime.class).toInstance(new FakeTime(fakeTimeStep)); ##### I wouldn't prefer toInstance, I would rather to have a provider
    ######## because it introduces a global singleton which is not necessary and may instroduce un-expected behavior.
  }
}
