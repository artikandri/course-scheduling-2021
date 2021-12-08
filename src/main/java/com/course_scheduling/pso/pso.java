package com.course_scheduling.pso;
// 08/12/2021: Added this so that the file structure comply with the rest of the code, feel free to delete this comment later

/**
 *
 * @author Teddy Ferdinan
 */
import java.util.Arrays;
import java.util.Random;
import java.lang.Math;

public class pso {

    static class Configuration {
        int countParticles;
        int currentIteration;
        int maxIteration;
        double[] globalBestPosition;
        double globalBestFV;
        double[][] positions;
        double[][] velocities;

        Configuration(int countParticles, int currentIteration, int maxIteration, double[] globalBestPosition, double globalBestFV, double[][] positions, double[][] velocities) {
            this.countParticles = countParticles;
            this.currentIteration = currentIteration;
            this.maxIteration = maxIteration;
            this.globalBestPosition = globalBestPosition;
            this.globalBestFV = globalBestFV;
            this.positions = positions;
            this.velocities = velocities;
        }

        void show() {
            System.out.printf("--------CONFIGURATION---------\n");
            System.out.printf("Number of Particles       : %d\n", countParticles);
            System.out.printf("Maximum Iteration         : %d\n", maxIteration);
            System.out.printf("Global Best Position      : %s\n", Arrays.toString(globalBestPosition));
            System.out.printf("Global Best Fitness Value : %.10f\n", globalBestFV);
            for(int i=0; i < countParticles; i++) {
                System.out.printf("Particle %d: %s , %s\n", i, Arrays.toString(positions[i]), Arrays.toString(velocities[i]));
            }
        }
    }

    private static Configuration psoInitialize(int countParticles, int maxIteration) {
        int currentIteration = 0;
        double[] globalBestPosition = {};
        double globalBestFV = 0;
        double[][] positions = new double[countParticles][2];//positions at x and y axes
        double[][] velocities = new double[countParticles][2];//velocities at x and y axes

        for(int i=0; i < countParticles; i++) {
            for(int j=0; j < positions[i].length; j++) {
                positions[i][j] = new Random().nextDouble();
            }
        }

        for(int i=0; i < countParticles; i++) {
            for(int j=0; j < velocities[i].length; j++) {
                velocities[i][j] = new Random().nextDouble();
            }
        }

        return new Configuration(countParticles, currentIteration, maxIteration, globalBestPosition, globalBestFV, positions, velocities);
    }

    private static double psoFitnessFunction(double[] position) {
        double fitnessValue;

        //adjust fitness function here !!!
        //placeholder : a customization of the McCormick function
        //fitnessValue = Math.sin(position[0] + position[1]) + (position[0] - position[1]) * (position[0] - position[1]) + 1.0 + 2.5 * position[1] - 1.5 * position[0];
        fitnessValue = Math.pow(position[0]+2, 2) + Math.pow(position[1]-2, 2) + 2;

        return fitnessValue;
    }

    private static double[] psoFindGlobalBest(double[][] positions, double[] globalBestPosition) {
        if(globalBestPosition.length == 0) {
            globalBestPosition = positions[0];
        }
        for(int i=0; i < positions.length; i++) {
            if(psoFitnessFunction(positions[i]) < psoFitnessFunction(globalBestPosition)) {
                globalBestPosition = positions[i];
            }
        }

        return globalBestPosition;
    }

    private static double[][] psoUpdatePositions(double[][] positions, double[][] velocities, double[] globalBestPosition) {
        for(int i=0; i < positions.length; i++) {
            if(positions[i] != globalBestPosition) {
                for(int j=0; j < positions[i].length; j++) {
                    positions[i][j] += velocities[i][j];
                }
            }

            //adjust local search method here !!!
        }

        return positions;
    }

    private static double[][] psoUpdateVelocities(double[][] positions, double[][] velocities, double[] globalBestPosition) {
        Random randomizer = new Random();
        double coefficientAcceleration = randomizer.nextDouble();

        //the particles generally move towards the global best position
        for(int i=0; i < velocities.length; i++) {
            //check if the current particle is at the global best position
            if(positions[i] != globalBestPosition) {
                //it is not the global best, update its velocity
                for(int j=0; j < velocities[i].length; j++) {
                    if(positions[i][j] < globalBestPosition[j]) {
                        velocities[i][j] += coefficientAcceleration * (globalBestPosition[j] - positions[i][j]);
                    } else {
                        velocities[i][j] -= coefficientAcceleration * (positions[i][j] - globalBestPosition[j]);
                    }
                }
            } else {
                //it is the global best, it stays in its place
                for(int j=0; j < velocities[i].length; j++) {
                    velocities[i][j] = 0;
                }
            }
        }

        return velocities;
    }

    private static Configuration psoIterate(Configuration x, String executionMode) {
        for(int epoch=0; epoch < x.maxIteration; epoch++) {
            //find current global best position
            x.globalBestPosition = psoFindGlobalBest(x.positions, x.globalBestPosition);
            //update current global best fitness value
            x.globalBestFV = psoFitnessFunction(x.globalBestPosition);
            //for each particle, update its velocity
            x.velocities = psoUpdateVelocities(x.positions, x.velocities, x.globalBestPosition);
            //for each particle, update its position
            x.positions = psoUpdatePositions(x.positions, x.velocities, x.globalBestPosition);
            if(executionMode == "detailed") {
                System.out.printf("---------ITERATION %d---------\n", epoch);
                System.out.printf("Number of Particles       : %d\n", x.countParticles);
                System.out.printf("Maximum Iteration         : %d\n", x.maxIteration);
                System.out.printf("Global Best Position      : %s\n", Arrays.toString(x.globalBestPosition));
                System.out.printf("Global Best Fitness Value : %.10f\n", x.globalBestFV);
                for(int i=0; i < x.countParticles; i++) {
                    System.out.printf("Particle %d: %s , %s\n", i, Arrays.toString(x.positions[i]), Arrays.toString(x.velocities[i]));
                }
            }
        }

        return x;
    }

    /**
     * @param args the command line arguments
     */

    public static void main(String args[]) {
        // TODO code application logic here
        Configuration x = psoInitialize(10, 1000);
        x.show();
        Configuration y = psoIterate(x, "detailed");
        y.show();
    }

}
