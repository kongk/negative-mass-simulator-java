package dk.karsten.ronge.application;

import dk.karsten.ronge.initialization.Configuration;
import dk.karsten.ronge.initialization.ParticleHaloInitializer;
import dk.karsten.ronge.simulation.Simulation;

/**
 * Created by kar on 23/01/2019.
 */
public class SimulationRunner {
    public static void main(String[] args){
        Configuration.BasicParameters defaultHaloParameters = new Configuration().createDefaultHaloParameters();
        ParticleHaloInitializer particleHaloInitializer = new ParticleHaloInitializer(defaultHaloParameters);
        final Configuration.ParticlesDefinition particlesDefinition = particleHaloInitializer.haloInit();
        final Simulation simulation = new Simulation( defaultHaloParameters);
        simulation.run_nbody(particlesDefinition);
//        System.exit(0);
    }
}
