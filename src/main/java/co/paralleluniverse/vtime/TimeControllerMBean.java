package co.paralleluniverse.vtime;

/**
 * Created by james on 18/07/15.
 */
public interface TimeControllerMBean {

    String getCurrentVirtualClock();

    String getCurrentTime();

    void scaleTime(double scaleFactor);

    void scaleTimeUntil(double scaleFactor, long destinationTimeInMs);

    void jumpToTime(long absoluteTimeInMs);
}
