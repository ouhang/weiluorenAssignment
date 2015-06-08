package gameOfLife;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.BindingAnnotation;

import mpi.MPI;
import mpi.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;

public class GameOfLife {
  public final static int numberOfRows = 16;
  public final static int numberOfCols = 16;
  public final int global_grid[] = 
      { 0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,
        1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 };
  
  
  private static final Map<String, Coordinate> directions;
  static {
    Map<String, Coordinate> aMap = new HashMap<>();
    aMap.put("up", new Coordinate(-1, 0));
    aMap.put("down", new Coordinate(1, 0));
    aMap.put("left", new Coordinate(0, -1));
    aMap.put("right", new Coordinate(0, 1));
    aMap.put("upperLeft", new Coordinate(-1, -1));
    aMap.put("upperRight", new Coordinate(-1, 1));
    aMap.put("lowerLeft", new Coordinate(1, -1));
    aMap.put("lowerRight", new Coordinate(1, 1));
    directions = Collections.unmodifiableMap(aMap);
  }
  private final int neighborInfoGrid[] = new int[numberOfRows * numberOfCols];
  private final int[] terminationMsg = { 0 };
  private final int rank;
  private final int size;
  private final int rowStepsForEachWorker;
  private final int numberOfSteps;
  /**
   * The program will run the Game of Life for 60 steps, 
   * with number of processes given in the command line and handled by MPI.
   * @param args
   */
  public static void main(String[] args) {
    MPI.Init(args);
    int rank = MPI.COMM_WORLD.Rank();
    int size = MPI.COMM_WORLD.Size();
    int numberOfSteps = 60;
    Injector injector = Guice.createInjector(new GameOfLifeModule(rank, size, numberOfSteps));
    if (rank == 0) {
      GameOfLife master = injector.getInstance(GameOfLife.class);
      master.run();
    } else {
      GameOfLife slave = injector.getInstance(GameOfLife.class);
      slave.run();
    }
    MPI.Finalize();
  }
  
  @BindingAnnotation @Target({ FIELD, PARAMETER, METHOD }) @Retention(RUNTIME)
  public @interface Rank {}
  @BindingAnnotation @Target({ FIELD, PARAMETER, METHOD }) @Retention(RUNTIME)
  public @interface Size {}
  @BindingAnnotation @Target({ FIELD, PARAMETER, METHOD }) @Retention(RUNTIME)
  public @interface NumberOfSteps {}

  
  /**
   * The GameOfLife instance with rank 0 is responsible to update the grid.
   * Other instances are only responsible to calculate the new state for the grid,
   * then send the new state to the instance with rank 0. 
   * @param rank: The rank assigned by MPI.
   * @param size: The number of processes used in the MPI program.
   * @param numberOfSteps: The number of steps we will run for the Game of Life.
   */
  @Inject
  public GameOfLife(@Rank Integer rank, @Size Integer size, @NumberOfSteps Integer numberOfSteps) {
    this.rank = rank;
    this.size = size;
    this.numberOfSteps = numberOfSteps;
    rowStepsForEachWorker = numberOfRows / size;
  }
  /**
   * Treat our grid as a 16 by 16 matrix, this method 
   * returns the element at coordinate (rowIndex, colIndex).
   * @param rowIndex: the row index of the element.
   * @param colIndex: the column index of the element.
   * @return: the value of the element at such coordinate.
   */
  private int getGridElement(int rowIndex, int colIndex) { 
    return global_grid[rowIndex * numberOfCols + colIndex];
  }
  
  /**
   * Treat our grid as a 16 by 16 matrix, this method
   * returns the element at coordinate "coord".
   * @param coord: The coordinate of the element.
   * @return: the value of the element at such coordinate.
   */
  private int getGridElement(Coordinate coord) { 
    return getGridElement(coord.rowIndex, coord.colIndex);
  }
  /**
   * A helper "pair" class just for the coordinates of
   * the elements in the grid treated as a matrix. 
   */
  private static class Coordinate {
    int rowIndex;
    int colIndex;

