package com.course_scheduling.ga;

import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.collections.ListUtils;

/**
 *
 * @author @artikandri
 */
public class GeneticAlgorithm {

    private Data data;

    public GeneticAlgorithm(Data data) {
        this.data = data;
    }

    public Population regenerate(Population population) {
        return mutatePopulation(crossoverPopulation(population));
    }

    Population crossoverPopulation(Population population) {
        Population populationToCrossoverWith = new Population(population.getSchedules().size(), data);

        // get top schedules
        for (int i = 0; i < Scheduler.NUMB_OF_TOP_SCHEDULES; i++) {
            populationToCrossoverWith.getSchedules().set(i,
                    population.getSchedules().get(i));
        }

        // crossover the rests
        for (int i = Scheduler.NUMB_OF_TOP_SCHEDULES; i < population.getSchedules().size(); i++) {
            if (Scheduler.CROSSOVER_RATE > Math.random()) {
                Schedule bestSchedule = competePopulation(population).sortByPenaltyAndNumbOfConflicts().getSchedules().get(0);
                Schedule secondBestSchedule = competePopulation(population).sortByPenaltyAndNumbOfConflicts().getSchedules().get(1);
                populationToCrossoverWith.getSchedules().set(i, crossoverSchedule(bestSchedule, secondBestSchedule));
            } else {
                populationToCrossoverWith.getSchedules().set(i, population.getSchedules().get(i));
            }
        }

        return populationToCrossoverWith;
    }

    Schedule crossoverSchedule(Schedule schedule1, Schedule schedule2) {
        Schedule scheduleToCrossoverWith = new Schedule(data).initialize();

        for (int i = 0; i < scheduleToCrossoverWith.getClasses().size(); i++) {
            if (Math.random() > 0.5) {
                if (i < schedule1.getClasses().size()) {
                    if (canScheduleBeExchanged(scheduleToCrossoverWith, schedule1, i)) {
                        scheduleToCrossoverWith.getClasses().set(i, schedule1.getClasses().get(i));
                    }
                } else {
                    if (schedule1.getClasses().size() > Scheduler.NUMB_OF_LARGE_SIZED_SCHEDULE_CLASSES) {
                        int randomIndex = (int) (schedule1.getClasses().size() * Math.random());
                        if (canScheduleBeExchanged(scheduleToCrossoverWith, schedule1, randomIndex)) {
                            scheduleToCrossoverWith.getClasses().set(randomIndex, schedule1.getClasses().get(randomIndex));
                        }
                    } else {
                        scheduleToCrossoverWith = exchangeOneRandomCourseInSchedule(scheduleToCrossoverWith, schedule1);
                    }
                }
            } else {
                if (i < schedule2.getClasses().size()) {
                    if (canScheduleBeExchanged(scheduleToCrossoverWith, schedule2, i)) {
                        scheduleToCrossoverWith.getClasses().set(i, schedule2.getClasses().get(i));
                    }
                } else {
                    if (schedule2.getClasses().size() > Scheduler.NUMB_OF_LARGE_SIZED_SCHEDULE_CLASSES) {
                        int randomIndex = (int) (schedule2.getClasses().size() * Math.random());
                        if (canScheduleBeExchanged(scheduleToCrossoverWith, schedule2, randomIndex)) {
                            scheduleToCrossoverWith.getClasses().set(randomIndex, schedule2.getClasses().get(randomIndex));
                        }
                    } else {
                        scheduleToCrossoverWith = exchangeOneRandomCourseInSchedule(scheduleToCrossoverWith, schedule2);
                    }
                }
            }
        }
        return scheduleToCrossoverWith;
    }

    Population mutatePopulation(Population population) {
        Population populationToMutateWith = new Population(population.getSchedules().size(), data);
        ArrayList<Schedule> schedules = populationToMutateWith.getSchedules();

        for (int i = 0; i < Scheduler.NUMB_OF_TOP_SCHEDULES; i++) {
            schedules.set(i, population.getSchedules().get(i));
        }

        for (int i = Scheduler.NUMB_OF_TOP_SCHEDULES; i < population.getSchedules().size(); i++) {
            schedules.set(i, mutateSchedule(population.getSchedules().get(i)));
        }

        return populationToMutateWith;
    }

    private Schedule exchangeOneRandomCourseInSchedule(Schedule schedule1, Schedule schedule2) {
        int rIndex = (int) (schedule1.getClasses().size() * Math.random());
        Class rClass = schedule1.getClasses().get(rIndex);
        List<Class> possibleReplacements = schedule2.getClasses()
                .stream()
                .filter(x -> x.getCourseId() == rClass.getCourseId())
                .collect(Collectors.toList());
        ArrayList<Class> filteredClasses = new ArrayList<Class>(schedule1.getClasses().stream()
                .collect(Collectors.filtering(x -> x.getCourseId() != rClass.getCourseId(),
                        Collectors.toList())));
        ArrayList<Class> newClasses = new ArrayList<Class>(ListUtils.union(filteredClasses, possibleReplacements));
        schedule1.setClasses(newClasses);

        return schedule1;
    }

