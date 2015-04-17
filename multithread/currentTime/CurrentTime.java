package currentTime;

/**
 * Interface used to test elapsed time in the programs.
 * 
 * @author weiluo
 * 
 */
public interface CurrentTime {
  /**
   * In real implementation, it should return the "real" current time, while in
   * the test, it might return some "fake" time.
   * 
   * @return current time in milli-second
   */
  public long NowMillis();
}
