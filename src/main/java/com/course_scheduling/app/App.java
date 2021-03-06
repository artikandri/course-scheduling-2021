package com.course_scheduling.app;

import com.course_scheduling.ga.Scheduler;
import com.course_scheduling.pso.*;
import com.course_scheduling.assets.*;
import com.course_scheduling.pso.pso.Configuration;

/**
 * Constructor. Called in pom.xml as the main class to execute when running the
 * whole project.
 *
 */
public class App {

    private final FileManager fileManager = new FileManager();
    private final Timer timer = new Timer();
    private final DateParser dateParser = new DateParser();

    private void runGaExperiment() {
        System.out.print("-----------------------------------------------------------------------------------");
        System.out.println("-------------------------------------------------------------------------------------");
        System.out.println("Genetic Algorithm");
        System.out.print("-----------------------------------------------------------------------------------");
        System.out.println("-------------------------------------------------------------------------------------");

        Scheduler gaScheduler = new Scheduler();
        int delayInMs = 10;

        // first experiment: comparing FVs after algorithm runs for 10 mins
        // algorithm will automatically stopped after 10 mins
        Scheduler.TARGET_TIMER_MINUTES = 10;

        gaScheduler.runAlgorithm(true, 1);
        timer.sleep(delayInMs);

        gaScheduler.runAlgorithm(true, 2);
        timer.sleep(delayInMs);

        gaScheduler.runAlgorithm(true, 3);
        timer.sleep(delayInMs);

        gaScheduler.runAlgorithm(false, 0);
        timer.sleep(delayInMs);

        // 2nd experiment: comparing times to reach a penalty threshold
        // algorithm will automatically stopped when penalty value has been reached
        // or when the experiment has run past 15 mins
        Scheduler.TARGET_TIMER_MINUTES = 15;

        Scheduler.TARGET_PENALTY = 600;
        gaScheduler.runAlgorithm(true, 1);
        timer.sleep(delayInMs);

        Scheduler.TARGET_PENALTY = 250000;
        gaScheduler.runAlgorithm(true, 2);
        timer.sleep(delayInMs);

        Scheduler.TARGET_PENALTY = 600000;
        gaScheduler.runAlgorithm(true, 3);
        timer.sleep(delayInMs);

        Scheduler.TARGET_PENALTY = 6000000;
        gaScheduler.runAlgorithm(false, 0);
        timer.sleep(delayInMs);

        System.out.println("Schedule has been successfully generated with GA algorithm.");
        System.out.println("Find newly generated schedule file in results/ga/ folder");
    }

    public static void main(String[] args) {
        App app = new App();
        // run GA algorithm
        app.runGaExperiment();

        //run PSO algorithm
        app.runPsoExperiment();
    }

    private void runPsoExperiment() {
        App app = new App();
        int delayInMs = 10;

        System.out.print("-----------------------------------------------------------------------------------");
        System.out.println("-------------------------------------------------------------------------------------");
        System.out.println("Particle Swarm Optimization");
        System.out.print("-----------------------------------------------------------------------------------");
        System.out.println("-------------------------------------------------------------------------------------");

        // first experiment: comparing FVs after algorithm runs for 10 mins
        // algorithm will automatically stopped after 10 mins
        app.runPsoAlgorithm("small", "limitFV", 1, 10, 10);
        timer.sleep(delayInMs);

        app.runPsoAlgorithm("medium", "limitFV", 1, 10, 10);
        timer.sleep(delayInMs);

        app.runPsoAlgorithm("large", "limitFV", 1, 10, 10);
        timer.sleep(delayInMs);

        app.runPsoAlgorithm("default", "limitFV", 1, 10, 10);
        timer.sleep(delayInMs);

        // 2nd experiment: comparing times to reach an FV threshold
        // algorithm will automatically stopped when penalty value has been reached
        // or when the experiment has run past 15 mins
        app.runPsoAlgorithm("small", "limitFV", 600, 10, 15);
        timer.sleep(delayInMs);

        app.runPsoAlgorithm("medium", "limitFV", 250000, 10, 15);
        timer.sleep(delayInMs);

        app.runPsoAlgorithm("large", "limitFV", 600000, 10, 15);
        timer.sleep(delayInMs);

        app.runPsoAlgorithm("default", "limitFV", 6000000, 10, 15);
        timer.sleep(delayInMs);

        System.out.println("Schedule has been successfully generated with PSO algorithm.");
        System.out.println("Find newly generated schedule file in results/pso/ folder");
    }

    private void runPsoAlgorithm(String datasetSize, String psoMode, int psoLimit, int psoParticles, int psoTimeLimit) {
        String courseDataset = "src/main/resources/dataset/processed/courses.csv";
        switch (datasetSize) {
            case "small":
                courseDataset = "src/main/resources/dataset/processed/courses_small.csv";
                break;
            case "medium":
                courseDataset = "src/main/resources/dataset/processed/courses_medium.csv";
                break;
            case "large":
                courseDataset = "src/main/resources/dataset/processed/courses_large.csv";
                break;
            default:
                courseDataset = "src/main/resources/dataset/processed/courses.csv";
        }

        float[] coursesWeeklyHours;
        String[] possibleRooms, possibleCourses, possibleTimes, possibleInstructors, coursesGroups, coursesPreferredTimes, courseInstructorPairs;
        float[] possibleTimesDurations;
        possibleCourses = pso.psoGetPossibleCourses(courseDataset);
        coursesWeeklyHours = pso.psoGetCoursesWeeklyHours(courseDataset);
        courseInstructorPairs = pso.psoGetCourseInstructorPairs(courseDataset);
        coursesGroups = pso.psoGetCoursesGroups(courseDataset);
        possibleRooms = pso.psoGetPossibleRooms("src/main/resources/dataset/processed/rooms.csv");
        possibleTimes = pso.psoGetPossibleTimes("src/main/resources/dataset/processed/timeslots.csv");
        possibleTimesDurations = pso.psoGetPossibleTimesDurations("src/main/resources/dataset/processed/timeslots.csv");
        possibleInstructors = pso.psoGetPossibleInstructors("src/main/resources/dataset/processed/instructors.csv");
        coursesPreferredTimes = pso.psoGetCoursesPreferredTimes("src/main/resources/dataset/processed/instructors.csv");
        String logOutput = "";

        //"limitFV", 700, 30 => operation mode limit FV, 700 max penalty score, 30 particles
        //"limitIter", 1000, 30 => operation mode limit Iter, 1000 max iteration, 30 particles
        Configuration x = pso.psoInitialize(psoMode, psoLimit, psoParticles, possibleRooms, possibleCourses, possibleTimes, possibleTimesDurations, coursesGroups, coursesWeeklyHours, coursesPreferredTimes, courseInstructorPairs);
        Configuration xNew = pso.psoIterate(x, "detailed", psoTimeLimit);
        logOutput += xNew.show();
        logOutput += pso.psoMapSchedules(xNew);

        //write log output to file
        String psoTimestamp = dateParser.getTodayDate("_yyyy_MM_dd_HH_mm_ss") + ".txt";
        fileManager.createTextFile(logOutput, "Schedules-PSO" + psoTimestamp, "results/pso/");
    }

}
