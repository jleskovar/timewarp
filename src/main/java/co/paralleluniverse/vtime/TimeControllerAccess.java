package co.paralleluniverse.vtime;

import javax.management.*;
import java.lang.management.ManagementFactory;

public class TimeControllerAccess {

    static final String OBJECT_NAME = "com.tyro.time:type=TimeController";
    private static TimeControllerMBean timeControllerMBean;

    static void init() throws NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, MalformedObjectNameException {
        MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName(OBJECT_NAME);

        try {
            timeControllerMBean = new TimeController(Long.parseLong(System.getProperty("timewarp.epoch", Long.toString(System.currentTimeMillis()))));
        } catch (NotCompliantMBeanException e) {
            e.printStackTrace();
        }

        platformMBeanServer.registerMBean(
                timeControllerMBean,
                name);
    }

    public static TimeControllerMBean getTimeController() {
        return timeControllerMBean;
    }

}
