package co.paralleluniverse.vtime;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by james on 18/07/15.
 */
public class TimeController implements TimeControllerMBean {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd kkmmss");

    private final long epoch;

    public TimeController(long epoch) {
        this.epoch = epoch;
        VirtualClock.setGlobalExceptCurrentThread(new FixedEpochClock(epoch));
    }

    @Override
    public void jumpToTime(long absoluteTimeInMs) {
        VirtualClock.setGlobal(new FixedEpochClock(absoluteTimeInMs));
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
    public void scaleTimeUntil(double scaleFactor, String destinationTime) {
        try {
            scaleTimeUntil(scaleFactor, DATE_FORMAT.parse(destinationTime).getTime());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void scaleTimeUntil(double scaleFactor, long destinationTimeInMs) {
        VirtualClock.setGlobal(new ScaledClock(new FixedEpochClock(VirtualClock.get().currentTimeMillis()), scaleFactor));
        while (VirtualClock.get().currentTimeMillis() < destinationTimeInMs) {
            try {
                SystemClock.instance().Thread_sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
        VirtualClock.setGlobal(new ScaledClock(new FixedEpochClock(VirtualClock.get().currentTimeMillis()), 1.0));
    }

    @Override
    public void scaleTime(double scaleFactor) {
        VirtualClock.setGlobal(new ScaledClock(new FixedEpochClock(VirtualClock.get().currentTimeMillis()), scaleFactor));
    }

    @Override
    public String getCurrentTime() {
        return Long.toString(VirtualClock.get().currentTimeMillis());
    }

    @Override
    public String getCurrentVirtualClock() {
        return VirtualClock.get().toString();
    }
}
