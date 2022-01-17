package com.course_scheduling.ga;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

public class Schedule {

    private ArrayList<Class> classes;
    private int classNumb = 0;
    private Data data;

    public Data getData() {
        return data;
    }

    public Schedule(Data data) {
        this.data = data;
        classes = new ArrayList<Class>(data.getNumberOfCourses());
    }

    public Schedule initialize() {
        int prevGroupId = -1;
        List<Integer> prevTimeslotIdsForGroup = new ArrayList(Arrays.asList());
        List<Course> courses = data.getCourses();

        for (Course course : courses) {

            // consider duration
            List possibleDurations = course.getPossibleDurations();
            double randomPossibleDurationIndex = possibleDurations.size() * Math.random();
            List<Double> randomDurations
                    = (List) possibleDurations.get((int) randomPossibleDurationIndex);
            List<Integer> prevTimeslotIds = new ArrayList(Arrays.asList());

            // define room
            Room room = data.getRooms().get((int) (data.getRooms().size() * Math.random()));

            // create new class for every durations
            // match duration with timeslot
            // > 1 hour means multiple classes
            for (Double duration : randomDurations) {
                Class newClass = new Class(classNumb++, course);
                newClass.setRoom(room);
                newClass.setInstructor(data.findInstructorById(course.getInstructorId()));
                newClass.setGroup(data.findGroupById(course.getGroupId()));
                newClass.setNumbOfClasses(randomDurations.size());

                Timeslot randomTimeslot;
                randomTimeslot = getSuitableTimeslot(course,
                        prevTimeslotIds,
                        prevTimeslotIdsForGroup,
                        duration.toString());

                if (prevGroupId == -1) {
                    prevGroupId = course.getGroupId();
                }

                // set timeslot
                newClass.setTimeslot(randomTimeslot);
                prevTimeslotIds.add(randomTimeslot.getId());
                if (course.getGroupId() == prevGroupId) {
                    prevTimeslotIdsForGroup.add(randomTimeslot.getId());
                } else {
                    prevTimeslotIdsForGroup.clear();
                    prevTimeslotIdsForGroup.add(randomTimeslot.getId());
                    prevGroupId = course.getGroupId();
                }

                // add class
                classes.add(newClass);
            }

            // clear timeslot for the course
            prevTimeslotIds.clear();

        } //end of for

        return this;
    }

    private Timeslot getSuitableTimeslot(
            Course course,
            List prevTimeslotIds,
            List prevTimeslotIdsForGroup,
            String duration) {

        // consider possible timeslot
        Map possibleTimeslotMap = course.getPossibleTimeslotMap();
        List<Integer> possibleTimeslots
                = (List) possibleTimeslotMap.get(duration);
        List<Integer> possibleAltTimeslots = duration == "1.0" ? (List) possibleTimeslotMap.get("1.5") : new ArrayList<>(Arrays.asList());

        // get preferences
        List<Integer> preferredTimeslots
                = data.findInstructorById(course.getInstructorId()).getPreferences();
        List<Integer> suitablePreferredTimeslots = preferredTimeslots.stream()
                .collect(Collectors.filtering(
                        timeslotId -> possibleTimeslots.contains(timeslotId),
                        Collectors.toList()));
        boolean checkPreference = !suitablePreferredTimeslots.isEmpty();

        // filter available timeslots
        List<Timeslot> suitableTimeslotObjects = data.getTimeslots().stream()
                .collect(Collectors.filtering(
                        timeslot -> possibleTimeslots.contains(timeslot.getId())
                        && (checkPreference ? preferredTimeslots.contains(timeslot.getId()) : true)
                        && !prevTimeslotIds.contains(timeslot.getId())
                        && !prevTimeslotIdsForGroup.contains(timeslot.getId()),
                        Collectors.toList()));

        List<Timeslot> suitableAltTimeslotObjects = data.getTimeslots().stream()
                .collect(Collectors.filtering(
                        timeslot -> possibleAltTimeslots.contains(timeslot.getId())
                        && (checkPreference ? preferredTimeslots.contains(timeslot.getId()) : true)
                        && !prevTimeslotIds.contains(timeslot.getId())
                        && !prevTimeslotIdsForGroup.contains(timeslot.getId()),
                        Collectors.toList()));

        if (suitableTimeslotObjects.isEmpty()) {
            if (suitableAltTimeslotObjects.isEmpty()) {
                suitableTimeslotObjects = data.getTimeslots().stream()
                        .collect(Collectors.filtering(
                                timeslot -> possibleTimeslots.contains(timeslot.getId())
                                && !prevTimeslotIds.contains(timeslot.getId()),
                                Collectors.toList()));
            } else {
                suitableTimeslotObjects = suitableAltTimeslotObjects;
            }
        }

        // create random timeslot
        int prevTimeslotId = prevTimeslotIds.isEmpty() ? 0 : (int) prevTimeslotIds.get(prevTimeslotIds.size() - 1);
        int nextTimeslotId = prevTimeslotId <= data.getTimeslots().size() - 2 ? prevTimeslotId + 1 : -1;

        // get next timeslot if possible
        // if not, assign random timeslot
        double randomTimeslotIndex = suitableTimeslotObjects.size() * Math.random();
        Timeslot randomTimeslot
                = suitableTimeslotObjects.get((int) randomTimeslotIndex);
        Timeslot nextTimeslot = nextTimeslotId == -1 ? randomTimeslot : data.getTimeslots().get(nextTimeslotId);

        // encourage randomizing to avoid identical schedules
        // generation may stop without notice when schedules are identical with each other
        boolean isScheduleSmall = data.getNumberOfCourses() <= Scheduler.NUMB_OF_MEDIUM_SIZED_SCHEDULE_COURSES;
        boolean isRandomizingEncouraged = Math.random() * 100 <= (isScheduleSmall ? 70 : 20);
        if (possibleTimeslots.contains(nextTimeslot.getId()) && !isRandomizingEncouraged) {
            randomTimeslot = nextTimeslot;
        }

        return randomTimeslot;
    }

