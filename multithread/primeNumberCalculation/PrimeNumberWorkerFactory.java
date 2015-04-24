package primeNumberCalculation;

import com.google.inject.assistedinject.Assisted;

import java.util.List;

/**
 * Interface used in Assist Inject for the factory providing 
 * "PrimeNumberCalculator.WorkerThread"
 * @author weiluo
 *
 */
 

public interface PrimeNumberWorkerFactory {
  public PrimeNumberCalculator.WorkerThread create(
      @Assisted("workerLowerBound") Integer workerLowerBound,
      @Assisted("workerUpperBound") Integer workerUpperBound,
      @Assisted("target") Integer target,
      @Assisted("index") Integer index,
      @Assisted List<Boolean>  primeNumberFlags
      );
}
