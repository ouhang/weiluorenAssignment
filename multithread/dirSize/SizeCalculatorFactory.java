package dirSize;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
/**
 * Interface used in Assist Inject for the factory providing 
 * "DirectoryTotalSizeCalculator.SizeCalculator"
 * @author weiluo
 *
 */
public interface SizeCalculatorFactory {
  public DirectoryTotalSizeCalculator.SizeCalculator create(
      @Assisted("index") Integer index, ######### have a better name other than index
      @Assisted LinkedBlockingQueue<String> subDirectoryName,
      @Assisted AtomicLong totalSize,
      @Assisted AtomicInteger fileInQueue,
      @Assisted("numThreads") Integer numThreads
      );
}
