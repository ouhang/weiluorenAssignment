package dirSize;

import com.google.inject.Guice;
import com.google.inject.Injector;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

@RunWith(value = Parameterized.class)
public class DirectoryTotalSizeCalculatorTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  private final int numThreads;
  private final int fakeTimeStep;

  /**
   * Parameters are used to construct DirectoryTotalSizeCalculator and FakeTime objects.
   */
  public DirectoryTotalSizeCalculatorTest(int numThreads, int fakeTimeStep) {
    this.numThreads = numThreads;
    this.fakeTimeStep = fakeTimeStep;
  }

  /**
   * Provide parameters for the tests.
   * 
   */
  @Parameters(name = "{index}: numThreads:{0}, fakeTimeStep:{1}")
  public static Iterable<Object[]> data1() {
    return Arrays.asList(new Object[][] { 
        { 1, 5 },
        { 5, 10 }, 
        { 10, 1 },
        { 20, 1 }, 
        { 5, 200 },
        { 10, 200 } 
        });
  }

  /**
   * Prepare a temporary directory for the test.
   * 
   * @return the total size of such directory
   * @throws IOException
   */
  private Long prepareTempFolder() throws IOException { ####### why I didn't see the case that there are more than 2 levels of directories ?
    File createdFile = folder.newFile("createdFile");
    long numChars = 1000;
    addOneFile(createdFile, 1000);
    String subFolderName = "subFolder";
    File createdFolder = folder.newFolder(subFolderName);
    int numFiles = 10;
    for (int i = 0; i < numFiles; ++i) {
      String pathName = createdFolder.getCanonicalPath() + "/" + Integer.toString(i);
      addOneFile(pathName, numChars);
    }
    return numChars + numChars * numFiles;
  }

  /**
   * Helper function to construct a new file with given size.
   * 
   * @param file
   *          the File object which we will fill with "numChars" of characters
   * @param numChars
   *          the size of file
   * @throws IOException
   */
  private void addOneFile(File file, long numChars) throws IOException {
    String stringInFile = "";
    for (int i = 0; i < numChars; ++i) {
      stringInFile += "a";
    }
    PrintWriter out = new PrintWriter(file);
    out.print(stringInFile);
    out.close();
  }
########## consider to combine the two addOneFile helper functions
  /**
   * Helper function to construct a new file with given size.
   * 
   * @param file
   *          the canonical path of the file which we will fill with "numChars"
   *          of characters
   * @param numChars
   *          the size of file
   * @throws IOException
   */
  private void addOneFile(String pathName, long numChars) throws IOException {
    File file = new File(pathName);
    addOneFile(file, numChars);
  }

  /**
   * Test "computeTotalSize" and "getTotalSize"
   * 
   * @throws IOException
   * @throws InterruptedException 
   */
  @Test
  public void testGetTotalSize() throws IOException, InterruptedException {
    Injector injector = Guice.createInjector(new DirectoryTotalSizeModule(numThreads));
    File folderLocationFile = folder.getRoot();
    Long totalSize = prepareTempFolder();
    DirectoryTotalSizeCalculator theCalculator = injector
        .getInstance(DirectoryTotalSizeCalculator.class);
    assertEquals(totalSize,
        new Long(theCalculator.computeTotalSize(folderLocationFile.getCanonicalPath())));
    assertEquals(totalSize.longValue(), theCalculator.getTotalSize());
  }

  /**
   * Test "getElapsedTime"
   * 
   * @throws IOException
   * @throws InterruptedException 
   */
  @Test
  public void testElapsedTime() throws IOException, InterruptedException {
    ####### try to use Guiceberry for guice injection for the junit test
    
    Injector injector = Guice.createInjector(new DirectoryTotalSizeModule(numThreads, true,
        fakeTimeStep));
####### change above to
    Injector injector = Guice.createInjector(new DirectoryTotalSizeModule(
        numThreads,
        true,
        fakeTimeStep));

#######
        
    File folderLocationFile = folder.getRoot();
    prepareTempFolder();
    DirectoryTotalSizeCalculator theCalculator = injector
        .getInstance(DirectoryTotalSizeCalculator.class);
    theCalculator.computeTotalSize(folderLocationFile.getCanonicalPath());
    assertEquals(fakeTimeStep, theCalculator.getElapsedTime());
  }

}
