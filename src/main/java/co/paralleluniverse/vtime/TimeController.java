package co.paralleluniverse.vtime;

/**
 * Created by james on 18/07/15.
 */
public class TimeController implements TimeControllerMBean {

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
    public void scaleTimeUntil(double scaleFactor, long destinationTimeInMs) {
        VirtualClock.setGlobal(new ScaledClock(new FixedEpochClock(VirtualClock.get().currentTimeMillis()), scaleFactor));
        while (VirtualClock.get().currentTimeMillis() < destinationTimeInMs) {
            try {
                SystemClock.instance().Thread_sleep(500);
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
