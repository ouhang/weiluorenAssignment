package dirSize;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.Parameterized.Parameters;

import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class DirectoryTotalSizeTestByGuiceberry {
  /*
   * Here I don't know how to pass parameter "numThreads" and "fakeTimeStep"
   * to the DirectoryTotalSizeTestEnv instance or GuiceBerryRule instance.
   */
  @Rule public GuiceBerryRule guiceBerry = 
      new GuiceBerryRule(DirectoryTotalSizeTestEnv.class);
  @Rule public TemporaryFolder folder = new TemporaryFolder();
  @Inject DirectoryTotalSizeCalculator theCalculator;
  private final int numThreads=4;
  private final int fakeTimeStep=4;
  private String subFolderName = "subFolder";

  /**
   * Prepare a temporary directory for the test.
   * 
   * @return the total size of such directory
   * @throws IOException
   */
  private Long prepareTempFolder() throws IOException { 
    File createdFile = folder.newFile("createdFile");
    long numChars = 1000;
    addOneFile(createdFile, 1000);
    File createdFolder = folder.newFolder(subFolderName);
    int numFiles = 10;
    for (int i = 0; i < numFiles; ++i) {
      String pathName = createdFolder.getCanonicalPath() + "/" + Integer.toString(i);
      addOneFile(pathName, numChars);
    }
    return numChars + numChars * numFiles;
  }
  /**
   * Prepare a deep temporary directory for the test.
   * @return the total size of the directory
   * @throws IOException
   */
  private Long perpareTempDeepDirectory() throws IOException {
    int depth = 10;
    long numChars = 1000;
    String currentPath = "";
    for (int i = 0; i < depth; ++i) {
      String subFileName = subFolderName + Integer.toString(i);
      File subFolder;
      if (i == 0) {
        subFolder = folder.newFolder(subFileName);
        currentPath = subFolder.getCanonicalPath();
      } else {
        currentPath += "/" + subFileName;
        subFolder = new File(currentPath);
        subFolder.mkdir();
      }
      String filePathName = subFolder.getCanonicalPath() + "/" + Integer.toString(i);
      addOneFile(filePathName, numChars);
    }
    return depth * numChars;
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
  
  @Test
  public void testGetTotalSizeOfDeepDirectory() throws IOException, InterruptedException {
    Injector injector = Guice.createInjector(new DirectoryTotalSizeModule(numThreads));
    File folderLocationFile = folder.getRoot();
    Long totalSize = perpareTempDeepDirectory();
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
    Injector injector = Guice.createInjector(new DirectoryTotalSizeModule(
        numThreads, 
        true,
        fakeTimeStep));        
    File folderLocationFile = folder.getRoot();
    prepareTempFolder();
    DirectoryTotalSizeCalculator theCalculator = injector
        .getInstance(DirectoryTotalSizeCalculator.class);
    theCalculator.computeTotalSize(folderLocationFile.getCanonicalPath());
    assertEquals(fakeTimeStep, theCalculator.getElapsedTime());
  }
}
