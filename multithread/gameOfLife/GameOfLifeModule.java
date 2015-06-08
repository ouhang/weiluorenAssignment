package gameOfLife;

import com.google.inject.AbstractModule;

public class GameOfLifeModule extends AbstractModule {
  final private Integer rank;
  final private Integer size;
  final private Integer numberOfSteps;
  
  public GameOfLifeModule(int rank, int size, int numberOfSteps) {
    this.rank = rank;
    this.size = size;
    this.numberOfSteps = numberOfSteps;
  }
  
  @Override
  protected void configure() {
      bind(Integer.class)
          .annotatedWith(GameOfLife.Rank.class)
          .toInstance(rank);
      bind(Integer.class)
          .annotatedWith(GameOfLife.Size.class)
          .toInstance(size);
      bind(Integer.class)
          .annotatedWith(GameOfLife.NumberOfSteps.class)
          .toInstance(numberOfSteps);
  }
}
