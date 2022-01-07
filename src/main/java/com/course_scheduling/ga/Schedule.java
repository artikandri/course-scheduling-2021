
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
            List<Integer> preferredTimeslots =
                    data.findInstructorById(course.getInstructorId()).getPreferences();
            List<Timeslot> preferredTimeslotObjects = data.getTimeslots().stream()
                    .filter(timeslot -> preferredTimeslots.contains(timeslot.getId()))
                    .collect(Collectors.toList());

            // consider duration
            List possibleDurations = course.getPossibleDurations();
            double randomPossibleDurationId = possibleDurations.size() * Math.random();
            List<Double> randomDurations =
                    (List) possibleDurations.get((int) randomPossibleDurationId);

            // match duration with timeslot
            // add classes by considering the duration
            // > 1 hour means multiple classes
            List<Integer> previouslyChosenTimeslotIds = new ArrayList(Arrays.asList());
            Map possibleTimeslotMap = course.getPossibleTimeslotMap();

            for (Double duration : randomDurations) {
                Class newClass = new Class(classNumb++, course);
                newClass.setRoom(
                        data.getRooms().get((int) (data.getRooms().size() * Math.random())));
                newClass.setInstructor(data.findInstructorById(course.getInstructorId()));
                newClass.setGroup(data.findGroupById(course.getGroupId()));

                List<Integer> possibleTimeslots =
                        (List) possibleTimeslotMap.get(duration.toString());
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

                if (suitablePreferredTimeslots.isEmpty()) {
                    // assign random timeslot with possible duration
                    boolean isTimeslotSet = false;
                    do {
                        double randomTimeslotId = suitableTimeslotObjects.size() * Math.random();
                        Timeslot randomTimeslot =
                                suitableTimeslotObjects.get((int) randomTimeslotId);
                        if (previouslyChosenTimeslotIds.isEmpty() || !previouslyChosenTimeslotIds
                                .contains((int) randomTimeslot.getId())) {
                            previouslyChosenTimeslotIds.add(randomTimeslot.getId());
                            newClass.setTimeslot(randomTimeslot);
                            classes.add(newClass);
                            isTimeslotSet = true;
                        }

                    } while (!isTimeslotSet);
                } else {
                    // assign random timeslot from preferred timeslots if possible
                    // if not, assign from timeslot pool
                    boolean isTimeslotSet = false;
                    do {
                        Timeslot timeslotObject = suitableTimeslotObjects
                                .get((int) (suitableTimeslotObjects.size() * Math.random()));
                        if (suitablePreferredTimeslots.contains(timeslotObject.getId())) {
                            if (previouslyChosenTimeslotIds.isEmpty()
                                    || !previouslyChosenTimeslotIds
                                            .contains((int) timeslotObject.getId())) {
                                previouslyChosenTimeslotIds.add(timeslotObject.getId());
                                newClass.setTimeslot(timeslotObject);

                                classes.add(newClass);
                                isTimeslotSet = true;
                            }
                        }

                        if (!isTimeslotSet) {
                            // do something if isTimeslotSet is still false
                            double randomTimeslotId =
                                    suitableTimeslotObjects.size() * Math.random();
                            Timeslot randomTimeslot =
                                    suitableTimeslotObjects.get((int) randomTimeslotId);
                            if (previouslyChosenTimeslotIds.isEmpty()
                                    || !previouslyChosenTimeslotIds
                                            .contains((int) randomTimeslot.getId())) {
                                previouslyChosenTimeslotIds.add(randomTimeslot.getId());
                                newClass.setTimeslot(randomTimeslot);
                                classes.add(newClass);
                                isTimeslotSet = true;
                            }
                        }
                    } while (!isTimeslotSet);
                }
            }
            previouslyChosenTimeslotIds.clear();
        });

        return this;
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
