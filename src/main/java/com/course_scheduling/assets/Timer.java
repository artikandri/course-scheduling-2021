package com.course_scheduling.assets;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author @artikandri, Teddy Ferdinan
 */
public class Timer {

    long starts;

    public static Timer start() {
        return new Timer();
    }

    public Timer() {
        reset();
    }

    public Timer reset() {
        starts = System.currentTimeMillis();
        return this;
    }

    public long time() {
        long ends = System.currentTimeMillis();
        return ends - starts;
    }

    public long time(TimeUnit unit) {
        return unit.convert(time(), TimeUnit.MILLISECONDS);
    }

    /**
     * Constructor. Let the app sleep for specified delay (in ms)
     *
     * @param delayInMilliseconds (in milliseconds)
     */
    public void sleep(int delayInMilliseconds) {
        sleep(0, Timer.class.getName());
    }

    /**
     * Let the app sleep for specified delay (in ms)
     *
     * @param delayInMilliseconds (in milliseconds)
     * @param className class name
     */
    public void sleep(int delayInMilliseconds, String className) {
        try {
            TimeUnit.SECONDS.sleep(delayInMilliseconds);
        } catch (InterruptedException ex) {
            Logger.getLogger(className).log(Level.SEVERE, null, ex);
        }
    }

}
