package primeNumberCalculation;

import java.util.concurrent.Callable;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

class PrimeNumberCalculationHelperModule extends AbstractModule {
  protected final int upperBound;
  protected final int numThreads;
  protected final int minLengthForEachWorker;

  public PrimeNumberCalculationHelperModule(int upperBound, int numThreads, int minLengthForEachWorker) {
    this.upperBound = upperBound;
    this.numThreads = numThreads;
    this.minLengthForEachWorker = minLengthForEachWorker;
  }  
  @Override
  protected void configure() { 
    install(new FactoryModuleBuilder().implement(new TypeLiteral<Callable<Integer>>() {
    }, PrimeNumberCalculator.WorkerThread.class).build(PrimeNumberWorkerFactory.class));
    bind(Integer.class).annotatedWith(Names.named("upperBound"))
        .toInstance(upperBound); 
    bind(Integer.class).annotatedWith(Names.named("numThreads"))
        .toInstance(new Integer(numThreads));
    bind(Integer.class).annotatedWith(Names.named("minLengthForEachWorker"))
        .toInstance(new Integer(minLengthForEachWorker));
  }
}