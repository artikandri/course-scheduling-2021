package com.course_scheduling.app;

import com.course_scheduling.ga.Scheduler;

/**
 * Constructor.
 *
 * Called in pom.xml
 *
 */
public class App {

    public static void main(String[] args) {
        // run GA algorithm
        Scheduler driver = new Scheduler();
        driver.runAlgorithm();
    }

}
