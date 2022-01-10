package com.course_scheduling.ga;

import java.util.*;
import java.util.stream.IntStream;

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
                Schedule schedule1 = selectTournamentPopulation(population).sortByFitness().getSchedules().get(0);
                Schedule schedule2 = selectTournamentPopulation(population).sortByFitness().getSchedules().get(1);
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
                    if (canScheduleBeChanged(crossoverSchedule, schedule1, i)) {
                        crossoverSchedule.getClasses().set(i, schedule1.getClasses().get(i));
                    }
                } else {
                    int randomIndex = (int) (schedule1.getClasses().size() * Math.random());
                    if (canScheduleBeChanged(crossoverSchedule, schedule1, randomIndex)) {
                        crossoverSchedule.getClasses().set(randomIndex, schedule1.getClasses().get(randomIndex));
                    }
                }
            } else {
                if (i < schedule2.getClasses().size()) {
                    if (canScheduleBeChanged(crossoverSchedule, schedule2, i)) {
                        crossoverSchedule.getClasses().set(i, schedule2.getClasses().get(i));
                    }
                } else {
                    int randomIndex = (int) (schedule2.getClasses().size() * Math.random());
                    if (canScheduleBeChanged(crossoverSchedule, schedule2, randomIndex)) {
                        crossoverSchedule.getClasses().set(randomIndex, schedule2.getClasses().get(randomIndex));
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

    Schedule mutateSchedule(Schedule mutateSchedule) {
        // randomly generated schedules may have differing length due to the random nature of timeslot assignment
        Schedule schedule = new Schedule(data).initialize();
        for (int i = 0; i < mutateSchedule.getClasses().size(); i++) {
            if (Scheduler.MUTATION_RATE > Math.random()) {
                if (i < schedule.getClasses().size()) {
                    if (canScheduleBeChanged(mutateSchedule, schedule, i)) {
                        mutateSchedule.getClasses().set(i, schedule.getClasses().get(i));
                    }
                } else {
                    int randomIndex = (int) (schedule.getClasses().size() * Math.random());
                    if (canScheduleBeChanged(mutateSchedule, schedule, randomIndex)) {
                        mutateSchedule.getClasses().set(randomIndex, schedule.getClasses().get(randomIndex));
                    }
                }
            }
        }
        return mutateSchedule;
    }

    private boolean canScheduleBeChanged(Schedule schedule1, Schedule schedule2, int index) {
        boolean canBeChanged = false;

        Class class1 = schedule1.getClasses().get(index);
        Class class2 = schedule2.getClasses().get(index);

        boolean hasTheSameCourse = class1.getCourseId() == class2.getCourseId();
        boolean hasTheSameDuration = class1.getTimeslot().getDuration() == class2.getTimeslot().getDuration();
        boolean hasClassExisted = schedule1.getClasses()
                .stream()
                .filter(x -> x.getCourseId() == class2.getCourseId() 
                        && x.getTimeslotId() == class2.getTimeslotId())
                .findFirst()
                .isPresent();
        
        canBeChanged = hasTheSameCourse && hasTheSameDuration && !hasClassExisted;

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
