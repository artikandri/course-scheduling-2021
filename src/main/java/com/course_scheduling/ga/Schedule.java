package com.course_scheduling.ga;

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

                if (suitablePreferredTimeslots.isEmpty()) {
                    // assign random timeslot with possible duration
                    Timeslot randomTimeslot = getSuitableTimeslot(course, previouslyChosenTimeslotIds, prevCourseId, courseDayId, duration.toString(), false);

                    if (previouslyChosenTimeslotIds.isEmpty() || !previouslyChosenTimeslotIds
                            .contains((int) randomTimeslot.getId())) {
                        previouslyChosenTimeslotIds.add(randomTimeslot.getId());
                        newClass.setTimeslot(randomTimeslot);
                        courseDayId = randomTimeslot.getDayId();

                        classes.add(newClass);
                    }

                } else {
                    // assign random timeslot from preferred timeslots if possible
                    // if not, assign from timeslot pool
                    Timeslot randomTimeslot = getSuitableTimeslot(course, previouslyChosenTimeslotIds, prevCourseId, courseDayId, duration.toString(), true);
                    if (previouslyChosenTimeslotIds.isEmpty() || !previouslyChosenTimeslotIds
                            .contains((int) randomTimeslot.getId())) {
                        previouslyChosenTimeslotIds.add(randomTimeslot.getId());
                        newClass.setTimeslot(randomTimeslot);
                        courseDayId = randomTimeslot.getDayId();
                        classes.add(newClass);
                    }

                }
                prevCourseId = course.getId();
            }
            previouslyChosenTimeslotIds.clear();
        });
        return this;
    }

    private Timeslot getSuitableTimeslot(Course course, List previouslyChosenTimeslotIds, int prevCourseId, int courseDayId, String duration, boolean isPreferenceChecked) {
        Map possibleTimeslotMap = course.getPossibleTimeslotMap();
        // consider timeslot
        List<Integer> preferredTimeslots
                = data.findInstructorById(course.getInstructorId()).getPreferences();
        List<Timeslot> preferredTimeslotObjects = data.getTimeslots().stream()
                .filter(timeslot -> preferredTimeslots.contains(timeslot.getId()))
                .collect(Collectors.toList());
        List<Integer> possibleTimeslots
                = (List) possibleTimeslotMap.get(duration.toString());
        List<Integer> suitablePreferredTimeslots = preferredTimeslots.stream()
                .collect(Collectors.filtering(
                        timeslotId -> possibleTimeslots.contains(timeslotId),
                        Collectors.toList()));
        List<Timeslot> suitablePreferredTimeslotObjects = data.getTimeslots().stream()
                .collect(Collectors.filtering(
                        timeslot -> suitablePreferredTimeslots.contains(timeslot.getId()),
                        Collectors.toList()));
        List<Timeslot> suitableTimeslotObjects = data.getTimeslots().stream()
                .collect(Collectors.filtering(
                        timeslot -> possibleTimeslots.contains(timeslot.getId()),
                        Collectors.toList()));

        // consider duration
        List possibleDurations = course.getPossibleDurations();
        double randomPossibleDurationIndex = possibleDurations.size() * Math.random();
        List<Double> randomDurations
                = (List) possibleDurations.get((int) randomPossibleDurationIndex);

        // create random timeslot
        double randomTimeslotIndex = suitableTimeslotObjects.size() * Math.random();
        Timeslot randomTimeslot
                = suitableTimeslotObjects.get((int) randomTimeslotIndex);

        if (suitablePreferredTimeslots.contains(randomTimeslot.getId()) || !isPreferenceChecked) {
            if (course.getId() == prevCourseId) {
                List<Timeslot> suitableTimeslotObjectsOnTheDay = suitableTimeslotObjects.stream()
                        .collect(Collectors.filtering(
                                timeslot -> timeslot.getDayId() == courseDayId,
                                Collectors.toList())
                        );

                if (suitableTimeslotObjectsOnTheDay != null && !suitableTimeslotObjectsOnTheDay.isEmpty()) {
                    randomTimeslotIndex = (int) (suitableTimeslotObjectsOnTheDay.size() * Math.random());
                    randomTimeslot = suitableTimeslotObjectsOnTheDay.get((int) randomTimeslotIndex);

                    // if possible, schedule classes with the same course id consecutively
                    if (course.getWeeklyHours() == 2.0 || course.getWeeklyHours() == 3.0) {
                        if (!previouslyChosenTimeslotIds.isEmpty()) {
                            int lastChosenTimeslotId = Integer.parseInt(previouslyChosenTimeslotIds.get(previouslyChosenTimeslotIds.size() - 1).toString());
                            List<Timeslot> nextSuitableTimeslotObjectsOnTheDay = suitableTimeslotObjects.stream()
                                    .collect(Collectors.filtering(
                                            timeslot -> timeslot.getId() == lastChosenTimeslotId + 1,
                                            Collectors.toList())
                                    );

                            if (nextSuitableTimeslotObjectsOnTheDay != null && !nextSuitableTimeslotObjectsOnTheDay.isEmpty()) {
                                randomTimeslotIndex = (int) (nextSuitableTimeslotObjectsOnTheDay.size() * Math.random());
                                randomTimeslot = nextSuitableTimeslotObjectsOnTheDay.get((int) randomTimeslotIndex);
                            }

                        }

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
        calculateFitnessAndPenalty();
        return penalty;
    }

    private double calculateFitnessAndPenalty() {
        numbOfConflicts = 0;
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
        classes.forEach(x -> {
            classes.stream().filter(y -> classes.indexOf(y) >= classes.indexOf(x)).forEach(y -> {
                if (x.getTimeslot() == y.getTimeslot() && x.getId() != y.getId()) {
                    if (x.getInstructor() == y.getInstructor()) {
                        numbOfConflicts++;
                        penalty += 99;
                    }

                    if (x.getRoom() == y.getRoom()) {
                        numbOfConflicts++;
                        penalty += 99;
                    }
                }
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

        Map<String, List<Class>> classInCourses = classes.stream()
                .collect(Collectors.groupingBy(p -> String.valueOf(p.getCourseId())));
        for (Map.Entry<String, List<Class>> classInCourse : classInCourses.entrySet()) {
            Class randomClass = classInCourse.getValue().get(0);
            Course course = randomClass.getCourse();

            if (classInCourse.getValue().size() == 2) {
                for (int i = 0; i < 2; i++) {
                    Class prevClass = classInCourse.getValue().get(i);
                    if (i + 1 < 2) {
                        Class nextClass = classInCourse.getValue().get(i + 1);
                        if (nextClass.getTimeslot().getId() != prevClass.getTimeslot().getId()
                                + 1) {
                            penalty += 2;
                        }
                    }
                }
            }

            if (classInCourse.getValue().size() == 3) {
                for (int i = 0; i < 3; i++) {
                    Class prevClass = classInCourse.getValue().get(i);
                    if (i + 1 < 3) {
                        Class nextClass = classInCourse.getValue().get(i + 1);
                        if (nextClass.getTimeslot().getId() != prevClass.getTimeslot().getId()
                                + 1) {
                            penalty += 2;
                        }
                    }

                    // average of classes each day is 7
                    // the maximum distance is 7
                    if (i == 1) {
                        Class nextClass = classInCourse.getValue().get(i + 1);
                        if (nextClass.getTimeslot().getId() > prevClass.getTimeslot().getId() + 7) {
                            penalty += 2;
                        }
                    }
                }
            }

            // ideally courses should be splitted into 2 days
            if (classInCourse.getValue().size() == 4) {
                Map<String, List<Class>> classInCoursesInDays = classInCourse.getValue().stream()
                        .collect(Collectors.groupingBy(p -> String.valueOf(p.getTimeslotDayId())));
                for (Map.Entry<String, List<Class>> classInCoursesInDay : classInCoursesInDays
                        .entrySet()) {
                    if (classInCoursesInDay.getValue().size() != 2) {
                        penalty += 2;
                    }
                }

                // check consecutive classes for every 2 classes
                for (int i = 0; i < 4; i++) {
                    Class prevClass = classInCourse.getValue().get(i);
                    if (i == 0 || i == 2) {
                        Class nextClass = classInCourse.getValue().get(i + 1);
                        if (nextClass.getTimeslot().getId() != prevClass.getTimeslot().getId()
                                + 1) {
                            penalty += 2;
                        }
                    }

                }
            }
        }
    }

    /*
     * calculates penalty for imbalanced lessons on each day param: none return: none
     */
    private void calculatePenaltyForImbalancedLessons() {
        // 5. Soft constraint: number of daily lessons for students should be evenly balanced
        // group the schedules by group id and check the number of lessons on each day
        // penalty += 1
        Map<String, List<Class>> classInGroups = classes.stream()
                .collect(Collectors.groupingBy(p -> String.valueOf(p.getGroupId())));
        for (Map.Entry<String, List<Class>> classInGroup : classInGroups.entrySet()) {
            Map<String, List<Class>> groupInDays = classInGroup.getValue().stream()
                    .collect(Collectors.groupingBy(p -> String.valueOf(p.getTimeslotDayId())));

            for (Map.Entry<String, List<Class>> groupInDay : groupInDays.entrySet()) {
                if (groupInDay.getValue().size() >= 5) {
                    penalty += 1;
                }
            }
        }

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
