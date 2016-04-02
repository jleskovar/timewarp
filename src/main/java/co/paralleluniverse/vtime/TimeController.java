
package co.paralleluniverse.vtime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import static java.time.Instant.ofEpochMilli;
import static java.time.LocalDateTime.from;

/**
 * Created by james on 18/07/15.
 */
public class TimeController implements TimeControllerMBean {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd kkmmss");
    private static final Clock SYSTEM_CLOCK = SystemClock.instance();
    private ManualClock manualClock;

    public TimeController(long epoch) {
        setGlobal(new FixedEpochClock(epoch));
    }

    @Override
    public void jumpToTime(long absoluteTimeInMs) {
        setGlobal(new FixedEpochClock(absoluteTimeInMs));
    }

    @Override
    public void jumpToTime(String destinationTime) {
        try {
            jumpToTime(DATE_FORMAT.parse(destinationTime).getTime());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void enterManualMode() {
        manualClock = new ManualClock(getCurrentlySetMilliseconds());
        setGlobal(manualClock);
    }

    @Override
    public void nextMinute() {
        manualClock.advance(1, TimeUnit.MINUTES);
    }

    @Override
    public void nextHour() {
        manualClock.advance(1, TimeUnit.HOURS);
    }

    @Override
    public void nextDay() {
        manualClock.advance(1, TimeUnit.DAYS);
    }

    @Override
    public void plusMinute(int minutes) {
        manualClock.advance(minutes, TimeUnit.MINUTES);
    }

    @Override
    public void plusHour(int hours) {
        manualClock.advance(hours, TimeUnit.HOURS);
    }

    @Override
    public void plusDay(int days) {
        manualClock.advance(days, TimeUnit.DAYS);
    }

    @Override
    public void scaleTimeUntil(double scaleFactor, String destinationTime) {
        try {
            scaleTimeUntil(scaleFactor, DATE_FORMAT.parse(destinationTime).getTime());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void scaleTimeUntil(double scaleFactor, long destinationTimeInMs) {
        setGlobal(new ScaledClock(new FixedEpochClock(getCurrentlySetMilliseconds()), scaleFactor));
        while (getCurrentlySetMilliseconds() < destinationTimeInMs) {
            try {
                SYSTEM_CLOCK.Thread_sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
        setGlobal(new FixedEpochClock(getCurrentlySetMilliseconds()));
    }

    @Override
    public void scaleTime(double scaleFactor) {
        setGlobal(new ScaledClock(new FixedEpochClock(getCurrentlySetMilliseconds()), scaleFactor));
    }

    @Override
    public String getCurrentTime() {
        ZonedDateTime instant = ofEpochMilli(getCurrentlySetMilliseconds()).atZone(ZoneId.systemDefault());
        LocalDateTime dateTime = from(instant);
        return dateTime.toString();
    }

    @Override
    public String getCurrentVirtualClock() {
        return VirtualClock.get().toString();
    }

    private void setGlobal(Clock newClock) {
        VirtualClock.setGlobal(newClock);
    }

    private long getCurrentlySetMilliseconds() {
        return VirtualClock.get().currentTimeMillis();
    }

}
