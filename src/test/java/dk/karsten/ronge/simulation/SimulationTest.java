package dk.karsten.ronge.simulation;

import dk.karsten.ronge.initialization.Configuration;
import dk.karsten.ronge.initialization.ParticleHaloInitializer;
import org.junit.Ignore;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import static org.junit.Assert.*;

/**
 * Created by kar on 24/01/2019.
 */
public class SimulationTest {

    @Test
    @Ignore
    public void runUpdate_velocities() {

        Configuration.BasicParameters defaultHaloParameters = new Configuration().createDefaultHaloParameters();
        ParticleHaloInitializer particleHaloInitializer = new ParticleHaloInitializer(defaultHaloParameters);
        final Configuration.ParticlesDefinition particlesDefinition = particleHaloInitializer.haloInit();
        final Simulation simulation = new Simulation(defaultHaloParameters);

        simulation.updateVelocities(particlesDefinition);
    }

    @Test
    public void testUpdate_velocities(){

        Configuration.BasicParameters defaultHaloParameters = new Configuration()
                .createBasicHaloParametersBuilder()
                .num_pos_particles(2)
                .num_neg_particles(3)
                .build();
        final INDArray mass = Nd4j.create(1, 5);
        mass.put(0, 0, 0.5f);
        mass.put(0, 1, 0.5f);
        mass.put(0, 2, -1.00f);
        mass.put(0, 3, -1.00f);
        mass.put(0, 4, -1.00f);

        INDArray velocities = Nd4j.create(3, 5);
        velocities.put(0, 0, -0.06069f);
        velocities.put(0, 1, -0.23227f);
        velocities.put(0, 2, 0.0f);
        velocities.put(0, 3, 0.0f);
        velocities.put(0, 4, 0.0f);
        velocities.put(1, 0, 0.48519f);
        velocities.put(1, 1, -0.11201f);
        velocities.put(1, 2, 0.0f);
        velocities.put(1, 3, -0.0f);
        velocities.put(1, 4, -0.0f);
        velocities.put(2, 0, 0.39294f);
        velocities.put(2, 1, 0.39378f);
        velocities.put(2, 2, -0.0f);
        velocities.put(2, 3, 0.0f);
        velocities.put(2, 4, -0.0f);
        velocities = velocities.transpose();

        INDArray positions = Nd4j.create(3, 5);
        positions.put(0, 0, 40000.01953f);
        positions.put(0, 1, 40000.48828f);
        positions.put(0, 2, 40118.16406f);
        positions.put(0, 3, 39876.07422f);
        positions.put(0, 4, 39891.21875f);
        positions.put(1, 0, 40000.20703f);
        positions.put(1, 1, 40000.91797f);
        positions.put(1, 2, 39966.01172f);
        positions.put(1, 3, 40079.46875f);
        positions.put(1, 4, 40080.24609f);
        positions.put(2, 0, 40000.05469f);
        positions.put(2, 1, 39999.88281f);
        positions.put(2, 2, 40034.20703f);
        positions.put(2, 3, 40104.85547f);
        positions.put(2, 4, 39963.45703f);
        positions = positions.transpose();

        final Configuration.ParticlesDefinition particlesDefinition = Configuration.ParticlesDefinition.builder().mass(mass).position(positions).velocity(velocities).build();

        final Simulation simulation = new Simulation(defaultHaloParameters);

        INDArray updatedVelocities = simulation.updateVelocities(particlesDefinition);
        assertEquals(0.2500f, updatedVelocities.getFloat(0, 0), 0.0001f);
        assertEquals(0.9487f, updatedVelocities.getFloat(0, 1), 0.0001f);
    }


}