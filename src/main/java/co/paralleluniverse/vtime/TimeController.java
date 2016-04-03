
package co.paralleluniverse.vtime;

import javax.management.*;
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
public class TimeController extends StandardMBean implements TimeControllerMBean {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd kkmmss");
    private static final Clock SYSTEM_CLOCK = SystemClock.instance();
    private ManualClock manualClock;

    public TimeController(long epoch) throws NotCompliantMBeanException {
        super(TimeControllerMBean.class);
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
    public void leaveManualMode() {
        // TODO: need to advance time far enough so that currently blocked scheduled waiters run, prior to resuming
        jumpToTime(getCurrentlySetMilliseconds());
    }

    @Override
    public void nextMinute() {
        plusMinute(1);
    }

    @Override
    public void nextHour() {
        plusHour(1);
    }

    @Override
    public void nextDay() {
        plusDay(1);
    }

    @Override
    public void plusMinute(int minutes) {
        if (isInManualMode())
            manualClock.advance(minutes, TimeUnit.MINUTES);
        else
            jumpToTime(getCurrentlySetMilliseconds() + (minutes * 60 * 1000));
    }

    @Override
    public void plusHour(int hours) {
        if (isInManualMode())
            manualClock.advance(hours, TimeUnit.HOURS);
        else
            jumpToTime(getCurrentlySetMilliseconds() + (hours * 60 * 60 * 1000));
    }

    @Override
    public void plusDay(int days) {
        if (isInManualMode())
            manualClock.advance(days, TimeUnit.DAYS);
        else
            jumpToTime(getCurrentlySetMilliseconds() + (days * 24 * 60 * 60 * 1000));
    }

    @Override
    public void scaleTimeUntil(double scaleFactor, String destinationTime) {
        try {
            scaleTimeUntil(scaleFactor, DATE_FORMAT.parse(destinationTime).getTime());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void scaleTime(double scaleFactor) {
        setGlobal(new ScaledClock(new FixedEpochClock(getCurrentlySetMilliseconds()), scaleFactor));
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
    public String getCurrentTime() {
        return getCurrentDateTimeString(getCurrentlySetMilliseconds());
    }

    @Override
    public String getRealTime() {
        return getCurrentDateTimeString(getCurrentRealSystemTime());
    }

    private String getCurrentDateTimeString(long epochMilli) {
        ZonedDateTime instant = ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault());
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

    private long getCurrentRealSystemTime() {
        return SYSTEM_CLOCK.currentTimeMillis();
    }

    private boolean isInManualMode() {
        return VirtualClock.get() == manualClock;
    }

    @Override
    protected String getDescription(MBeanInfo info) {
        return "Manipulate / skew / stretch the perception of current time on this JVM.";
    }

    @Override
    protected String getDescription(MBeanAttributeInfo info) {
        switch (info.getName()) {
            case "CurrentVirtualClock":
                return "The Clock instance actively running on this JVM";
            case "CurrentTime":
                return "The current time as perceived by this JVM";
            case "RealTime":
                return "The current 'real' time, as perceived by the system";
        }
        return super.getDescription(info);
    }

    @Override
    protected String getParameterName(MBeanOperationInfo op, MBeanParameterInfo param, int sequence) {
        switch (op.getName()) {
            case "jumpToTime":
                switch (param.getType()) {
                    case "long":
                        return "absoluteTimeInMs";
                    case "java.lang.String":
                        return "destinationTime";
                }
            case "plusMinute":
                return "minutes";
            case "plusHour":
                return "hours";
            case "plusDay":
                return "days";
            case "scaleTimeUntil":
                switch (sequence) {
                    case 0:
                        return "scaleFactor";
                    case 1:
                        return "destinationTime";
                }
            case "scaleTime":
                return "scaleFactor";
        }

        return super.getParameterName(op, param, sequence);
    }

    @Override
    protected String getDescription(MBeanOperationInfo op, MBeanParameterInfo param, int sequence) {
        switch (op.getName()) {
            case "jumpToTime":
                switch (param.getType()) {
                    case "long":
                        return "Time to jump to, in milliseconds since 1970-01-01";
                    case "java.lang.String":
                        return "Time to jump to, in \"YYYYMMDD HHMMSS\" format. Hours in [0,23]";
                }
            case "plusMinute":
                return "Number of minutes to advance";
            case "plusHour":
                return "Number of hours to advance";
            case "plusDay":
                return "Number of days to advance";
            case "scaleTimeUntil":
                switch (sequence) {
                    case 0:
                        return "Scale factor to advance time by. Must be a positive number. Can be less than one.";
                    case 1:
                        return "Destination time - when to time should resume";
                }
            case "scaleTime":
                return "Scale factor to advance time by. Must be a positive number. Can be less than one.";
        }

        return super.getDescription(op, param, sequence);
    }

    @Override
    protected String getDescription(MBeanOperationInfo op) {
        switch (op.getName()) {
            case "jumpToTime":
                switch (op.getSignature()[0].getType()) {
                    case "long":
                        return "Set JVM time directly to passed in time, in milliseconds since 1970-01-01";
                    case "java.lang.String":
                        return "Set JVM time directly to passed in time, in 'YYYYMMDD HHMMSS' format";
                }
            case "enterManualMode":
                return "Activate manual mode. Time will freeze to the current perceived time.";
            case "leaveManualMode":
                return "Deactivate manual mode. Time will resume from where it was left off.";
            case "nextMinute":
                return "Advance one minute";
            case "nextHour":
                return "Advance one hour";
            case "nextDay":
                return "Advance one day";
            case "plusMinute":
                return "Advance N minutes";
            case "plusHour":
                return "Advance N hours";
            case "plusDay":
                return "Advance N days";
            case "scaleTimeUntil":
                return "Scale time by scaleFactor until destination time reached";
            case "scaleTime":
                return "Scale time by scaleFactor";
        }

        return super.getDescription(op);

    }
}
