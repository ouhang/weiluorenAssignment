package dirSize;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import currentTime.CurrentTime;

/**
 * This class is used to compute the total size of certain directory with
 * certain number of threads.
 * 
 */
public class DirectoryTotalSizeCalculator {

  private final int numThreads;
  private final AtomicLong totalSize;
  private static final String specialDirectoryName = "///";
  private long startTime;
  private long elapsedTime;
  private final AtomicInteger fileInQueue;
  private ArrayList<Callable<Integer>> workers;
  private final LinkedBlockingQueue<String> subDirectoryName;
  private ExecutorService executorService;
  private final SizeCalculatorFactory factory;
  private final CurrentTime currentTime;



  /**
   * Method that is called only after a calling of "computeTotalSize" to get the
   * elapsed time of such calling.
   * 
   * @return The elapsed time of the last calling of "computeTotalSize" in
   *         millisecond.
   */
  public long getElapsedTime() {
    return elapsedTime;
  }

  /**
   * Method that is called only after a calling of "computeTotalSize" to get the
   * total size of the directory which is considered in such calling.
   * 
   * @return The total size of the directory
   */
  public long getTotalSize() {
    return totalSize.get();
  }

  /**
   * 
   * @param numThreads
   *          the number of threads that will be used in computing the total
   *          size of certain directory other than the main thread.
   * 
   * @param factory
   *          factory used to construct worker threads.
   * @param currentTime
   *          CurrentTime object used to get current time.
   */
  @Inject
  public DirectoryTotalSizeCalculator(@Named("numThreads") Integer numThreads,
      SizeCalculatorFactory factory, CurrentTime currentTime) {
    if (numThreads <= 0)
      throw new IllegalArgumentException("The number of threads allowed to use should be positive");
    this.numThreads = numThreads;
    this.subDirectoryName = new LinkedBlockingQueue<String>();
    this.totalSize = new AtomicLong(0);
    this.fileInQueue = new AtomicInteger(0);
    this.factory = factory;
    this.workers = new ArrayList<Callable<Integer>>();
    this.currentTime = currentTime;
  }

  /**
   * Compute the total size of the directory with name given as the parameter.
   * After calling this method, the user can call "getTotalSize" to get the
   * total size of the given directory and "getElapsedTime" to get the time used
   * for such computation.
   * 
   * @param directoryName
   *          the name of directory whose total size to be computed.
   *          
   * @return The total size of the directory
   */
  public long computeTotalSize(String directoryName) {
    executorService = Executors.newFixedThreadPool(this.numThreads);

    startTime = currentTime.NowMillis();
    try {
      subDirectoryName.put(directoryName);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    fileInQueue.set(1);
    totalSize.set(0);

    startThreads();

    try {
      executorService.invokeAll(workers);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    executorService.shutdown();
    elapsedTime = currentTime.NowMillis() - startTime;
    return totalSize.get();
  }

  /**
   * Starting given number of threads forming a thread pool. The number of
   * threads is given when the DirectoryTotalSize object is constructed.
   */
  private void startThreads() {
    workers = new ArrayList<Callable<Integer>>();

    for (int i = 0; i < numThreads; ++i) {
      workers.add(factory.create(i, subDirectoryName, totalSize, fileInQueue, numThreads));
    }
  }

  /**
   * Inner class for the threads in the thread pool. The thread would look for
   * tasks in the task queue which are basically the name of sub-directories
   * that need to be dealt with. If the task queue is empty, the thread would be
   * blocked. If the task the thread takes is a special directory name, which in
   * this case is "///", the thread would exit and let the main thread know that
   * it has exited.
   * 
   * @author weiluo
   * 
   */

  public static class SizeCalculator implements Callable<Integer> {
    private final int index;
    private final LinkedBlockingQueue<String> subDirectoryName;
    private AtomicLong totalSize;
    private AtomicInteger fileInQueue;
    private final int numThreads;

    @Inject
    public SizeCalculator(@Assisted("index") Integer index,
        @Assisted LinkedBlockingQueue<String> subDirectoryName, @Assisted AtomicLong totalSize,
        @Assisted AtomicInteger fileInQueue, @Assisted("numThreads") Integer numThreads) {
      this.index = index;
      this.subDirectoryName = subDirectoryName;
      this.totalSize = totalSize;
      this.fileInQueue = fileInQueue;
      this.numThreads = numThreads;
    }

    /**
     * The method that the thread would be working on when the thread is
     * started. The thread would look for tasks in the task queue which are
     * basically the name of sub-directories that need to be dealt with. If the
     * task queue is empty, the thread would be blocked. If the task the thread
     * takes is a special directory name, which in this case is "///", the
     * thread would exit and let the main thread know that it has exited.
     */
    @Override
    public Integer call() throws Exception {
      while (true) {
        try {
          String currentDirectoryName = subDirectoryName.take();

          if (isDirectorySpecial(currentDirectoryName)) {
            return 0;
          }
          totalSize.addAndGet(calculateCurrentDirectorySize(currentDirectoryName));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    /**
     * 
     * @param directoryName
     *          the name of directory that this thread would work on. The thread
     *          would scan over the directory, computing the total size of files
     *          in such directory, and putting the names of sub-directory into
     *          the task queue.
     * @return The total size of files in such directory, not including the ones
     *         in the sub-directories of such directory.
     */
    private long calculateCurrentDirectorySize(String directoryName) {
      // System.out.println(getName()+" "+directoryName+" ");
      File directory = new File(directoryName);
      if (!(directory.isDirectory() && directory.canRead()))
        throw new IllegalArgumentException();
      File[] allFiles = directory.listFiles();
      long directorySize = 0;
      for (File file : allFiles) {
        if (file.isFile()) {
          directorySize += file.length();
        } else if (file.isDirectory() && file.canRead()) {
          try {
            fileInQueue.addAndGet(1);
            subDirectoryName.put(file.getCanonicalPath());
          } catch (InterruptedException e) {
            e.printStackTrace();
          } catch (IOException e) {
            e.printStackTrace();
          }

        }
      }
      synchronized (fileInQueue) {
        if (fileInQueue.get() == 1)
          addSpecialDirectoryNames();
        else
          fileInQueue.decrementAndGet();
      }
      return directorySize;
    }

    /**
     * To check whether the parameter matches the special directory name.
     * 
     * @param directoryName
     *          the name of directory to be checked whether special or not
     * @return true if the parameter is indeed the special directory name, and
     *         false otherwise.
     */
    private boolean isDirectorySpecial(String directoryName) {
      return directoryName.equals(specialDirectoryName);
    }

    /**
     * Adding special directory names to the task queue to notify the threads in
     * the thread pool that all the tasks are done, and they can safely exit.
     */
    private void addSpecialDirectoryNames() {
      for (int i = 0; i < numThreads; ++i)
        try {
          subDirectoryName.put(specialDirectoryName);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
    }
  }

}
