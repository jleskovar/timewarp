package co.paralleluniverse.vtime;

import co.paralleluniverse.test.TimeTester;
import com.ea.agentloader.AgentLoader;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by james on 2/04/16.
 */
public class JavaAgentTest {

    @BeforeClass
    public static void setUp() throws Exception {
        AgentLoader.loadAgentClass(JavaAgent.class.getName(), null);
    }

    @After
    public void tearDown() throws Exception {
        VirtualClock.setGlobal(SystemClock.instance());
    }

    @Test
    public void manualClockShouldWork() throws Exception {
        ManualClock clock = new ManualClock(0);
        VirtualClock.setGlobal(clock);
        TimeTester timeTester = new TimeTester();
        assertThat(timeTester.getCurrentTimeMillis(), is(0L));

        clock.advance(1, TimeUnit.SECONDS);
        assertThat(timeTester.getCurrentTimeMillis(), is(1000L));
    }
}