    public ArrayList<Class> getClasses() {
        return classes;
    }

    public void setClasses(ArrayList classes) {
        this.classes = classes;
    }

    @Override
    public String toString() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        printWriter.print("------------------------------------------------------------------------------------");
        printWriter.println("------------------------------------------------------------------------------------");
        printWriter.println("Class # | Course (Id) - Weekly hours | Room  |   Instructor (Id)   |  Meeting Time (Id) | Group (Id)");
        printWriter.print("------------------------------------------------------------------------------------");
        printWriter.println("------------------------------------------------------------------------------------");

        classes.sort(Comparator.comparing(Class::getCourseId).thenComparing(Class::getTimeslotId));

        for (Class x : classes) {
            int coursesIndex = data.getCourses().indexOf(x.getCourse());
            int roomsIndex = data.getRooms().indexOf(x.getRoom());
            int instructorsIndex = data.getInstructors().indexOf(x.getInstructor());
            int meetingTimeIndex = data.getTimeslots().indexOf(x.getTimeslot());
            int groupIndex = data.getCourses().get(coursesIndex).getGroupId();

            printWriter.println("                       ");
            printWriter.print(String.format("  %1$02d  ", classes.indexOf(x)) + "  | ");
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
        }

        printWriter.print("\n-----------------------------------------------------------------------------------");
        printWriter.println("-------------------------------------------------------------------------------------");

        String scheduleTables = stringWriter.toString();
        return scheduleTables;
    }

    public String getScheduleAsList() {
        // rewrote this function to produce schedules
        // in similar style with the pso algorithm

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        classes.sort(Comparator.comparing(Class::getGroupId).thenComparing(Class::getTimeslotId));
        Map<String, List<Class>> classInGroups = classes.stream()
                .collect(Collectors.groupingBy(p -> String.valueOf(p.getGroupId())));

        for (Map.Entry<String, List<Class>> classInGroup : classInGroups.entrySet()) {
            printWriter.println("--Group " + classInGroup.getKey() + ":");

            List<Integer> classTimeslotIds = classInGroup.getValue()
                    .stream()
                    .map(Class::getTimeslotId)
                    .collect(Collectors.toList());

            for (Timeslot timeslot : data.getTimeslots()) {
                if (classTimeslotIds.contains(timeslot.getId())) {
                    int classIndex = classTimeslotIds.indexOf(timeslot.getId());
                    Class gClass = classInGroup.getValue().get(classIndex);
                    String classSchedule = gClass.getTimeslot().getTime()
                            + " = "
                            + gClass.getCourse().getName()
                            + " ("
                            + gClass.getRoom().getName()
                            + ") ";
                    printWriter.println(classSchedule);
                } else {
                    String classSchedule = timeslot.getTime()
                            + " = (-)";
                    printWriter.println(classSchedule);
                }
            }
        }

        String scheduleTables = stringWriter.toString();
        return scheduleTables;
    }

    public static void main(String[] args) {
        Data data = new Data();
        Schedule schedule = new Schedule(data);

        schedule.initialize();
    }

}
