package com.course_scheduling.ga;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.collections.ListUtils;

public class GeneticAlgorithm {

    private Data data;

    public GeneticAlgorithm(Data data) {
        this.data = data;
    }

    // mutate population which is already crossovered
    public Population evolve(Population population) {
        return mutatePopulation(crossoverPopulation(population));
    }

    // compare two population, get the best one (has the best fitness), cross them over, and return the new pop
    Population crossoverPopulation(Population population) {
        Population crossoverPopulation = new Population(population.getSchedules().size(), data);
        IntStream.range(0, Scheduler.NUMB_OF_ELITE_SCHEDULES).forEach(x -> crossoverPopulation.getSchedules().set(x,
                population.getSchedules().get(x)));
        IntStream.range(Scheduler.NUMB_OF_ELITE_SCHEDULES, population.getSchedules().size()).forEach(x -> {
            if (Scheduler.CROSSOVER_RATE > Math.random()) {
                Schedule schedule1 = selectTournamentPopulation(population).sortByPenaltyAndNumbOfConflicts().getSchedules().get(0);
                Schedule schedule2 = selectTournamentPopulation(population).sortByPenaltyAndNumbOfConflicts().getSchedules().get(1);
                crossoverPopulation.getSchedules().set(x, crossoverSchedule(schedule1, schedule2));
            } else {
                crossoverPopulation.getSchedules().set(x, population.getSchedules().get(x));
            }
        });
        return crossoverPopulation;
    }

    Schedule crossoverSchedule(Schedule schedule1, Schedule schedule2) {
        Schedule crossoverSchedule = new Schedule(data).initialize();

        for (int i = 0; i < crossoverSchedule.getClasses().size(); i++) {
            if (Math.random() > 0.5) {
                if (i < schedule1.getClasses().size()) {
                    if (canScheduleBeExchanged(crossoverSchedule, schedule1, i)) {
                        crossoverSchedule.getClasses().set(i, schedule1.getClasses().get(i));
                    }
                } else {
                    if (schedule1.getClasses().size() > Scheduler.NUMB_OF_LARGE_SIZED_SCHEDULE_CLASSES) {
                        int randomIndex = (int) (schedule1.getClasses().size() * Math.random());
                        if (canScheduleBeExchanged(crossoverSchedule, schedule1, randomIndex)) {
                            crossoverSchedule.getClasses().set(randomIndex, schedule1.getClasses().get(randomIndex));
                        }
                    } else {
                        crossoverSchedule = exchangeOneRandomCourseInSchedule(crossoverSchedule, schedule1);
                    }
                }
            } else {
                if (i < schedule2.getClasses().size()) {
                    if (canScheduleBeExchanged(crossoverSchedule, schedule2, i)) {
                        crossoverSchedule.getClasses().set(i, schedule2.getClasses().get(i));
                    }
                } else {
                    if (schedule2.getClasses().size() > 250) {
                        int randomIndex = (int) (schedule2.getClasses().size() * Math.random());
                        if (canScheduleBeExchanged(crossoverSchedule, schedule2, randomIndex)) {
                            crossoverSchedule.getClasses().set(randomIndex, schedule2.getClasses().get(randomIndex));
                        }
                    } else {
                        crossoverSchedule = exchangeOneRandomCourseInSchedule(crossoverSchedule, schedule2);
                    }
                }
            }
        }
        return crossoverSchedule;
    }

    Population mutatePopulation(Population population) {
        Population mutatePopulation = new Population(population.getSchedules().size(), data);
        ArrayList<Schedule> schedules = mutatePopulation.getSchedules();
        IntStream.range(0, Scheduler.NUMB_OF_ELITE_SCHEDULES).forEach(x -> schedules.set(x, population.getSchedules().get(x)));
        IntStream.range(Scheduler.NUMB_OF_ELITE_SCHEDULES, population.getSchedules().size()).forEach(x -> {
            schedules.set(x, mutateSchedule(population.getSchedules().get(x)));
        });
        return mutatePopulation;
    }

    private Schedule exchangeOneRandomCourseInSchedule(Schedule schedule1, Schedule schedule2) {
        int rIndex = (int) (schedule1.getClasses().size() * Math.random());
        Class rClass = schedule1.getClasses().get(rIndex);
        List<Class> possibleReps = schedule2.getClasses()
                .stream()
                .filter(x -> x.getCourseId() == rClass.getCourseId())
                .collect(Collectors.toList());
        ArrayList<Class> filteredClasses = new ArrayList<Class>(schedule1.getClasses().stream()
                .collect(Collectors.filtering(x -> x.getCourseId() != rClass.getCourseId(),
                        Collectors.toList())));
        ArrayList<Class> newClasses = new ArrayList<Class>(ListUtils.union(filteredClasses, possibleReps));
        schedule1.setClasses(newClasses);

        return schedule1;
    }

    Schedule mutateSchedule(Schedule mutateSchedule) {
        Schedule schedule = new Schedule(data).initialize();
        // randomly introducing bad genes
        // 1st: mutate all classes related to a random course

        if (mutateSchedule.getClasses().size() < Scheduler.NUMB_OF_LARGE_SIZED_SCHEDULE_CLASSES) {
            mutateSchedule = exchangeOneRandomCourseInSchedule(mutateSchedule, schedule);
        } else {
            for (int i = 0; i < mutateSchedule.getClasses().size(); i++) {
                if (Scheduler.MUTATION_RATE > Math.random()) {
                    if (i < schedule.getClasses().size()) {
                        if (canScheduleBeExchanged(mutateSchedule, schedule, i)) {
                            mutateSchedule.getClasses().set(i, schedule.getClasses().get(i));
                        }
                    } else {
                        int randomIndex = (int) (schedule.getClasses().size() * Math.random());
                        if (canScheduleBeExchanged(mutateSchedule, schedule, randomIndex)) {
                            mutateSchedule.getClasses().set(randomIndex, schedule.getClasses().get(randomIndex));
                        }
                    }
                }
            }
        }

        return mutateSchedule;
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

    Population selectTournamentPopulation(Population population) {
        Population tournamentPopulation = new Population(Scheduler.TOURNAMENT_SELECTION_SIZE, data);
        IntStream.range(0, Scheduler.TOURNAMENT_SELECTION_SIZE).forEach(x -> {
            tournamentPopulation.getSchedules().set(x,
                    population.getSchedules().get((int) (Math.random() * population.getSchedules().size())));
        });
        return tournamentPopulation;
    }
}
