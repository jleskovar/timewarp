package co.paralleluniverse.vtime;

/**
 * Created by james on 18/07/15.
 */
public interface TimeControllerMBean {

    String getCurrentVirtualClock();

    String getCurrentTime();

    void scaleTime(double scaleFactor);

    void scaleTimeUntil(double scaleFactor, String destinationTime);

    void jumpToTime(long absoluteTimeInMs);

    void jumpToTime(String destinationTime);

    void enterManualMode();

    void nextMinute();

    void nextHour();

    void nextDay();

    void plusMinute(int minutes);

    void plusHour(int hours);

    void plusDay(int days);
}
