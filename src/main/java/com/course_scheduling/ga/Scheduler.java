package com.course_scheduling.ga;

import com.course_scheduling.assets.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    // adjust these values as needed
    public static int POPULATION_SIZE = 10;
    public static double MUTATION_RATE = 0.7;
    public static double CROSSOVER_RATE = 0.3;
    public static int NUMB_OF_POPULATION_COMPETITOR = 3;
    public static int NUMB_OF_TOP_SCHEDULES = 1;
    public static int NUMB_OF_LARGE_SIZED_SCHEDULE_CLASSES = 250;

    // adjust these values as needed
    public static double TARGET_FITNESS = 1.0;
    public static int TARGET_PENALTY = 20;
    public static int TARGET_TIMER_MINUTES = 15;
    public static int TARGET_GENERATION = 100000;
    public static boolean IS_SCHEDULE_PRINTED_ON_GENERATION = false;
    public static boolean IS_GEN_INFO_PRINTED_ON_GENERATION = true;

    private Data data;

    private Timer watch = Timer.start();
    private FileManager fileManager = new FileManager();
    private DateParser dateParser = new DateParser();

    public void runAlgorithm(boolean IS_EXPERIMENT_MODE) {
        runAlgorithm(false, 0);
    }

    public void runAlgorithm(boolean IS_EXPERIMENT_MODE, int EXPERIMENT_TYPE) {
        Data.IS_EXPERIMENT_MODE = IS_EXPERIMENT_MODE;
        Data.EXPERIMENT_TYPE = EXPERIMENT_TYPE;

        Scheduler scheduler = new Scheduler();
        scheduler.data = new Data();
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

        String generationInfo = scheduler.getGenerationInfo(generationNumber, population);
        String firstScheduleAsTable = schedule.getScheduleAsTable();
        System.out.println(generationInfo);
        System.out.println(firstScheduleAsTable);

        while (!isFitnessReached && !isPenaltyReached && !isTimerReached && !isGenerationReached) {
            generationNumber += 1;
            population = geneticAlgorithm.regenerate(population).sortByFitness();

            if (Scheduler.IS_SCHEDULE_PRINTED_ON_GENERATION) {
                Schedule bestSchedule = population.getSchedules().get(0);
                String bestScheduleAsTable = bestSchedule.getScheduleAsTable();
                System.out.println(bestScheduleAsTable);
            }

            if (Scheduler.IS_GEN_INFO_PRINTED_ON_GENERATION) {
                generationInfo = scheduler.getGenerationInfo(generationNumber, population);
                System.out.println(generationInfo);
            }

            Schedule bestSchedule = population.getSchedules().get(0);
            Penalty bestSchedulePenalty = new Penalty(bestSchedule);

            isFitnessReached = bestSchedulePenalty.getFitness() == TARGET_FITNESS;
            isPenaltyReached = bestSchedulePenalty.getPenalty() <= TARGET_PENALTY;
            isTimerReached = passedTimeInMinutes >= TARGET_TIMER_MINUTES;
            isGenerationReached = generationNumber >= TARGET_GENERATION;

            passedTimeInMs = watch.time();
            passedTimeInMinutes = watch.time(TimeUnit.MINUTES);

            System.out.print("Time (seconds): " + passedTimeInMs / 1000);
            System.out.println("        |    Time (minutes): " + passedTimeInMinutes);
            System.out.print("-----------------------------------------------------------------------------------");
            System.out.println("-------------------------------------------------------------------------------------");

            if (isTimerReached || isPenaltyReached || isFitnessReached || isGenerationReached) {
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

                if (penalty.getFitness() == 1) {
                    printWriter.println("Solution found in " + (generationNumber + 1) + " generations");
                }
            }

            if (isGenerationReached) {
                System.out.println("Process stopped because targeted generation value ("
                        + TARGET_GENERATION + ") has been reached");
            }

        }

        printWriter.print("Time (seconds): " + passedTimeInMs / 1000);
        printWriter.println("        |    Time (minutes): " + passedTimeInMinutes);
        printWriter.print("-----------------------------------------------------------------------------------");
        printWriter.println("-------------------------------------------------------------------------------------");
        printWriter.print(generationInfo);
        printWriter.print("-----------------------------------------------------------------------------------");
        printWriter.println("-------------------------------------------------------------------------------------");

        Schedule bestSchedule = population.getSchedules().get(0);
        String schedulesAsTable = bestSchedule.getScheduleAsTable();
        String schedulesAsList = bestSchedule.getScheduleAsList();

        String printInfo = stringWriter.toString();
        String paramsInfo = getParametersInfo();
        System.out.println(printInfo.concat(paramsInfo).concat(schedulesAsTable));

        String scheduleFileName = "Schedules-GA__" + dateParser.getTodayDate("dd-MM-yyyy HH.mm.ss") + ".txt";
        String schedulesAsTableContent = printInfo.concat(paramsInfo).concat(schedulesAsTable);
        fileManager.createTextFile(schedulesAsTableContent, scheduleFileName, "results/ga/");

        String line = "\n------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
        String schedulesAsListContent = printInfo.concat(paramsInfo).concat(line).concat(schedulesAsList);
        fileManager.createTextFile(schedulesAsListContent, "List__" + scheduleFileName, "results/ga/");

    }

    private String getParametersInfo() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        printWriter.print("Population size # " + POPULATION_SIZE);
        printWriter.println("       |         Mutation rate "
                + MUTATION_RATE
                + "       |         Crossover rate # "
                + CROSSOVER_RATE);
        printWriter.println("Number of population competitor # "
                + NUMB_OF_POPULATION_COMPETITOR
                + "  |                  Number of top schedules # "
                + NUMB_OF_TOP_SCHEDULES
                + " ");

        String info = stringWriter.toString();
        return info;
    }

    private String getGenerationInfo(int generationNumber, Population population) {
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
                + penalty.getNumbOfConflicts());

        String info = stringWriter.toString();

        return info;
    }

    public static void main(String[] args) {
        Scheduler scheduler = new Scheduler();
        scheduler.runAlgorithm(false);
    }

}
