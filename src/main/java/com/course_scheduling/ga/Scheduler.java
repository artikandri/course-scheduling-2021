package com.course_scheduling.ga;

import com.course_scheduling.assets.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    // adjust these values as needed
    public static int POPULATION_SIZE = 10;
    public static double MUTATION_RATE = 0.7;
    public static double CROSSOVER_RATE = 0.3;
    public static int TOURNAMENT_SELECTION_SIZE = 3;
    public static int NUMB_OF_ELITE_SCHEDULES = 1;
    public static int NUMB_OF_SMALL_SIZED_SCHEDULE_COURSES = 30;
    public static int NUMB_OF_LARGE_SIZED_SCHEDULE_CLASSES = 250;

    // adjust these values as needed
    public static double TARGET_FITNESS = 1.0;
    public static int TARGET_PENALTY = 20;
    public static int TARGET_TIMER_MINUTES = 15;
    public static int TARGET_GENERATION = 100000;
    public static boolean IS_SCHEDULE_PRINTED_ON_GENERATION = false;
    public static boolean IS_GEN_INFO_PRINTED_ON_GENERATION = true;

    private int classNumb = 1;
    private Data data;

    private Timer watch = Timer.start();
    private FileManager fileManager = new FileManager();
    private DateParser dateParser = new DateParser();

    public void runAlgorithm(boolean IS_EXPERIMENT_MODE, int EXPERIMENT_TYPE) {
        Data.IS_EXPERIMENT_MODE = IS_EXPERIMENT_MODE;
        Data.EXPERIMENT_TYPE = EXPERIMENT_TYPE;

        Scheduler scheduler = new Scheduler();
        scheduler.data = new Data();
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

        Schedule schedule = population.getSchedules().get(0);
        Penalty penalty = new Penalty(schedule);

        boolean isFitnessReached = penalty.getFitness() == TARGET_FITNESS;
        boolean isPenaltyReached = penalty.getPenalty() <= TARGET_PENALTY;
        boolean isTimerReached = passedTimeInMinutes >= TARGET_TIMER_MINUTES;
        boolean isGenerationReached = generationNumber >= TARGET_GENERATION;

        String generationInfo = scheduler.printGenerationInfo(generationNumber, population);

        scheduler.printScheduleAsTable(population.getSchedules().get(0), generationNumber);
        scheduler.classNumb = 1;

        while (!isFitnessReached && !isPenaltyReached && !isTimerReached && !isGenerationReached) {
            generationNumber += 1;
            population = geneticAlgorithm.evolve(population).sortByFitness();

            if (Scheduler.IS_SCHEDULE_PRINTED_ON_GENERATION) {
                scheduler.printScheduleAsTable(population.getSchedules().get(0), generationNumber);
            }
            if (Scheduler.IS_GEN_INFO_PRINTED_ON_GENERATION) {
                generationInfo = scheduler.printGenerationInfo(generationNumber, population);
            }

            Schedule scheduleEvolve = population.getSchedules().get(0);
            Penalty penaltyEvolve = new Penalty(scheduleEvolve);

            isFitnessReached = penaltyEvolve.getFitness() == TARGET_FITNESS;
            isPenaltyReached = penaltyEvolve.getPenalty() <= TARGET_PENALTY;
            isTimerReached = passedTimeInMinutes >= TARGET_TIMER_MINUTES;
            isGenerationReached = generationNumber >= TARGET_GENERATION;

            passedTimeInMs = watch.time();
            passedTimeInMinutes = watch.time(TimeUnit.MINUTES);

            System.out.print("Time (seconds): " + passedTimeInMs / 1000);
            System.out.println("        |    Time (minutes): " + passedTimeInMinutes);

            scheduler.classNumb = 1;

            if (isTimerReached || isPenaltyReached || isFitnessReached) {
                System.out.print("-----------------------------------------------------------------------------------");
                System.out.println("-------------------------------------------------------------------------------------");
            }

            if (isTimerReached) {
                System.out.println("Process stopped because maximum time in minutes ("
                        + TARGET_TIMER_MINUTES + ") has been reached.");
            }
            if (isPenaltyReached) {
                System.out.println("Process stopped because minimum number of penalty ("
                        + TARGET_PENALTY + ") has been reached");
            }
            if (isFitnessReached) {
                System.out.println("Process stopped because targeted fitness value ("
                        + TARGET_FITNESS + ") has been reached");
            }

            if (isGenerationReached) {
                System.out.println("Process stopped because targeted generation value ("
                        + TARGET_GENERATION + ") has been reached");
            }
        }

        printWriter.println(generationInfo);
        printWriter.print("Time (seconds): " + passedTimeInMs / 1000);
        printWriter.println("        |    Time (minutes): " + passedTimeInMinutes);
        printWriter.println(population.getSchedules().get(0));

        String schedulesAsTable = stringWriter.toString();
        String schedulesAsList = population.getSchedules().get(0).getScheduleAsList();
        System.out.println(schedulesAsTable);

        String paramsInfo = printParametersInfo();
        String scheduleFileName = "Schedules-GA__" + dateParser.getTodayDate("dd-MM-yyyy hh.mm.ss") + ".txt";
        fileManager.createTextFile(paramsInfo.concat(schedulesAsTable), scheduleFileName, "results/ga/");

        String schedulesAsListContent = paramsInfo.concat(generationInfo).concat(schedulesAsList);
        fileManager.createTextFile(schedulesAsListContent, "List__" + scheduleFileName, "results/ga/");

    }

    private String printParametersInfo() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        printWriter.print("-----------------------------------------------------------------------------------");
        printWriter.println("-------------------------------------------------------------------------------------");

        printWriter.print("Population size # " + POPULATION_SIZE);
        printWriter.println("       |         Mutation rate "
                + MUTATION_RATE
                + "       |         Crossover rate # "
                + CROSSOVER_RATE);
        printWriter.println("Tournament selection size # "
                + TOURNAMENT_SELECTION_SIZE
                + "  |                  Number of elite schedules # "
                + NUMB_OF_ELITE_SCHEDULES
                + " ");
        printWriter.print("-----------------------------------------------------------------------------------");
        printWriter.println("-------------------------------------------------------------------------------------");

        String info = stringWriter.toString();
        return info;
    }

    private String printGenerationInfo(int generationNumber, Population population) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        Penalty penalty = new Penalty(population.getSchedules().get(0));

        printWriter.println("Generation # " + generationNumber);
        printWriter.print("Schedule # " + generationNumber);
        printWriter.println("       |         Fitness # "
                + penalty.getFitness()
                + "       |         Penalty # "
                + penalty.getPenalty()
                + "  |                  Conflicts # "
                + penalty.getNumbOfConflicts() + " ");

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
                    + " - " + x.getTimeslot().getDuration() + " hours             | "));
            printWriter.print(String.format("%1$10s", data.getRooms().get(roomsIndex).getName() + "     | "));
            printWriter.print(String.format("%1$15s", data.getInstructors().get(instructorsIndex).getName()
                    + " (" + data.getInstructors().get(instructorsIndex).getId() + ")") + "  | ");
            printWriter.print(data.getTimeslots().get(meetingTimeIndex).getTime()
                    + " (" + data.getTimeslots().get(meetingTimeIndex).getId() + ")  | ");
            printWriter.print(data.getGroups().get(groupIndex).getName()
                    + " (" + data.getGroups().get(groupIndex).getId() + ")");

            classNumb++;
        });

        Penalty penalty = new Penalty(schedule);
        if (penalty.getFitness() == 1) {
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

        printWriter.println("\nAvailable Courses");
        data.getCourses().forEach(x -> printWriter.println("course #: " + x.getId() + ", name: " + x.getName()
                + ", instructors: " + data.findInstructorById(x.getInstructorId()).getName()));
        printWriter.println("\nAvailable Rooms");
        data.getRooms().forEach(x -> printWriter.println("room #: " + x.getName()));
        printWriter.println("\nAvailable Instructors");
        data.getInstructors().forEach(x -> printWriter.println("id: " + x.getId() + ", name: " + x.getName()));
        printWriter.println("\nAvailable Meeting Times");
        data.getTimeslots().forEach(x -> printWriter.println("id: " + x.getId() + ", Meeting Time: " + x.getTime()));
        printWriter.print("------------------------------------------------------------------------------------");
        printWriter.println("-------------------------------------------------------------------------------------");

        String availableData = stringWriter.toString();
        System.out.println(availableData);
    }

    public static void main(String[] args) {
        Scheduler scheduler = new Scheduler();
        scheduler.runAlgorithm(false, 0);
    }

}
