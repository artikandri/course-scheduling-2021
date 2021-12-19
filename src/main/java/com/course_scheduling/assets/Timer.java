/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.course_scheduling.assets;

import java.util.concurrent.TimeUnit;

// Usage
// TimeWatch watch = TimeWatch.start();
//    // do something
//    long passedTimeInMs = watch.time();
//    long passedTimeInSeconds = watch.time(TimeUnit.SECONDS);
public class Timer {

    long starts;

    public static Timer start() {
        return new Timer();
    }

    private Timer() {
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
}
