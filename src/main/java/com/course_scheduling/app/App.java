
package com.course_scheduling.app;

import com.course_scheduling.ga.Driver;

/**
 * Main class, called in pom.xml
 *
 */
public class App {

    public static void main(String[] args) {
        // run ga algorithm
        Driver driver = new Driver();
        driver.runAlgorithm();
    }

}
