package dk.karsten.ronge.simulation;

import dk.karsten.ronge.initialization.Configuration.BasicParameters;
import dk.karsten.ronge.initialization.Configuration.ParticlesDefinition;
import dk.karsten.ronge.serialization.BasicParametersJsonImpl;
import dk.karsten.ronge.visualisation.ImageMaker;
import org.apache.commons.io.FileUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.shade.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.*;

import static org.nd4j.linalg.ops.transforms.Transforms.pow;

/**
 * Created by kar on 23/01/2019.
 */
public class Simulation {
    final BasicParameters basicParameters;

    public Simulation(BasicParameters basicParameters) {
        this.basicParameters = basicParameters;
    }

    public void run_nbody(ParticlesDefinition particlesDefinition) {
        int timeSteps = basicParameters.time_steps;

        int index = 0;
        final File indexFile = new File("DATA/index.txt");
        if (indexFile.exists()) {
            try {
                final String indexString = FileUtils.readFileToString(indexFile, "UTF-8");
                index = Integer.parseInt(indexString);
                timeSteps -= index;
                System.out.println("Found existing index=" + index);
                final BasicParameters existingBasicParameters = new ObjectMapper().readValue(new File("DATA/basicparameters.txt"), BasicParametersJsonImpl.class);
                if (!existingBasicParameters.equals(basicParameters)) {
                    throw new RuntimeException("Trying finishing a simulation, but with different basic parameters. Will not do. Check DATA folder.");
                }
            } catch (IOException e) {
                throw new RuntimeException("Trying finishing a simulation, but with unreadable basic parameters. Check DATA folder.", e);
            }
        } else {
            try {
                Nd4j.write(new FileOutputStream("DATA/mass_.iar", false), particlesDefinition.getMass());
                Nd4j.write(new FileOutputStream("DATA/velocity_" + index + ".iar", false), particlesDefinition.getVelocity());
                Nd4j.write(new FileOutputStream("DATA/position_" + index + ".iar", false), particlesDefinition.getPosition());
                new ObjectMapper().writeValue(new File("DATA/basicparameters.txt"), basicParameters);
                FileUtils.write(indexFile, String.valueOf(index), "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ParticlesDefinition iteratedParticlesDefinition = getLatestSavedParticlesDefinition(index);
        if (iteratedParticlesDefinition == null) {
            iteratedParticlesDefinition = particlesDefinition;
        }
        final float aFloat = iteratedParticlesDefinition.getPosition().getFloat(1, 0);
        runIterations(timeSteps, index, indexFile, iteratedParticlesDefinition);
        final ParticlesDefinition latestSavedParticlesDefinition = getLatestSavedParticlesDefinition(200);
//        final ParticlesDefinition latestSavedParticlesDefinition = getLatestSavedParticlesDefinition(basicParameters.time_steps);
        try {
            final ImageMaker imageMaker = new ImageMaker(basicParameters, latestSavedParticlesDefinition.getPosition());
//            final IndexProvider indexProvider = new IndexProvider(basicParameters.time_steps);
//            final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
//            scheduledExecutorService.scheduleWithFixedDelay(() -> {
//                imageMaker.showPositions(getLatestSavedParticlesDefinition(indexProvider.next()).getPosition());
//            }, 1000, 200, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class IndexProvider {
        volatile int i = 0;
        final int max;

        IndexProvider(int max) {
            this.max = max;
        }

        int next() {
            i += 4;
            i = i >max ? 0 : i;
            return i;
        }
    }

    private void runIterations(int timeSteps, int index, File indexFile,
                               ParticlesDefinition iteratedParticlesDefinition) {
        while (timeSteps > 0) {
            index++;
            System.out.println(new Date() + " ----- Iteration no. " + (index) + " of " + basicParameters.time_steps + " -----");
            final INDArray newVelocities = update_velocities(iteratedParticlesDefinition);
            //Update positions, can just add velocities if assumed that one time step is one unit of time
            final INDArray newPositions = iteratedParticlesDefinition.getPosition().add(newVelocities);

            //TODO: apply boundary conditions

            //Write result to disk
            try {
                System.out.println(new Date() + " Start write velocities and positions to disk");
                Nd4j.write(new FileOutputStream("DATA/velocity_" + index + ".iar", false), newVelocities);
                Nd4j.write(new FileOutputStream("DATA/position_" + index + ".iar", false), newPositions);
                System.out.println(new Date() + " Done writing velocities and positions to disk.");
                FileUtils.write(indexFile, String.valueOf(index), "UTF-8");
            } catch (Throwable e) {
                throw new RuntimeException("An error happened in iteration no " + index, e);
            }
            iteratedParticlesDefinition = ParticlesDefinition.builder().mass(iteratedParticlesDefinition.getMass()).position(newPositions).velocity(newVelocities).build();

            timeSteps--;
        }
        System.out.println("Done with simulation of " + basicParameters.time_steps + " iterations.");
    }

    ParticlesDefinition getLatestSavedParticlesDefinition(int index) {
        if (index > -1) {
            try {
                final INDArray mass = Nd4j.read(new FileInputStream("DATA/mass_.iar"));
                final INDArray velocities = Nd4j.read(new FileInputStream("DATA/velocity_" + index + ".iar"));
                final INDArray positions = Nd4j.read(new FileInputStream("DATA/position_" + index + ".iar"));
                return ParticlesDefinition.builder().mass(mass).position(positions).velocity(velocities).build();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    INDArray update_velocities(ParticlesDefinition particlesDefinition) {
        final int numberOfChunks = basicParameters.chunks;
//        final ExecutorService executorService = Executors.newFixedThreadPool(numberOfChunks);
//        CountDownLatch countDownLatch = new CountDownLatch(numberOfChunks);
        System.out.println(new Date() + " Starting simulation with " + basicParameters.numTotalParticles() + " particles in " + numberOfChunks + " chunks");
//        final ConcurrentHashMap<Integer, INDArray> simulationResultMap = new ConcurrentHashMap<>();
        long start = System.currentTimeMillis();
        int j = 0;
//        for (int i = 0; i < numberOfChunks; i++) {
//            final int j = i;
//            executorService.submit(() -> {
//                System.out.println(new Date() + " Chunk no " + j + " starting with from-to = " + basicParameters.chunkFrom(j) + " - " + basicParameters.chunkTo(j));
                final INDArray newVelocity = update_velocities(particlesDefinition, basicParameters.chunkFrom(j), basicParameters.chunkTo(j));
//                simulationResultMap.put(j, newVelocity);
//                System.out.println(new Date() + " Chunk no " + j + " done");
//                countDownLatch.countDown();
//            });
//        }
//        try {
//            countDownLatch.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        System.out.println(new Date() + " Calculation Done. Time taken=" + (System.currentTimeMillis() - start) + "ms");
        //concat the results:
//        INDArray result = null;
//        for (int i = 0; i < numberOfChunks; i++) {
//            if (i == 0) {
//                result = simulationResultMap.get(i);
//            } else {
//                result = Nd4j.vstack(result, simulationResultMap.get(i));
//            }
//        }
//        System.out.println(new Date() + " Done. Time taken=" + (System.currentTimeMillis() - start) + "ms");
//        return result;
        return newVelocity;
    }


    INDArray update_velocities(ParticlesDefinition particlesDefinition, int from, int to) {
        INDArray newVelocity = Nd4j.create(to - from, 3);
        int index = 0;
        final INDArray position = particlesDefinition.getPosition();
        final INDArray mass = particlesDefinition.getMass();
        final INDArray velocity = particlesDefinition.getVelocity();
        for (int i = from; i < to; i++) {
            INDArray dx = position.getColumn(0).getScalar(i).addColumnVector(position.getColumn(0).neg());
//            INDArray dy = position.getColumn(1).getScalar(i).addColumnVector(position.getColumn(1).neg());
//            INDArray dz = position.getColumn(2).getScalar(i).addColumnVector(position.getColumn(2).neg());
//            INDArray dx = position.distance2(position);
            INDArray dy = position.getColumn(1).getScalar(i).addColumnVector(position.getColumn(1).neg());
            INDArray dz = position.getColumn(2).getScalar(i).addColumnVector(position.getColumn(2).neg());

            INDArray r2 = pow(dx, 2).add(pow(dy, 2).add(pow(dz, 2).add(Math.pow(basicParameters.epsilon, 2.0d))));
            INDArray coef = mass.mul(-basicParameters.G);
            INDArray ax = dx.mul(coef.transpose());
            INDArray ay = dy.mul(coef.transpose());
            INDArray az = dz.mul(coef.transpose());
            INDArray ax_scaled = ax.div(r2);
            INDArray ay_scaled = ay.div(r2);
            INDArray az_scaled = az.div(r2);
            final INDArray total_ax = ax_scaled.sum(0);
            final INDArray total_ay = ay_scaled.sum(0);
            final INDArray total_az = az_scaled.sum(0);
            final INDArray new_vel_x = velocity.getColumn(0).getScalar(i).add(total_ax);
            final INDArray new_vel_y = velocity.getColumn(1).getScalar(i).add(total_ay);
            final INDArray new_vel_z = velocity.getColumn(2).getScalar(i).add(total_az);
            newVelocity.put(index, 0, new_vel_x);
            newVelocity.put(index, 1, new_vel_y);
            newVelocity.put(index, 2, new_vel_z);
            index++;
        }
        return newVelocity;
    }
}
