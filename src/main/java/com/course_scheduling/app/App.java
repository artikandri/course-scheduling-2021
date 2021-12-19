package com.course_scheduling.app;

import com.course_scheduling.ga.Driver;

/**
 * Main class Called in pom.xml
 *
 */
public class App {

    public static void main(String[] args) {
        Driver driver = new Driver();
        driver.runAlgorithm();
    }

}
