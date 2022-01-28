package com.course_scheduling.ga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.course_scheduling.assets.Statistics;

/**
 *
 * @author artikandri
 */
public class Penalty {

    private int numbOfConflicts = 0;
    private int penalty = 0;
    private boolean isFitnessChanged = false;
    private double fitness = -1;

    private List<Class> classes;
    private Statistics statistics = new Statistics();

    public Penalty(Schedule schedule) {
        this.classes = schedule.getClasses();
        this.isFitnessChanged = true;

        this.calculateFitnessAndPenalty();
    }

    public void setClasses(List classes) {
        this.classes = classes;
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
        classes.forEach(cls -> {
            Instructor clsInstructor = cls.getInstructor();
            List<Integer> clsInstructorPreferences = clsInstructor.getPreferences();
            Timeslot xTimeslot = cls.getTimeslot();
            if (!clsInstructorPreferences.isEmpty()
                    && !clsInstructorPreferences.contains(xTimeslot.getId())) {
                penalty += 1;
            }
        });

    }

    // 1. Hard constraint: same instructor can't teach 2 different courses simultaneously
    // penalty += 9999
    // 2. Hard constraint: same room can't be scheduled for 2 diff courses simultaneously
    // penalty += 9999
    private void calculatePenaltyForHardConstraints() {
        classes.sort(Comparator.comparing(Class::getTimeslotId));
        classes.forEach(cls1 -> {
            classes.stream().filter(cls2
                    -> classes.indexOf(cls2) >= classes.indexOf(cls1)).forEach(cls2 -> {
                // same timeslot can only be assigned
                // to one group, one instructor,
                // one room, one course at one time
                if (cls1.getTimeslot() == cls2.getTimeslot()
                        && cls1.getGroupId() == cls2.getGroupId()) {

                    if (cls1.getCourseId() != cls2.getCourseId()) {
                        numbOfConflicts++;
                        penalty += 9999;
                    }

                }

                if (cls1.getTimeslot() == cls2.getTimeslot()
                        && cls1.getGroupId() != cls2.getGroupId()) {

                    if (cls1.getRoom() == cls2.getRoom()) {
                        numbOfConflicts++;
                        penalty += 9999;
                    }

                    if (cls1.getInstructor() == cls2.getInstructor()) {
                        numbOfConflicts++;
                        penalty += 9999;
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

    public List<Class> getConflictedClasses() {
        classes.forEach(cls1 -> {
            classes.stream().filter(y -> classes.indexOf(y) >= classes.indexOf(cls1)).forEach(cls2 -> {

                if (cls1.getTimeslot() == cls2.getTimeslot()
                        && cls1.getGroupId() == cls2.getGroupId()) {

                    if (cls1.getCourseId() != cls2.getCourseId()) {
                        cls1.setIsFlagged(true);
                    }

                }

                if (cls1.getTimeslot() == cls2.getTimeslot()
                        && cls1.getGroupId() != cls2.getGroupId()) {

                    if (cls1.getRoom() == cls2.getRoom()) {
                        cls1.setIsFlagged(true);
                    }

                    if (cls1.getInstructor() == cls2.getInstructor()) {
                        cls1.setIsFlagged(true);
                    }
                }

            });
        });

        List<Class> filteredClasses = classes.stream()
                .collect(Collectors.filtering(
                        x -> x.getIsFlagged() == true,
                        Collectors.toList()));

        return filteredClasses;
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

            List<Integer> numberOfCoursesEverydayPerGroup = new ArrayList<>(Arrays.asList());
            for (Map.Entry<String, List<Class>> groupInDay : groupInDays.entrySet()) {
                if (groupInDay.getValue().size() > 5) {
                    penalty += 99 * (groupInDay.getValue().size() - 5);
                }
                numberOfCoursesEverydayPerGroup.add(groupInDay.getValue().size());
            }

            if (statistics.getVariance(numberOfCoursesEverydayPerGroup) < 1) {
                penalty += statistics.getStandardDeviation(numberOfCoursesEverydayPerGroup);
            }

        }
    }

    public void setIsFitnessChanged(boolean isFitnessChanged) {
        this.isFitnessChanged = isFitnessChanged;
    }

    public double getFitness() {
        if (isFitnessChanged) {
            fitness = calculateFitnessAndPenalty();
            isFitnessChanged = false;
        }
        return fitness;
    }

    public int getPenalty() {
        if (isFitnessChanged) {
            fitness = calculateFitnessAndPenalty();
            isFitnessChanged = false;
        }
        return penalty;
    }

    public int getNumbOfConflicts() {
        return numbOfConflicts;
    }

}
