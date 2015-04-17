package currentTime;

/**
 * Interface used to test elapsed time in the programs.
 * 
 * @author weiluo
 * ########################Delete extra useless lines or spaces like this line, here and anywhere else
 */
public interface CurrentTime {
  /**
   * In real implementation, it should return the "real" current time, while in
   * the test, it might return some "fake" time.
   * ########################You don't need to say a lot about it's implements like test / real stuff
   * @return current time in milli-second
   */
  public long NowMillis();
}
