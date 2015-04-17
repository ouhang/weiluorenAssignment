package findPrimeNumber; ######### the package name is not good, should use noun instead of verb or sentence

import java.util.*; ######## avoid to use any * unless there are more than 5 or 6 imports from this package
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

########## or import java... should below import com.google....... all imports should follow the order of alphabets

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import currentTime.CurrentTime;

public class PrimeNumberCalculator {
  private final int numThreads;
  private ExecutorService executorService;

  private final List<Boolean> primeNumberFlags; // Flags indicating whether a
                                                // number is prime or not. #####instead of separate the comments in different lines, I would rather making them a new line.
  private final int upperBound;
  private long startTime;
  private long elapsedTime;
  private final int minLengthForEachWorker;
  private final PrimeNumberWorkerFactory factory;
  private final CurrentTime currentTime;

  /**
   * 
   * @param upperBound
   *          the upperBound (excluded) of the numbers to be considered
   * @param numThreads
   *          the number of threads used in the thread pool.
   * @param minLengthForEachWorker
   *          the smallest size of the list of numbers a worker thread would
   *          handle.
   * @param factory
   *          factory object to provide WorkerThread objects
   * @param currentTime
   *          CurrentTime object used to get current time.
   */
  @Inject
  public PrimeNumberCalculator(@Named("upperBound") Integer upperBound, 
  ########## in general, instead of using named, I would prefer annotation while in your case, named is good
  ################## why ? Because annotation can be reused convenient instead of typing the string inside named everytime
  ################## which makes errors easier to detect (String is easily to false type).
      @Named("numThreads") Integer numThreads,
      @Named("minLengthForEachWorker") Integer minLengthForEachWorker,
      PrimeNumberWorkerFactory factory, CurrentTime currentTime) {
    this.numThreads = numThreads;
    this.executorService = Executors.newCachedThreadPool();
    this.primeNumberFlags = new ArrayList<Boolean>(Arrays.asList(new Boolean[upperBound]));
    this.upperBound = upperBound;
    this.minLengthForEachWorker = minLengthForEachWorker;
    this.factory = factory;
    this.currentTime = currentTime;
  }

  public long getElapsedTime() {
    return elapsedTime;
  }

  private void refreshCalculator() {
    executorService = Executors.newCachedThreadPool();
    Collections.fill(primeNumberFlags, Boolean.FALSE); ######## just use false, boxing process from boolean to Boolean is fine...
  }

  /**
   * Each time we find a prime number, we mark all its multiples to be non-prime
   * using multiple threads, with each thread handling a trunk of numbers in the
   * interval from the square of the current prime number to the upper bound of
   * numbers we need to consider. Then we go to the next un-marked number which
   * will be our next prime number.
   * 
   * @return All the prime numbers in [0, upperBound).
   */
  public ArrayList<Integer> run() {
    startTime = currentTime.NowMillis();
    refreshCalculator();

    ArrayList<Integer> result = new ArrayList<Integer>(); ######actually you can write List<Integer> result = new ArrayList<>();
    if (upperBound < 2) ############ prefer to have {}
      return result;

    if (upperBound == 2) {
      result.add(new Integer(2)); ######## that's ok to have boxing process here
      return result;
    }

    int currentNum = 2;
    while (currentNum * currentNum < upperBound) {
      result.add(currentNum);
      primeNumberFlags.set(currentNum, true);
      int currentNumSquared = currentNum * currentNum;
      int range = (upperBound - currentNumSquared) / numThreads;
      int numTasks = numThreads;
      if (range < minLengthForEachWorker) {
        numTasks = (upperBound - currentNumSquared) / minLengthForEachWorker
            + (((upperBound - currentNumSquared) % minLengthForEachWorker == 0) ? 0 : 1);
        range = minLengthForEachWorker;
      } // We might decrease the number of worker threads
        // if there are not many numbers to check.

      Set<Callable<Integer>> callables = new HashSet<Callable<Integer>>();
      for (int i = 0; i < numTasks; ++i) {
        if (i < numTasks - 1) {
          ####### stack parameters of this method for better readability
          ###########like:
          callables.add(
              factory.create(currentNumSquared + i * range, 
              currentNumSquared + (i + 1) * range, 
              currentNum, 
              i, 
              primeNumberFlags));
        #######################################     Same below  
          callables.add(factory.create(currentNumSquared + i * range, currentNumSquared + (i + 1) 
              * range, currentNum, i, primeNumberFlags));
        } else {
          callables.add(factory.create(currentNumSquared + i * range, upperBound, currentNum, i,
              primeNumberFlags));
        }
      }
      try {
        executorService.invokeAll(callables);
      } catch (InterruptedException e) {
        e.printStackTrace(); ####### I would rather throw the exception...print wouldn't work in our system.
      }
      currentNum = updateCurrentNum(currentNum);
    }

    for (int i = currentNum; i < upperBound; ++i) {
      if (!primeNumberFlags.get(i)) {
        result.add(i);
      }
    }

    executorService.shutdown();
    elapsedTime = currentTime.NowMillis() - startTime;
    return result;
  }

  /**
   * Find the next prime number.
   * 
   * @param oldNum
   *          the current prime number.
   * @return: the next prime number
   */
  private int updateCurrentNum(int oldNum) {
    for (int i = oldNum + 1; i < upperBound; ++i) {
      if (!primeNumberFlags.get(i)) {
        return i;
      }
    }
    return upperBound;
  }

  public static class WorkerThread implements Callable<Integer> {
    private final int workerLowerBound; // included
    private final int workerUpperBound; // excluded
    private final int target;
    private final int index;
    private final List<Boolean> primeNumberFlags;

    /**
     * 
     * @param workerLowerBound
     *          the lower bound (included) of the list of numbers handled by
     *          this thread.
     * @param workerUpperBound
     *          the upper bound (excluded) of the list of numbers handled by
     *          this thread.
     * @param target
     *          the thread mark the multiples of this number to be true in
     *          "primeNumberFlags" indicating they are not prime number.
     * @param index
     *          the index of this worker thread (not used in the current
     *          implementation, but is useful for debugging).
     * @param primeNumberFlags
     *          Flags indicating whether a number is prime or not.
     */
    @Inject ########### stack these parameters
    public WorkerThread(@Assisted("workerLowerBound") Integer workerLowerBound,
        @Assisted("workerUpperBound") Integer workerUpperBound, @Assisted("target") Integer target,
        @Assisted("index") Integer index, @Assisted List<Boolean> primeNumberFlags) {
      this.workerLowerBound = workerLowerBound;
      this.workerUpperBound = workerUpperBound;
      this.target = target;
      this.index = index;
      this.primeNumberFlags = primeNumberFlags;
    }

    /**
     * The thread mark the multiples of the target number between the lower and
     * upper bounds, which are assigned through the constructor, to be true in
     * "primeNumberFlags" indicating they are not prime number.
     */
    @Override
    public Integer call() throws Exception {
      if (target > workerUpperBound) {
        // Probably never get here if we schedule the threads wisely.
        return -1;
      }

      int lowerMultiple = workerLowerBound / target + ((workerLowerBound % target == 0) ? 0 : 1);
      int upperMultiple = workerUpperBound / target + ((workerUpperBound % target == 0) ? 0 : 1);

      for (int multiple = lowerMultiple; multiple < upperMultiple; ++multiple) {
        primeNumberFlags.set(target * multiple, true);
      }
      return 0;
    }
  }

}
