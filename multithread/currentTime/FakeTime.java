package currentTime;

/**
 * Fake time used in test. Every time the method "NowMillis" is called, it will
 * return the value equal the one it returned last time it's called plus "fakeTimeStep".
 * 
 * @author weiluo
 * 
 */
public class FakeTime implements CurrentTime {
  private long fakeCurrentTime = 0; ##############   0L
  private long fakeTimeStep;

  /**
   * ############### delete useless line here and else where
   * @param fakeTimeStep
   *          : The fake elapsed time between two "NowMillis" calls.
   */
  public FakeTime(long fakeTimeStep) {
    this.fakeTimeStep = fakeTimeStep;
  }

  /**
   * @return fake current time ########### the comments here is not that clear, since you update the fakeCurrentTime
   */
  public long NowMillis() {
    long result = fakeCurrentTime;
    fakeCurrentTime += fakeTimeStep;
    return result;
  }
########### delete this line
}
