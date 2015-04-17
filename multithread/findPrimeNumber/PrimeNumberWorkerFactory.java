package findPrimeNumber;

import java.util.List;

import com.google.inject.assistedinject.Assisted;
/**
 * Interface used in Assist Inject for the factory providing 
 * "PrimeNumberCalculator.WorkerThread"
 * @author weiluo
 *
 */
public interface PrimeNumberWorkerFactory {
  public PrimeNumberCalculator.WorkerThread create(@Assisted("workerLowerBound") Integer workerLowerBound,
      @Assisted("workerUpperBound") Integer workerUpperBound,
      @Assisted("target") Integer target,
      @Assisted("index") Integer index,
      @Assisted List<Boolean>  primeNumberFlags
      );
}