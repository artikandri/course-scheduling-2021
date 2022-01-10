
package com.course_scheduling.app;

import com.course_scheduling.ga.Scheduler;

/**
 * Main class, called in pom.xml
 *
 */
public class App {

    public static void main(String[] args) {
        // run ga algorithm
        Scheduler driver = new Scheduler();
        driver.runAlgorithm();
    }

}
