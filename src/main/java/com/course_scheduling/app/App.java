package com.course_scheduling.app;

import com.course_scheduling.ga.Scheduler;
import com.course_scheduling.pso.pso;
import com.course_scheduling.pso.pso.Configuration;
import com.course_scheduling.assets.FileManager;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

/**
 * Constructor.
 *
 * Called in pom.xml
 *
 */
public class App {

    private final FileManager fileManager = new FileManager();

    public static void main(String[] args) {

        App app = new App();
//         run GA algorithm
        Scheduler driver = new Scheduler();
        driver.runAlgorithm();

        //run PSO algorithm
//        app.runPsoAlgorithm("default", "limitFV", 100, 30);
    }

    private void runPsoAlgorithm(String datasetSize, String psoMode, int psoLimit, int psoParticles) {
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

        //initialize PSO; automatic stop after 15 minutes
        //"limitFV", 700, 30 => operation mode limit FV, 700 max penalty score, 30 particles
        //"limitIter", 1000, 30 => operation mode limit Iter, 1000 max iteration, 30 particles
        //Configuration x = pso.psoInitialize("limitIter", 1000, 30, possibleRooms, possibleCourses, possibleTimes, possibleTimesDurations, coursesGroups, coursesWeeklyHours, coursesPreferredTimes, courseInstructorPairs);
        //Configuration x = pso.psoInitialize("limitFV", 100, 30, possibleRooms, possibleCourses, possibleTimes, possibleTimesDurations, coursesGroups, coursesWeeklyHours, coursesPreferredTimes, courseInstructorPairs);
        Configuration x = pso.psoInitialize(psoMode, psoLimit, psoParticles, possibleRooms, possibleCourses, possibleTimes, possibleTimesDurations, coursesGroups, coursesWeeklyHours, coursesPreferredTimes, courseInstructorPairs);
        Configuration xNew = pso.psoIterate(x, "");
        logOutput += xNew.show();
        logOutput += pso.psoMapSchedules(xNew);
        //write log output to file
        DateTimeFormatter psoDTF = DateTimeFormatter.ofPattern("_yyyy_MM_dd_HH_mm_ss");
        LocalDateTime psoNow = LocalDateTime.now();
        String psoTimestamp = psoDTF.format(psoNow) + ".txt";
        fileManager.createTextFile(logOutput, "Schedules-PSO" + psoTimestamp, "results/pso/");
    }

}
