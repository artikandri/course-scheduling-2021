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
        classes = new ArrayList<Class>(data.getNumberOfClasses());
    }

    public Schedule initialize() {
        int prevGroupId = -1;
        List<Integer> prevTimeslotIdsForGroup = new ArrayList(Arrays.asList());
        List<Course> courses = data.getCourses();

        for (Course course : courses) {
            List<Integer> preferredTimeslots
                    = data.findInstructorById(course.getInstructorId()).getPreferences();

            // consider duration
            List possibleDurations = course.getPossibleDurations();
            double randomPossibleDurationIndex = possibleDurations.size() * Math.random();
            List<Double> randomDurations
                    = (List) possibleDurations.get((int) randomPossibleDurationIndex);
            List<Integer> prevTimeslotIds = new ArrayList(Arrays.asList());
            Map possibleTimeslotMap = course.getPossibleTimeslotMap();

            // define room
            Room room = data.getRooms().get((int) (data.getRooms().size() * Math.random()));

            // flag to check course day
            boolean isCourseScheduleValid = true;

            // create new class for every durations
            // match duration with timeslot
            // > 1 hour means multiple classes
            if (isCourseScheduleValid) {
                for (Double duration : randomDurations) {
                    Class newClass = new Class(classNumb++, course);
                    newClass.setRoom(room);
                    newClass.setInstructor(data.findInstructorById(course.getInstructorId()));
                    newClass.setGroup(data.findGroupById(course.getGroupId()));
                    newClass.setNumbOfClasses(randomDurations.size());

                    List<Integer> possibleTimeslots
                            = (List) possibleTimeslotMap.get(duration.toString());
                    List<Integer> suitablePreferredTimeslots = preferredTimeslots.stream()
                            .collect(Collectors.filtering(
                                    timeslotId -> possibleTimeslots.contains(timeslotId),
                                    Collectors.toList()));

                    boolean isPreferenceConsidered = !suitablePreferredTimeslots.isEmpty();
                    Timeslot randomTimeslot;
                    boolean isTimeslotAlreadyAdded = true;
                    do {
                        randomTimeslot = getSuitableTimeslot(course, 
                                prevTimeslotIds,
                                prevTimeslotIdsForGroup,
                                duration.toString(), 
                                isPreferenceConsidered);
                        isTimeslotAlreadyAdded = prevTimeslotIds.contains(randomTimeslot.getId())
                                || prevTimeslotIdsForGroup.contains(randomTimeslot.getId());

                    } while (isTimeslotAlreadyAdded);

                    boolean isTimeslotUnique = !prevTimeslotIds.contains((int) randomTimeslot.getId());
                    boolean canAssignTimeslot = prevTimeslotIds.isEmpty() || isTimeslotUnique;

                    if (canAssignTimeslot) {
                        prevTimeslotIds.add(randomTimeslot.getId());

                        if (prevGroupId == -1) {
                            prevGroupId = course.getGroupId();
                        }

                        if (course.getGroupId() == prevGroupId) {
                            prevTimeslotIdsForGroup.add(randomTimeslot.getId());
                        }

                        newClass.setTimeslot(randomTimeslot);

                        classes.add(newClass);
                    }
                }

            } else {
                // repeat the process for the course
                courses.add(course);
                classes = classes
                        .stream()
                        .collect(Collectors.filtering(
                                cClass -> cClass.getCourseId() != course.getId(),
                                Collectors.toList()));
                isCourseScheduleValid = true;
            }

            // clean timeslots
            if (course.getGroupId() != prevGroupId) {
                prevGroupId = course.getGroupId();
                prevTimeslotIdsForGroup.clear();
            }
            prevTimeslotIds.clear();
        }

        return this;
    }

    private Timeslot getSuitableTimeslot(Course course, List prevTimeslotIds,
            List prevTimeslotIdsForGroup, String duration,
            boolean shouldCheckPreference) {

        // consider possible timeslot
        Map possibleTimeslotMap = course.getPossibleTimeslotMap();
        List<Integer> possibleTimeslots
                = (List) possibleTimeslotMap.get(duration);

        // set alternative timeslots
        List<Integer> possibleAltTimeslots = new ArrayList<>(Arrays.asList());
        if ("1.0".equals(duration)) {
            possibleAltTimeslots = (List) possibleTimeslotMap.get("1.5");
        }

        // flag to check invalid timeslot
        boolean isFlagged = false;

        // get preferences
        List<Integer> preferredTimeslots
                = data.findInstructorById(course.getInstructorId()).getPreferences();
        List<Integer> suitablePreferredTimeslots = preferredTimeslots.stream()
                .collect(Collectors.filtering(
                        timeslotId -> possibleTimeslots.contains(timeslotId),
                        Collectors.toList()));

        // filter available timeslots
        List<Timeslot> suitableTimeslotObjects = data.getTimeslots().stream()
                .collect(Collectors.filtering(
                        timeslot -> possibleTimeslots.contains(timeslot.getId())
                        && !prevTimeslotIds.contains(timeslot.getId())
                        && !prevTimeslotIdsForGroup.contains(timeslot.getId()),
                        Collectors.toList()));
        List<Timeslot> suitableAltTimeslotObjects = data.getTimeslots().stream()
                .collect(Collectors.filtering(
                        timeslot
                        -> possibleAltTimeslots.contains(timeslot.getId())
                        && !prevTimeslotIds.contains(timeslot.getId())
                        && !prevTimeslotIdsForGroup.contains(timeslot.getId()),
                        Collectors.toList()));
        List<Timeslot> tempSuitableTimeslotObjects = new ArrayList<>(Arrays.asList());

        if (suitableTimeslotObjects.isEmpty()) {
            if (suitableAltTimeslotObjects.isEmpty()) {
                suitableTimeslotObjects = data.getTimeslots().stream()
                        .collect(Collectors.filtering(
                                timeslot -> possibleTimeslots.contains(timeslot.getId()),
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

        if (suitableTimeslotObjects.contains(nextTimeslot)) {
            randomTimeslot = nextTimeslot;
        }

        return randomTimeslot;
    }

    public ArrayList<Class> getClasses() {
        return classes;
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
            printWriter.println("Group " + classInGroup.getKey());
            for (Class gClass : classInGroup.getValue()) {
                String classSchedule = gClass.getTimeslot().getTime()
                        + " = "
                        + gClass.getCourse().getName()
                        + " ("
                        + gClass.getRoom().getName()
                        + ") ";
                printWriter.println(classSchedule);
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
