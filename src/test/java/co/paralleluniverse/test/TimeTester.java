package co.paralleluniverse.test;

import java.time.LocalDateTime;

/**
 * Created by james on 2/04/16.
 */
public class TimeTester {

    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) throws InterruptedException {
        while (true) {
            System.err.println("Current time is " + LocalDateTime.now());
            Thread.sleep(1000);
        }
    }

}