    Schedule mutateSchedule(Schedule schedule) {
        Schedule scheduleToMutateWith = new Schedule(data).initialize();

        if (schedule.getClasses().size() < Scheduler.NUMB_OF_LARGE_SIZED_SCHEDULE_CLASSES) {
            schedule = exchangeOneRandomCourseInSchedule(schedule, scheduleToMutateWith);
        } else {
            for (int i = 0; i < schedule.getClasses().size(); i++) {
                if (Scheduler.MUTATION_RATE > Math.random()) {
                    if (i < scheduleToMutateWith.getClasses().size()) {
                        if (canScheduleBeExchanged(schedule, scheduleToMutateWith, i)) {
                            schedule.getClasses().set(i, scheduleToMutateWith.getClasses().get(i));
                        }
                    } else {
                        int randomIndex = (int) (scheduleToMutateWith.getClasses().size() * Math.random());
                        if (canScheduleBeExchanged(schedule, scheduleToMutateWith, randomIndex)) {
                            schedule.getClasses().set(randomIndex, scheduleToMutateWith.getClasses().get(randomIndex));
                        }
                    }
                }
            }
        }

        return schedule;
    }

    private boolean isClassIdeal(Schedule schedule, int index) {
        Class class1 = schedule.getClasses().get(index);
        List<Class> similarClasses = schedule.getClasses()
                .stream()
                .filter(x -> x.getCourseId() == class1.getCourseId()
                && x.getTimeslotDayId() == class1.getTimeslotDayId()
                && x.getRoom().getId() == class1.getRoom().getId())
                .collect(Collectors.toList());

        similarClasses.sort(Comparator.comparing(Class::getCourseId).thenComparing(Class::getTimeslotId));
        int timeslotIdDifference = -1;
        for (Class similarClass : similarClasses) {
            if (timeslotIdDifference == -1) {
                timeslotIdDifference = similarClass.getTimeslotId();
            } else {
                timeslotIdDifference -= similarClass.getTimeslotId();
            }
        }

        boolean isAlreadyConsecutive = Math.abs(timeslotIdDifference) == similarClasses.size() - 1;
        boolean isClassAlreadyIdeal = similarClasses.size() > 1 && isAlreadyConsecutive;

        return isClassAlreadyIdeal;
    }

    private boolean canScheduleBeExchanged(Schedule schedule1, Schedule schedule2, int index) {
        boolean canBeChanged = false;
        // randomly encourage exchange to prevent schedules suffering from elitism
        boolean isExchangeEncouraged = Math.random() * 100 <= 50;

        Class class1 = schedule1.getClasses().get(index);
        Class class2 = schedule2.getClasses().get(index);

        boolean hasTheSameCourse = class1.getCourseId() == class2.getCourseId();
        boolean hasTheSameDuration = class1.getTimeslot().getDuration() == class2.getTimeslot().getDuration();
        boolean hasGroupClassWithTheSameTimeslotExisted = schedule1.getClasses()
                .stream()
                .filter(x -> x.getCourseId() == class2.getCourseId()
                || x.getGroupId() == class2.getGroupId()
                && x.getTimeslotId() == class2.getTimeslotId())
                .findFirst()
                .isPresent();

        // improve performance by reducing number of checking on large schedules
        // java stream method performs very slowly
        boolean isClassAlreadyIdeal = schedule1.getClasses().size() > Scheduler.NUMB_OF_LARGE_SIZED_SCHEDULE_CLASSES
                ? true : isClassIdeal(schedule1, index);

        if (hasTheSameCourse && hasTheSameDuration) {
            canBeChanged = isExchangeEncouraged || !isClassAlreadyIdeal
                    && !hasGroupClassWithTheSameTimeslotExisted;
        }

        return canBeChanged;
    }

    Population competePopulation(Population population) {
        Population populationToCompeteWith = new Population(Scheduler.NUMB_OF_POPULATION_COMPETITOR, data);
        for (int i = 0; i < Scheduler.NUMB_OF_POPULATION_COMPETITOR; i++) {
            int randomScheduleIndex = (int) (Math.random() * population.getSchedules().size());
            populationToCompeteWith.getSchedules().set(i,
                    population.getSchedules().get(randomScheduleIndex));
        }
        return populationToCompeteWith;
    }
}
