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
    final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

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

        runIterations(timeSteps, index, indexFile, iteratedParticlesDefinition);
        executorService.close();
        final ParticlesDefinition latestSavedParticlesDefinition = getLatestSavedParticlesDefinition(basicParameters.time_steps);
        try {
            final ImageMaker imageMaker = new ImageMaker(basicParameters, latestSavedParticlesDefinition.getPosition());

            final IndexProvider indexProvider = new IndexProvider(basicParameters.time_steps);
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
            scheduledExecutorService.scheduleWithFixedDelay(() ->
                    imageMaker.showPositions(
                            getLatestSavedParticlesDefinition(indexProvider.next()).getPosition()
                    ), 1000, 500, TimeUnit.MILLISECONDS
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ParticlesDefinition getLatestSavedParticlesDefinition(int index) {
        if (index > -1) {
            try {
                System.out.println("Position for index=" + index);
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

    static class IndexProvider {
        volatile int i = 0;
        final int max;

        IndexProvider(int max) {
            this.max = max;
        }

        int next() {
            i += 1;
            i = i > max ? 0 : i;
            return i;
        }
    }

    private void runIterations(int timeSteps, int index, File indexFile,
                               ParticlesDefinition iteratedParticlesDefinition) {
        while (timeSteps > 0) {
            index++;
            System.out.println(new Date() + " ----- Iteration no. " + (index) + " of " + basicParameters.time_steps + " -----");

            final INDArray newVelocities = updateVelocitiesInChunks(iteratedParticlesDefinition);

            //Update positions, can just add velocities if assumed that one time step is one unit of time
            final INDArray newPositions = iteratedParticlesDefinition.getPosition().add(newVelocities);

            //TODO: apply boundary conditions

            //Write result to disk
            try {
                System.out.println(new Date() + " Start write velocities and positions to disk");
                try (FileOutputStream velocityOut = new FileOutputStream("DATA/velocity_" + index + ".iar", false)) {
                    Nd4j.write(velocityOut, newVelocities);
                }
                try (FileOutputStream positionOut = new FileOutputStream("DATA/position_" + index + ".iar", false)) {
                    Nd4j.write(positionOut, newPositions);
                }
                System.out.println(new Date() + " Done writing velocities and positions to disk.");
                FileUtils.write(indexFile, String.valueOf(index), "UTF-8");
            } catch (Throwable e) {
                throw new RuntimeException("An error happened in iteration no " + index, e);
            }
            iteratedParticlesDefinition.getVelocity().close();
            iteratedParticlesDefinition.getPosition().close();
            iteratedParticlesDefinition = ParticlesDefinition.builder().mass(iteratedParticlesDefinition.getMass()).position(newPositions).velocity(newVelocities).build();

            timeSteps--;
        }
        System.out.println("Done with simulation of " + basicParameters.time_steps + " iterations.");
    }

    INDArray updateVelocitiesInChunks(ParticlesDefinition particlesDefinition) {
        final int numberOfChunks = basicParameters.chunks;

        CountDownLatch countDownLatch = new CountDownLatch(numberOfChunks);
        System.out.println(new Date() + " Starting simulation with " + basicParameters.numTotalParticles() + " particles in " + numberOfChunks + " chunks");
        final ConcurrentHashMap<Integer, INDArray> simulationResultMap = new ConcurrentHashMap<>();
        long start = System.currentTimeMillis();

        for (int i = 0; i < numberOfChunks; i++) {
            final int j = i;
            executorService.submit(() -> {
                System.out.println(new Date() + " Chunk no " + j + " starting with from-to = " + basicParameters.chunkFrom(j) + " - " + basicParameters.chunkTo(j));
                final INDArray newVelocity = updateVelocities(particlesDefinition, basicParameters.chunkFrom(j), basicParameters.chunkTo(j));
                simulationResultMap.put(j, newVelocity);
                System.out.println(new Date() + " Chunk no " + j + " done");
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(new Date() + " Calculation Done. Time taken=" + (System.currentTimeMillis() - start) + "ms");
        //concat the results:
        INDArray result = null;
        for (int i = 0; i < numberOfChunks; i++) {
            INDArray newVelocity = simulationResultMap.get(i);
            if (i == 0) {
                result = newVelocity;
            } else {
                result = Nd4j.vstack(result, newVelocity);
            }
        }

        //Close them all
        for (int i = 0; i < numberOfChunks; i++) {
            simulationResultMap.get(i).close();
        }
        System.out.println(new Date() + " Done. Time taken=" + (System.currentTimeMillis() - start) + "ms");
        return result;
    }

    INDArray updateVelocities(ParticlesDefinition particlesDefinition, int from, int to) {
        int index = 0;
        final INDArray newVelocity = Nd4j.create(to - from, 3);

        final INDArray position = particlesDefinition.getPosition();
        final INDArray mass = particlesDefinition.getMass();
        final INDArray coef = mass.mul(-basicParameters.G);
        final INDArray velocity = particlesDefinition.getVelocity();

        for (int i = from; i < to; i++) {
            if (i % 2000 == 0) System.out.println("Doing #" + i);
            try (
                    INDArray px = position.getColumn(0).neg();
                    INDArray py = position.getColumn(1).neg();
                    INDArray pz = position.getColumn(2).neg();
                    INDArray scalarX = position.getColumn(0).getScalar(i);
                    INDArray scalarY = position.getColumn(1).getScalar(i);
                    INDArray scalarZ = position.getColumn(2).getScalar(i);
                    INDArray dx = scalarX.addColumnVector(px);
                    INDArray dy = scalarY.addColumnVector(py);
                    INDArray dz = scalarZ.addColumnVector(pz);
                    INDArray r2 = pow(dx, 2).add(pow(dy, 2).add(pow(dz, 2).add(Math.pow(basicParameters.epsilon, 2.0d))));

                    INDArray ax = dx.mul(coef);
                    INDArray ay = dy.mul(coef);
                    INDArray az = dz.mul(coef);

                    INDArray ax_scaled = ax.div(r2);//x-direction velocity squared
                    INDArray ay_scaled = ay.div(r2);
                    INDArray az_scaled = az.div(r2);

                    INDArray total_ax = ax_scaled.sum(1);
                    INDArray total_ay = ay_scaled.sum(1);
                    INDArray total_az = az_scaled.sum(1);
                    INDArray vScalarX = velocity.getColumn(0).getScalar(i);
                    INDArray vScalarY = velocity.getColumn(1).getScalar(i);
                    INDArray vScalarZ = velocity.getColumn(2).getScalar(i);
                    INDArray new_vel_x = vScalarX.add(total_ax);
                    INDArray new_vel_y = vScalarY.add(total_ay);
                    INDArray new_vel_z = vScalarZ.add(total_az)
            ) {
                newVelocity.put(index, 0, new_vel_x);
                newVelocity.put(index, 1, new_vel_y);
                newVelocity.put(index, 2, new_vel_z);
                index++;
            }
        }
        coef.close();
        return newVelocity;
    }

    INDArray updateVelocities(ParticlesDefinition particlesDefinition) {
        final int numberOfChunks = basicParameters.chunks;
        System.out.println(new Date() + " Starting simulation with " + basicParameters.numTotalParticles() + " particles in " + numberOfChunks + " chunks");
        long start = System.currentTimeMillis();
        int j = 0;
        final INDArray newVelocity = updateVelocities(particlesDefinition, basicParameters.chunkFrom(j), basicParameters.chunkTo(j));
        System.out.println(new Date() + " Calculation Done. Time taken=" + (System.currentTimeMillis() - start) + "ms");

        return newVelocity;
    }

}
