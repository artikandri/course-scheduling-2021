package com.course_scheduling.ga;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Schedule {

    private ArrayList<Class> classes;
    private boolean isFitnessChanged = true;
    private double fitness = -1;
    private int classNumb = 0;
    private int numbOfConflicts = 0;
    private int penalty = 0;
    private Data data;

    public Data getData() {
        return data;
    }

    public Schedule(Data data) {
        this.data = data;
        classes = new ArrayList<Class>(data.getNumberOfClasses());
    }

    public Schedule initialize() {
        // add class for each course
        new ArrayList<Course>(data.getCourses()).forEach(course -> {
            // consider timeslot
            List<Integer> preferredTimeslots
                    = data.findInstructorById(course.getInstructorId()).getPreferences();
            List<Timeslot> preferredTimeslotObjects = data.getTimeslots().stream()
                    .filter(timeslot -> preferredTimeslots.contains(timeslot.getId()))
                    .collect(Collectors.toList());

            // consider duration
            List possibleDurations = course.getPossibleDurations();
            double randomPossibleDurationIndex = possibleDurations.size() * Math.random();
            List<Double> randomDurations
                    = (List) possibleDurations.get((int) randomPossibleDurationIndex);

            // match duration with timeslot
            // add classes by considering the duration
            // > 1 hour means multiple classes
            List<Integer> previouslyChosenTimeslotIds = new ArrayList(Arrays.asList());
            Map possibleTimeslotMap = course.getPossibleTimeslotMap();

            // define room
            Room room = data.getRooms().get((int) (data.getRooms().size() * Math.random()));

            // flag to check course day
            int courseDayId = -1;
            int prevCourseId = -1;

            // create new class for every durations
            for (Double duration : randomDurations) {
                Class newClass = new Class(classNumb++, course);

                newClass.setRoom(room);
                newClass.setInstructor(data.findInstructorById(course.getInstructorId()));
                newClass.setGroup(data.findGroupById(course.getGroupId()));

                List<Integer> possibleTimeslots
                        = (List) possibleTimeslotMap.get(duration.toString());
                List<Integer> suitablePreferredTimeslots = preferredTimeslots.stream()
                        .collect(Collectors.filtering(
                                timeslotId -> possibleTimeslots.contains(timeslotId),
                                Collectors.toList()));

                boolean isPreferenceConsidered = !suitablePreferredTimeslots.isEmpty();
                Timeslot randomTimeslot = getSuitableTimeslot(course, previouslyChosenTimeslotIds, prevCourseId, courseDayId, duration.toString(), isPreferenceConsidered);

                boolean isTimeslotAlreadyAdded = previouslyChosenTimeslotIds.contains(randomTimeslot.getId());
                do {
                    randomTimeslot = getSuitableTimeslot(course, previouslyChosenTimeslotIds, prevCourseId, courseDayId, duration.toString(), isPreferenceConsidered);
                    isTimeslotAlreadyAdded = previouslyChosenTimeslotIds.contains(randomTimeslot.getId());
                } while (isTimeslotAlreadyAdded);

                boolean canAssignTimeslot = previouslyChosenTimeslotIds.isEmpty()
                        || !previouslyChosenTimeslotIds
                                .contains((int) randomTimeslot.getId());

                if (canAssignTimeslot) {
                    previouslyChosenTimeslotIds.add(randomTimeslot.getId());
                    newClass.setTimeslot(randomTimeslot);
                    courseDayId = randomTimeslot.getDayId();

                    classes.add(newClass);
                }
                prevCourseId = course.getId();
            }
            previouslyChosenTimeslotIds.clear();
        });
        return this;
    }

    private Timeslot getSuitableTimeslot(Course course, List previouslyChosenTimeslotIds, int prevCourseId, int courseDayId, String duration, boolean shouldCheckPreference) {
        Map possibleTimeslotMap = course.getPossibleTimeslotMap();
        // consider timeslot
        List<Integer> preferredTimeslots
                = data.findInstructorById(course.getInstructorId()).getPreferences();
        List<Integer> possibleTimeslots
                = (List) possibleTimeslotMap.get(duration.toString());
        List<Integer> suitablePreferredTimeslots = preferredTimeslots.stream()
                .collect(Collectors.filtering(
                        timeslotId -> possibleTimeslots.contains(timeslotId),
                        Collectors.toList()));
        List<Timeslot> suitablePreferredTimeslotObjects = data.getTimeslots().stream()
                .collect(Collectors.filtering(
                        timeslot -> suitablePreferredTimeslots.contains(timeslot.getId()) && possibleTimeslots.contains(timeslot.getId()),
                        Collectors.toList()));
        List<Timeslot> suitableTimeslotObjects = data.getTimeslots().stream()
                .collect(Collectors.filtering(
                        timeslot -> possibleTimeslots.contains(timeslot.getId()),
                        Collectors.toList()));

        // consider the day
        List<Timeslot> suitableTimeslotObjectsOnTheDay = suitableTimeslotObjects.stream()
                .collect(Collectors.filtering(
                        timeslot -> timeslot.getDayId() == courseDayId,
                        Collectors.toList())
                );

        // create random timeslot
        double randomTimeslotIndex = suitableTimeslotObjects.size() * Math.random();
        Timeslot randomTimeslot
                = suitableTimeslotObjects.get((int) randomTimeslotIndex);

        boolean isTimeslotOnTheDayExist = suitableTimeslotObjectsOnTheDay.contains(randomTimeslot);
        boolean isPreferredTimeslotExist = suitablePreferredTimeslots.contains(randomTimeslot.getId());

        // check preferences
        // not prioritized because penalty is lower
        if (isPreferredTimeslotExist && shouldCheckPreference) {
            randomTimeslotIndex = suitablePreferredTimeslotObjects.size() * Math.random();
            randomTimeslot = suitablePreferredTimeslotObjects.get((int) randomTimeslotIndex);
        }

        // make sure the same course get consecutive timeslot on the same day
        if (course.getId() == prevCourseId) {
            if (isTimeslotOnTheDayExist) {
                randomTimeslotIndex = (int) (suitableTimeslotObjectsOnTheDay.size() * Math.random());
                randomTimeslot = suitableTimeslotObjectsOnTheDay.get((int) randomTimeslotIndex);

                // if possible, schedule classes with the same course id consecutively
                if (!previouslyChosenTimeslotIds.isEmpty()) {
                    int lastChosenTimeslotId = Integer.parseInt(previouslyChosenTimeslotIds.get(previouslyChosenTimeslotIds.size() - 1).toString());
                    List<Timeslot> nextSuitableTimeslotObjectsOnTheDay = suitableTimeslotObjects.stream()
                            .collect(Collectors.filtering(
                                    timeslot -> timeslot.getId() == lastChosenTimeslotId + 1,
                                    Collectors.toList())
                            );

                    if (!nextSuitableTimeslotObjectsOnTheDay.isEmpty()) {
                        randomTimeslotIndex = (int) (nextSuitableTimeslotObjectsOnTheDay.size() * Math.random());
                        randomTimeslot = nextSuitableTimeslotObjectsOnTheDay.get((int) randomTimeslotIndex);
                    }
                }
            }
        }

        return randomTimeslot;
    }

    public int getNumbOfConflicts() {
        return numbOfConflicts;
    }

    public ArrayList<Class> getClasses() {
        isFitnessChanged = true;
        return classes;
    }

    public double getFitness() {
        if (isFitnessChanged == true) {
            fitness = calculateFitnessAndPenalty();
            isFitnessChanged = false;
        }
        return fitness;
    }

    public int getPenalty() {
        if (isFitnessChanged == true) {
            fitness = calculateFitnessAndPenalty();
            isFitnessChanged = false;
        }
        return penalty;
    }

    private double calculateFitnessAndPenalty() {
        numbOfConflicts = 0;
        penalty = 0;

        calculatePenaltyForHardConstraints();
        calculatePenaltyForInstructorPreferences();
        calculatePenaltyForCourseShape();
        calculatePenaltyForImbalancedLessons();

        return 1 / (double) (penalty + 1);
    }

    // 3. Soft constraint: Maintain preference of instructor as much as possible
    // penalty += 1
    private void calculatePenaltyForInstructorPreferences() {
        classes.forEach(x -> {
            Instructor xInstructor = x.getInstructor();
            List<Integer> xInstructorPreferences = xInstructor.getPreferences();
            Timeslot xTimeslot = x.getTimeslot();
            if (!xInstructorPreferences.isEmpty()
                    && !xInstructorPreferences.contains(xTimeslot.getId())) {
                penalty += 1;
            }
        });

    }

    // 1. Hard constraint: same instructor can't teach 2 different courses simultaneously
    // penalty += 99
    // 2. Hard constraint: same room can't be scheduled for 2 diff courses simultaneously
    // penalty += 99
    private void calculatePenaltyForHardConstraints() {
        classes.sort(Comparator.comparing(Class::getTimeslotId));
        classes.forEach(x -> {
            classes.stream().filter(y -> classes.indexOf(y) >= classes.indexOf(x)).forEach(y -> {
                if (x.getTimeslot().getId() == y.getTimeslot().getId() && x.getId() != y.getId()) {
                    if (x.getInstructor().getId() == y.getInstructor().getId()) {
                        numbOfConflicts++;
                        penalty += 99;
                    }

                    if (x.getRoom() == y.getRoom()) {
                        numbOfConflicts++;
                        penalty += 99;
                    }
                }

                // Additional soft constraint: classes with the same course id on the same day
                // should be assigned to the same room
                // commented because not used now
//                boolean isConsecutive = Math.abs(x.getTimeslot().getId() - y.getTimeslot().getId()) == 1;
//                if (x.getId() == y.getId() && isConsecutive && x.getRoom().getId() != y.getRoom().getId()) {
//                    penalty += 1;
//                }
            });
        });
    }

    private void calculatePenaltyForCourseShape() {
        // 4. Soft constraint: geometrical shapes of 2h, 3h, 4h courses must be considered
        // 2h courses should be scheduled consecutively
        // 3h courses may be scheduled consecutively OR on different days, but the differences must
        // not be high and the shape should be 2+1
        // 4h courses should not be scheduled consecutively, better to split them into 2 days
        // penalty += 2

        classes.sort(Comparator.comparing(Class::getTimeslotId));
        Map<String, List<Class>> classInCourses = classes
                .stream()
                .collect(Collectors.groupingBy(p -> String.valueOf(p.getCourseId())));

        for (Map.Entry<String, List<Class>> classInCourse : classInCourses.entrySet()) {
            Class randomClass = classInCourse.getValue().get(0);
            Course course = randomClass.getCourse();

            // course with weeklyHours = 2 may only get assigned into 2 classes
            if (course.getWeeklyHours() == 2.0) {
                for (int i = 0; i < classInCourse.getValue().size(); i++) {
                    Class prevClass = classInCourse.getValue().get(i);
                    if (i + 1 < 2) {
                        Class nextClass = classInCourse.getValue().get(i + 1);
                        boolean isConsecutive = Math.abs(nextClass.getTimeslot().getId() - prevClass.getTimeslot().getId()) == 1;
                        if (!isConsecutive) {
                            penalty += 2;

                        }
                    }
                }
            }

            // course with weeklyHours = 3 may get assigned into 3 classes (1 hour each)
            // or 2 classes (1.5 hours each)
            if (course.getWeeklyHours() == 3.0) {
                for (int i = 0; i < classInCourse.getValue().size(); i++) {
                    Class prevClass = classInCourse.getValue().get(i);
                    if (i + 1 < classInCourse.getValue().size()) {
                        Class nextClass = classInCourse.getValue().get(i + 1);
                        boolean isConsecutive = Math.abs(nextClass.getTimeslot().getId() - prevClass.getTimeslot().getId()) == 1;
                        boolean isDistanceMoreThan7 = Math.abs(nextClass.getTimeslot().getId() - prevClass.getTimeslot().getId()) >= 7;

                        if (!isConsecutive) {
                            penalty += 2;
                        }

                        if (isDistanceMoreThan7) {
                            penalty += 2;
                        }
                    }

                }
            }

            // course with weeklyHours = 4 may get assigned into 4 classes (1 hour each)
            // or 3 classes (1.5 hours for 2 classes and 1 hour for 1 class)
            if (course.getWeeklyHours() == 4.0) {
                // check number of days
                // ideally the classes should be splitted into 2 days (3 and 1 combination)
                Map<String, List<Class>> classInCoursesInDays = classInCourse.getValue().stream()
                        .collect(Collectors.groupingBy(p -> String.valueOf(p.getTimeslotDayId())));
                if (classInCoursesInDays.size() != 2) {
                    penalty += 2;

                }

                // check number of classes in one day
                // ideally the max number of classes is 2
                for (Map.Entry<String, List<Class>> classInCoursesInDay : classInCoursesInDays
                        .entrySet()) {
                    if (classInCoursesInDay.getValue().size() > 2) {
                        penalty += 2;
                    }

                    // check consecutive classes in one day
                    for (int i = 0; i < classInCoursesInDay.getValue().size(); i++) {
                        Class prevClass = classInCoursesInDay.getValue().get(i);
                        if (i < classInCoursesInDay.getValue().size() - 1) {
                            Class nextClass = classInCoursesInDay.getValue().get(i + 1);
                            boolean isConsecutive = Math.abs(nextClass.getTimeslot().getId() - prevClass.getTimeslot().getId()) == 1;

                            if (!isConsecutive) {
                                penalty += 2;
                            }
                        }

                    }
                }

            }
        }
    }

    /*
     * calculates penalty for imbalanced lessons on each day
     */
    private void calculatePenaltyForImbalancedLessons() {
        // 5. Soft constraint: number of daily lessons for students should be evenly balanced
        // group the schedules by group id and check the number of lessons on each day
        // penalty += 1
        classes.sort(Comparator.comparing(Class::getTimeslotId));
        Map<String, List<Class>> classInGroups = classes.stream()
                .collect(Collectors.groupingBy(p -> String.valueOf(p.getGroupId())));

        for (Map.Entry<String, List<Class>> classInGroup : classInGroups.entrySet()) {
            Map<String, List<Class>> groupInDays = classInGroup.getValue().stream()
                    .collect(Collectors.groupingBy(p -> String.valueOf(p.getTimeslotDayId())));

            for (Map.Entry<String, List<Class>> groupInDay : groupInDays.entrySet()) {
                if (groupInDay.getValue().size() - 1 > 6) {
                    penalty += 1;
                }
            }
        }
    }

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
                    + " - " + data.getCourses().get(coursesIndex).getWeeklyHours() + " hours             | "));
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

    // function is not used
    // only here to help debugging
    // to do: remove before submission
    public static void main(String[] args) {
        Data data = new Data();
        Schedule schedule = new Schedule(data);
        schedule.initialize();
        schedule.calculateFitnessAndPenalty();
    }

}