    public Coordinate(int rowIndex, int colIndex) {
      this.rowIndex = rowIndex;
      this.colIndex = colIndex;
    }
    public Coordinate add( Coordinate other) {
      return new Coordinate(rowIndex + other.rowIndex, colIndex + other.colIndex);
    }
  }
  /**
   * Print out the current state of the Game of Life.
   */
  private void printGrid() {
    for (int rowIndex = 0; rowIndex < numberOfRows; ++rowIndex) {
      for (int colIndex = 0; colIndex < numberOfCols; ++colIndex) {
        System.out.print(" " + getGridElement(rowIndex, colIndex));
      }
      System.out.println("");
    }
  }
  /**
   * The GameOfLife instance with rank 0 will send out 
   * termination message "terminationMsg" to other instances
   * at the beginning of every step. If the first element of 
   * "terminationMsg" is -1, then other GameOfLife instances will exit.  
   */
  private void sendTerminationMsg() {
    for (int workerIndex = 1; workerIndex < size; ++workerIndex) {
      MPI.COMM_WORLD.Send(terminationMsg, 0, 1, MPI.INT, workerIndex, 96);
    }
  }

  
  private boolean checkPositionValidity(Coordinate coordinate) {
    return coordinate.colIndex >= 0 
        && coordinate.colIndex <= numberOfCols - 1
        && coordinate.rowIndex >= 0
        && coordinate.rowIndex <= numberOfRows - 1;
  }
  
   /**
    * Find all the valid neighbors of the element at (rowIndex, colIndex).
    * @param rowIndex: the row index of the element.
    * @param colIndex: the column index of the element.
    * @return: a non-empty list containing the coordinates of all the neighbors
    * of the element at (rowIndex, colIndex). 
    */
  private List<Coordinate> getAllNeighbors(int rowIndex, int colIndex) { 
    List<Coordinate> result = new ArrayList<Coordinate>();
    Coordinate currentCoordinate = new Coordinate(rowIndex, colIndex);
    for (Coordinate direction: directions.values()) {
      Coordinate neighbor = currentCoordinate.add(direction);
      if (checkPositionValidity(neighbor)) {
        result.add(neighbor);
      }
    } 
    return result;
  }
  /**
   * Return a index summarizing whether the cell at (rowIndex, colIndex)
   * should become alive or dead.
   * @param rowIndex: the row index of the element.
   * @param colIndex: the column index of the element.
   * @return: If the current cell at (rowIndex, colIndex) is alive, then
   * the return value is non-negative, otherwise it's non-positive.
   * The absolute value of the return value is the number of alive neighbors
   * of such cell.
   */
  private int calcNeighbors(int rowIndex, int colIndex) {
    int result = 0;
    int deadOrAliveMuliplier = (getGridElement(rowIndex, colIndex) > 0) ? 1 : -1;
    List<Coordinate> neighbors = getAllNeighbors(rowIndex, colIndex);
    for (Coordinate coord : neighbors) {
      result += getGridElement(coord);
    }
    return deadOrAliveMuliplier * result;
  }
  /**
   * The method running the Game of Life.
   */
  public void run() {
    if (rank == 0) {
      runMaster();
    } else {
      runSlave();
    }
  }

