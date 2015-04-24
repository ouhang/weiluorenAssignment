package currentTime;

/**
 * Fake time used in test. Every time the method "NowMillis" is called, it will
 * return the value equal the one it returned last time it's called plus "fakeTimeStep".
 * 
 * @author weiluo
 * 
 */
public class FakeTime implements CurrentTime {
  private long fakeCurrentTime = 0L;
  private long fakeTimeStep;

  /**
   * @param fakeTimeStep
   *          : The fake elapsed time between two "NowMillis" calls.
   */
  public FakeTime(Long fakeTimeStep) {
    this.fakeTimeStep = fakeTimeStep;
  }

  /**
   * The fake current time would be updated by being added with fake time step,
   * and it will be returned after the update.
   * @return fake current time
   */
  public long NowMillis() {
    long result = fakeCurrentTime;
    fakeCurrentTime += fakeTimeStep;
    return result;
  }
}
