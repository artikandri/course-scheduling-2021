package com.course_scheduling.ga;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class Driver {

    public static final int POPULATION_SIZE = 9;
    public static final double MUTATION_RATE = 0.1;
    public static final double CROSSOVER_RATE = 0.9;
    public static final int TOURNAMENT_SELECTION_SIZE = 3;
    public static final int NUMB_OF_ELITE_SCHEDULES = 1;
    private int scheduleNumb = 0;
    private int classNumb = 1;
    private Data data;

    public void runAlgorithm() {
        Driver driver = new Driver();
        driver.data = new Data();
        driver.printAvailableData();
        driver.generateSchedules();
    }

    private void generateSchedules() {
        Driver driver = new Driver();
        driver.data = new Data();
        int generationNumber = 0;

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        printWriter.println("> Generation # " + generationNumber);
        printWriter.print("  Schedule # |                                           ");
        printWriter.print("Classes [class, room, instructor, timeslot, group]       ");
        printWriter.println("                                  | Fitness | Conflicts");
        printWriter.print("-----------------------------------------------------------------------------------");
        printWriter.println("-------------------------------------------------------------------------------------");
        GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(driver.data);
        Population population = new Population(Driver.POPULATION_SIZE, driver.data).sortByFitness();
        population.getSchedules().forEach(schedule
                -> printWriter.println("       " + driver.scheduleNumb++
                        + "     | " + schedule + " | "
                        + String.format("%.5f", schedule.getFitness())
                        + " | " + schedule.getNumbOfConflicts()));
        driver.printScheduleAsTable(population.getSchedules().get(0), generationNumber);
        driver.classNumb = 1;

        printWriter.println(population.getSchedules().get(0).getFitness());

        while (population.getSchedules().get(0).getFitness() < 1.0) {

            generationNumber += 1;
            driver.printGenerationInfo(generationNumber, population);

            population = geneticAlgorithm.evolve(population).sortByFitness();
            driver.scheduleNumb = 0;
            population.getSchedules().forEach(schedule
                    -> printWriter.println("       " + driver.scheduleNumb++
                            + "     | " + schedule + " | "
                            + String.format("%.5f", schedule.getFitness())
                            + " | " + schedule.getNumbOfConflicts()));

            driver.printScheduleAsTable(population.getSchedules().get(0), generationNumber);
            driver.classNumb = 1;
        }

        String schedules = stringWriter.toString();
        System.out.println(schedules);
    }

    private void printGenerationInfo(int generationNumber, Population population) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        printWriter.println("Generation # " + generationNumber);
        printWriter.print("Schedule # " + generationNumber);

        printWriter.println("       |         Fitness # "
                + population.getSchedules().get(0).getFitness()
                + "  |                  Conflicts # "
                + population.getSchedules().get(0).getNumbOfConflicts() + " ");
        printWriter.print("-----------------------------------------------------------------------------------");
        printWriter.println("-------------------------------------------------------------------------------------");

        String info = stringWriter.toString();
        System.out.println(info);
    }

    private void printScheduleAsTable(Schedule schedule, int generation) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        ArrayList<Class> classes = schedule.getClasses();
        printWriter.println("\n                       ");
        printWriter.println("------------------------------------------------------------------------------------");
        printWriter.println("------------------------------------------------------------------------------------");

        printWriter.println("Class # | Course (number) | Room  |   Instructor (Id)   |  Meeting Time (Id) | Group (Id)");
        printWriter.println("------------------------------------------------------------------------------------");
        printWriter.println("------------------------------------------------------------------------------------");

        classes.forEach(x -> {
            int coursesIndex = data.getCourses().indexOf(x.getCourse());
            int roomsIndex = data.getRooms().indexOf(x.getRoom());
            int instructorsIndex = data.getInstructors().indexOf(x.getInstructor());
            int meetingTimeIndex = data.getTimeslots().indexOf(x.getTimeslot());
            int groupIndex = data.getCourses().get(coursesIndex).getGroupId();

            printWriter.println("                       ");
            printWriter.print(String.format("  %1$02d  ", classNumb) + "  | ");
            printWriter.print(String.format("%1$21s", data.getCourses().get(coursesIndex).getName() + " (" + data.getCourses().get(coursesIndex).getId() + ")             | "));
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
            printWriter.println("> Solution Found in " + (generation + 1) + " generations");
        }
        printWriter.println("\n-----------------------------------------------------------------------------------");

        String scheduleTables = stringWriter.toString();
        System.out.println(scheduleTables);
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
