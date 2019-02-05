package dk.karsten.ronge.initialization;

import dk.karsten.ronge.initialization.Configuration.BasicParameters;
import dk.karsten.ronge.initialization.Configuration.ParticlesDefinition;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.util.Random;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Created by kar on 11/01/2019.
 */
public class ParticleHaloInitializer {
    private final BasicParameters haloParameters;
    private final HernquistModelProvider hernquistModelProvider;

    public ParticleHaloInitializer(BasicParameters haloParameters) {
        this.haloParameters = haloParameters;
        this.hernquistModelProvider = new HernquistModelProvider();
    }

    public Mass massInit() {

        // Set all the masses:
        final INDArray mass_pos;
        if (haloParameters.num_pos_particles > 0) {
            final float massPerParticle = haloParameters.M_pos / haloParameters.num_pos_particles;
            mass_pos = Nd4j.create(1, haloParameters.num_pos_particles);
            mass_pos.addi(massPerParticle);
        } else {
            mass_pos = Nd4j.empty();
        }
        final INDArray mass_neg;
        if (haloParameters.num_neg_particles > 0) {
            final float massPerParticle = haloParameters.M_neg / haloParameters.num_neg_particles;
            mass_neg = Nd4j.create(1, haloParameters.num_neg_particles);
            mass_neg.addi(massPerParticle);
        } else {
            mass_neg = Nd4j.empty();
        }
        return new Mass(mass_pos, mass_neg);
    }

    public ParticlesDefinition haloInit() {
        Mass mass = massInit();
        //Initially set all velocities to zero:
        final INDArray velocity = Nd4j.randn(haloParameters.numTotalParticles(), 3).mul(0.0f);
        final INDArray radius = hernquistModelProvider.hernquist_ppf(Nd4j.rand(1, haloParameters.num_pos_particles), haloParameters.a_scale);
        //Must vel_0 be inside the loop?? No. It would come out with the same values in every iteration.
        final float[] vel_0 = hernquistModelProvider.hernquist_vcirc(radius, haloParameters.a_scale, mass.mass_pos, haloParameters.G).getRow(0).toFloatVector();
        final float[] phi_v = randomPhiDist().getRow(0).toFloatVector();
        final float[] theta_v = randomThetaDist().getRow(0).toFloatVector();
        final Random random = new Random();
        for (int i = 0; i < haloParameters.num_pos_particles; i++) {
            velocity.put(i, 0, vel_0[i] * sin(theta_v[i]) * cos(phi_v[i]) + random.nextGaussian() * haloParameters.gauss_velocity_comp);
            velocity.put(i, 1, vel_0[i] * sin(theta_v[i]) * sin(phi_v[i]) + random.nextGaussian() * haloParameters.gauss_velocity_comp);
            velocity.put(i, 2, vel_0[i] * cos(theta_v[i]) + random.nextGaussian() * haloParameters.gauss_velocity_comp);
        }

        INDArray phi = randomPhiDist();
        INDArray theta = randomThetaDist();
        final float universeMiddle = getUniverseMiddle();
        //Convert to cartesian coordinates (located at the centre of the simulation):
        INDArray x = radius.mul(Transforms.sin(theta)).mul(Transforms.cos(phi)).add(universeMiddle);
        INDArray y = radius.mul(Transforms.sin(theta)).mul(Transforms.sin(phi)).add(universeMiddle);
        INDArray z = radius.mul(Transforms.cos(theta)).add(universeMiddle);

        final INDArray positions = stackNegAndPositiveMassParticlesPositions(x, y, z);
        return ParticlesDefinition.builder().mass(mass.totalMass()).velocity(velocity).position(positions).build();
    }

    private INDArray stackNegAndPositiveMassParticlesPositions(INDArray x, INDArray y, INDArray z) {
        INDArray x_neg = getRandomOneDimNegPositions();
        INDArray y_neg = getRandomOneDimNegPositions();
        INDArray z_neg = getRandomOneDimNegPositions();

        //Combine the positive and negative mass positions together:
        x = Nd4j.concat(1, x, x_neg);
        y = Nd4j.concat(1, y, y_neg);
        z = Nd4j.concat(1, z, z_neg);
        return Nd4j.vstack(x, y, z).transpose();
    }

    private INDArray getRandomOneDimNegPositions() {
        return getRandomOneDimPositions(haloParameters.cube_neg_width, haloParameters.num_neg_particles);
    }

    private INDArray getRandomOneDimPositions(int cubeWidth, int numParticles) {
        final float universeMiddle = getUniverseMiddle();
        final double cubeMiddle = cubeWidth / 2.0f;
        return randomOneRowBetweenMinusOneAndOne(numParticles).mul(cubeMiddle).add(universeMiddle);
    }

    private float getUniverseMiddle() {
        return haloParameters.limit / 2.0f;
    }

    INDArray randomPhiDist() {
        return Nd4j.rand(1, haloParameters.num_pos_particles).mul(2 * Math.PI);
    }

    INDArray randomThetaDist() {
        return Transforms.acos(randomOneRowBetweenMinusOneAndOne(haloParameters.num_pos_particles));
    }

    INDArray randomOneRowBetweenMinusOneAndOne(int size) {
        //TODO: There may be an easier way than this...
        final INDArray dist = Nd4j.create(1, size);
        final Random random1 = new Random();
        for (int i = 0; i < size; i++) {
            double randBetweenMinusOneAndOne = random1.nextDouble() * 2 - 1;
            dist.put(0, i, randBetweenMinusOneAndOne);
        }
        return dist;
    }


    class Mass {
        final INDArray mass_pos, mass_neg;

        Mass(INDArray mass_pos, INDArray mass_neg) {
            this.mass_pos = mass_pos;
            this.mass_neg = mass_neg;

            if (mass_neg.isEmpty() && mass_pos.isEmpty()) {
                throw new RuntimeException("No particles included in the simulation");
            }
        }

        INDArray totalMass() {
            return Nd4j.concat(1, mass_pos, mass_neg);
        }
    }

}
