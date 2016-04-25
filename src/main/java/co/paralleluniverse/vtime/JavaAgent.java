/*
 * Copyright (c) 2015-2016, Parallel Universe Software Co. All rights reserved.
 * 
 * This program and the accompanying materials are license under the terms of the
 * MIT license.
 */
package co.paralleluniverse.vtime;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import java.lang.instrument.Instrumentation;

public final class JavaAgent {

    private static TimeController timeController;

    public static void premain(String agentArguments, Instrumentation instrumentation) throws Exception {
        performInstrumentation(agentArguments, instrumentation);
    }

    public static void agentmain(String agentArguments, Instrumentation instrumentation) throws Exception {
        performInstrumentation(agentArguments, instrumentation);
    }

    private static void performInstrumentation(String agentArguments, Instrumentation instrumentation) throws
            MalformedObjectNameException,
            InstanceAlreadyExistsException,
            MBeanRegistrationException,
            NotCompliantMBeanException {

        TimeControllerAccess.init();

        instrumentation.addTransformer(new VirtualTimeClassTransformer());

        if (agentArguments != null && !agentArguments.isEmpty()) {
            final double scale = 1.0 / Integer.parseInt(agentArguments);
            System.err.println("SCALING CLOCK by " + scale);
            VirtualClock.setGlobal(new ScaledClock(scale));
        }
    }

    private JavaAgent() {
    }
}