  /**
   * If the GameOfLife instance has rank 0, this method will be called
   * when running the Game of Life. It's responsible to receive the calculation
   * of the new state from all other instances and then update the grid.
   * It will also print out the current state at each step.
   */
  private void runMaster() { 
    for (int tick = 0; tick <= numberOfSteps; ++tick) {
      printGrid();
      if (tick == numberOfSteps) terminationMsg[0] = -1;
      sendTerminationMsg();
      if (tick == numberOfSteps) break;
      if (size > 1) {
        receiveFromAdjacentWorkers();
        sendToAdjacentWorkers();
      }
      updateLocalRows();
      for (int workerIndex = 1; workerIndex < size; ++workerIndex) {
        Status mps = MPI.COMM_WORLD.Recv(global_grid, workerIndex * rowStepsForEachWorker * numberOfCols,
            rowStepsForEachWorker * numberOfCols, MPI.INT, workerIndex, 97);
      }
      System.out.println("done with the tick " + tick);
    }
  }
  /**
   * If the GameOfLife instance has non-zero rank, this method will be called
   * when running the Game of Life. It will send out and receive the state information from and to
   * its adjacent instances, then it will update the grid elements that it's responsible for.
   * At each step of Game of Life, it will send those new grid elements
   * to the GameOfLife instance with rank 0.
   * If the rank is even, it will first receive messages from its neighbors and then send out messages.
   * If the rank is odd, it will first send out messages and then receive messages.
   */
  private void runSlave() { 
    while (true) {
      Status mps = MPI.COMM_WORLD.Recv(terminationMsg, 0, 1, MPI.INT, 0, 96);
      if (terminationMsg[0] < 0){ break; }
      if (rank % 2 == 0) {
        receiveFromAdjacentWorkers();
        sendToAdjacentWorkers();
      } else {
        sendToAdjacentWorkers();
        receiveFromAdjacentWorkers();
      }  
      updateLocalRows();
      MPI.COMM_WORLD.Send(global_grid, 
                          rank * rowStepsForEachWorker * numberOfCols, 
                          rowStepsForEachWorker * numberOfCols, 
                          MPI.INT, 
                          0, 
                          97);
    }
  }
  /**
   * Receive messages from the neighbors.
   */
  private void receiveFromAdjacentWorkers() {
    if (rank > 0) {
      Status mps = MPI.COMM_WORLD.Recv(global_grid, 
                                      (rank - 1) * rowStepsForEachWorker * numberOfCols,
                                      rowStepsForEachWorker * numberOfCols,
                                      MPI.INT,
                                      rank - 1, 
                                      99);
    }
    if (rank < size - 1) {
      Status mps = MPI.COMM_WORLD.Recv(global_grid, 
                                      (rank + 1) * rowStepsForEachWorker * numberOfCols, 
                                      rowStepsForEachWorker * numberOfCols,
                                      MPI.INT, 
                                      rank + 1, 
                                      98);
    }
  }
  /**
   * Send out messages to the neighbors.
   */
  private void sendToAdjacentWorkers() {
    if (rank > 0) {
      MPI.COMM_WORLD.Send(global_grid, rank * rowStepsForEachWorker * numberOfCols,
        rowStepsForEachWorker * numberOfCols, MPI.INT, rank - 1, 98);
    }
    if (rank < size - 1) {
      MPI.COMM_WORLD.Send(global_grid, rank * rowStepsForEachWorker * numberOfCols,
        rowStepsForEachWorker * numberOfCols, MPI.INT, rank + 1, 99);
    }
  }
  /**
   * Update locally the elements in the grid that this process is responsible for.
   */
  private void updateLocalRows() {
    for (int rowIndex = rank * rowStepsForEachWorker; 
        rowIndex < (rank + 1) * rowStepsForEachWorker;
        ++rowIndex) {
      for (int colIndex = 0; colIndex < numberOfCols; ++colIndex) {
        neighborInfoGrid[rowIndex * numberOfCols + colIndex] = calcNeighbors(rowIndex, colIndex);
      }
    }
    for (int rowIndex = rank * rowStepsForEachWorker; 
        rowIndex < (rank + 1) * rowStepsForEachWorker; 
        ++rowIndex) {
      for (int colIndex = 0; colIndex < numberOfCols; ++colIndex) {
        if (neighborInfoGrid[rowIndex * numberOfCols + colIndex] == -3) {
          global_grid[rowIndex * numberOfCols + colIndex] = 1;
        } else if (neighborInfoGrid[rowIndex * numberOfCols + colIndex] > 0
            && (neighborInfoGrid[rowIndex * numberOfCols + colIndex] < 2 
                ||  neighborInfoGrid[rowIndex * numberOfCols + colIndex] > 3)) {
          global_grid[rowIndex * numberOfCols + colIndex] = 0;
        }
      }
    } 
  }
}
