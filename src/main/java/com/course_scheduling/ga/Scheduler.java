package com.course_scheduling.ga;

import com.course_scheduling.assets.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    public static final int POPULATION_SIZE = 9;
    public static final double MUTATION_RATE = 0.1;
    public static final double CROSSOVER_RATE = 0.9;
    public static final int TOURNAMENT_SELECTION_SIZE = 3;
    public static final int NUMB_OF_ELITE_SCHEDULES = 1;
    private int scheduleNumb = 0;
    private int classNumb = 1;
    private Data data;

    private Timer watch = Timer.start();
    private FileManager fileManager = new FileManager();
    private DateParser dateParser = new DateParser();

    public void runAlgorithm() {
        Scheduler scheduler = new Scheduler();
        scheduler.data = new Data();
        // set your experiment parameters here
        // 1 = small, 2 = medium, 3 = large
        scheduler.data.setExperimentParameters(true, 1);

        scheduler.printAvailableData();
        scheduler.generateSchedules();
    }

    private void generateSchedules() {
        Scheduler scheduler = new Scheduler();
        scheduler.data = new Data();
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(scheduler.data);
        Population population = new Population(Scheduler.POPULATION_SIZE, scheduler.data).sortByFitness();

        int generationNumber = 0;
        long passedTimeInMs = watch.time();
        long passedTimeInMinutes = watch.time(TimeUnit.MINUTES);
        boolean isFitnessReached = population.getSchedules().get(0).getFitness() == 1.0;
        boolean isPenaltyReached = population.getSchedules().get(0).getPenalty() <= 60;
        boolean isTimerReached = passedTimeInMinutes == 15;
        String generationInfo = "";

        population.getSchedules().forEach(schedule -> System.out.println(schedule));
        scheduler.printScheduleAsTable(population.getSchedules().get(0), generationNumber);
        scheduler.classNumb = 1;

        while (!isFitnessReached && !isPenaltyReached && !isTimerReached) {
            generationNumber += 1;
            population = geneticAlgorithm.evolve(population).sortByFitness();
            scheduler.scheduleNumb = 0;

            scheduler.printScheduleAsTable(population.getSchedules().get(0), generationNumber);
            generationInfo = scheduler.printGenerationInfo(generationNumber, population);

            isFitnessReached = population.getSchedules().get(0).getFitness() == 1.0;
            isPenaltyReached = population.getSchedules().get(0).getPenalty() <= 60;
            isTimerReached = passedTimeInMinutes == 15;
            passedTimeInMs = watch.time();
            passedTimeInMinutes = watch.time(TimeUnit.MINUTES);

            System.out.print("Time (seconds): " + passedTimeInMs / 1000);
            System.out.println("        |    Time (minutes): " + passedTimeInMinutes);

            scheduler.classNumb = 1;

            if (isTimerReached) {
                System.out.println("Process stopped because it has taken more than 15 minutes.");
            }
            if (isPenaltyReached) {
                System.out.println("Process stopped because minimum number of penalty has been reached");
            }
            if (isFitnessReached) {
                System.out.println("Process stopped because the target fitness value has been reached");
            }
        }

        printWriter.println(generationInfo);
        printWriter.print("Time (seconds): " + passedTimeInMs / 1000);
        printWriter.println("        |    Time (minutes): " + passedTimeInMinutes);
        printWriter.println(population.getSchedules().get(population.getSchedules().size() - 1));

        String schedules = stringWriter.toString();
        System.out.println(schedules);

        String scheduleFileName = "Schedules-GA-" + dateParser.getTodayDate("dd-MM-yyyy hh.mm.ss") + ".txt";
        fileManager.createTextFile(schedules, scheduleFileName, "results/ga/");
    }

    private String printGenerationInfo(int generationNumber, Population population) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        printWriter.println("Generation # " + generationNumber);
        printWriter.print("Schedule # " + generationNumber);
        printWriter.println("       |         Fitness # "
                + population.getSchedules().get(0).getFitness()
                + "       |         Penalty # "
                + population.getSchedules().get(0).getPenalty()
                + "  |                  Conflicts # "
                + population.getSchedules().get(0).getNumbOfConflicts() + " ");
        printWriter.print("-----------------------------------------------------------------------------------");
        printWriter.println("-------------------------------------------------------------------------------------");

        String info = stringWriter.toString();
        System.out.println(info);

        return info;
    }

    private String printScheduleAsTable(Schedule schedule, int generation) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        ArrayList<Class> classes = schedule.getClasses();
        classes.sort(Comparator.comparing(Class::getCourseId).thenComparing(Class::getTimeslotId));

        printWriter.println("\n                       ");
        printWriter.print("------------------------------------------------------------------------------------");
        printWriter.println("------------------------------------------------------------------------------------");
        printWriter.println("Class # | Course (number) | Room  |   Instructor (Id)   |  Meeting Time (Id) | Group (Id)");
        printWriter.print("------------------------------------------------------------------------------------");
        printWriter.println("------------------------------------------------------------------------------------");

        classes.forEach(x -> {
            int coursesIndex = data.getCourses().indexOf(x.getCourse());
            int roomsIndex = data.getRooms().indexOf(x.getRoom());
            int instructorsIndex = data.getInstructors().indexOf(x.getInstructor());
            int meetingTimeIndex = data.getTimeslots().indexOf(x.getTimeslot());
            int groupIndex = data.getCourses().get(coursesIndex).getGroupId();

            printWriter.println("                       ");
            printWriter.print(String.format("  %1$02d  ", classNumb) + "  | ");
            printWriter.print(String.format("%1$21s", data.getCourses().get(coursesIndex).getName()
                    + " (" + data.getCourses().get(coursesIndex).getId() + ")"
                    + " - " + data.getCourses().get(coursesIndex).getWeeklyHours() + " hours             | "));
            printWriter.print(String.format("%1$10s", data.getRooms().get(roomsIndex).getName() + "     | "));
            printWriter.print(String.format("%1$15s", data.getInstructors().get(instructorsIndex).getName()
                    + " (" + data.getInstructors().get(instructorsIndex).getId() + ")") + "  | ");
            printWriter.print(data.getTimeslots().get(meetingTimeIndex).getTime()
                    + " (" + data.getTimeslots().get(meetingTimeIndex).getId() + ")  | ");
            printWriter.print(data.getGroups().get(groupIndex).getName()
                    + " (" + data.getGroups().get(groupIndex).getId() + ")");

            classNumb++;
        });

        if (schedule.getFitness() == 1) {
            printWriter.println("> Solution found in " + (generation + 1) + " generations");
        }

        printWriter.print("\n-----------------------------------------------------------------------------------");
        printWriter.println("-------------------------------------------------------------------------------------");

        String scheduleTables = stringWriter.toString();
        System.out.println(scheduleTables);

        return scheduleTables;
    }

    private void printAvailableData() {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        printWriter.println("\nAvailable Courses ==>");
        data.getCourses().forEach(x -> printWriter.println("course #: " + x.getId() + ", name: " + x.getName()
                + ", instructors: " + data.findInstructorById(x.getInstructorId()).getName()));
        printWriter.println("\nAvailable Rooms ==>");
        data.getRooms().forEach(x -> printWriter.println("room #: " + x.getName()));
        printWriter.println("\nAvailable Instructors ==>");
        data.getInstructors().forEach(x -> printWriter.println("id: " + x.getId() + ", name: " + x.getName()));
        printWriter.println("\nAvailable Meeting Times ==>");
        data.getTimeslots().forEach(x -> printWriter.println("id: " + x.getId() + ", Meeting Time: " + x.getTime()));
        printWriter.println("------------------------------------------------------------------------------------");
        printWriter.println("-------------------------------------------------------------------------------------");

        String availableData = stringWriter.toString();
        System.out.println(availableData);
    }
}
