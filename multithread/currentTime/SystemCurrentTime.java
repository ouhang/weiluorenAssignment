package currentTime;

/**
 * Return the real time in milli-second in "NowMillis"
 * Should be used outside the tests.
 * 
 * @author weiluo
 *
 */
public class SystemCurrentTime implements CurrentTime {
  public long NowMillis() {
    return System.currentTimeMillis();
  }

}
