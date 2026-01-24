package dk.karsten.ronge.initialization;

import org.junit.Before;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;

import static org.junit.Assert.*;

/**
 * Created by kar on 14/01/2019.
 */
public class ParticleHaloInitializerTest {
    ParticleHaloInitializer particleHaloInitializer;
    Configuration.BasicParameters defaultHaloParameters;

    @Before
    public void initTest() {
        defaultHaloParameters = new Configuration()
                .createBasicHaloParametersBuilder()
                .num_pos_particles(5000)
                .num_neg_particles(45000)
                .build();
        particleHaloInitializer = new ParticleHaloInitializer(defaultHaloParameters);
    }

    @Test
    public void massInit(){
        final ParticleHaloInitializer.Mass mass = particleHaloInitializer.massInit();
        final INDArray totalMass = mass.totalMass();
        assertEquals(1, totalMass.rows());
        assertEquals(defaultHaloParameters.numTotalParticles(), totalMass.columns());
        assertEquals(defaultHaloParameters.num_pos_particles, mass.mass_pos.columns());
        assertEquals(defaultHaloParameters.num_neg_particles, mass.mass_neg.columns());
        for (float aMass : mass.mass_pos.getRow(0).toFloatVector()) {
            assertEquals(0.0002f, aMass, 0.000001f);
        }
        for (float aMass : mass.mass_neg.getRow(0).toFloatVector()) {
            assertEquals(-0.00006666667f, aMass, 0.0000001f);
        }
        for (float aMass : totalMass.getRow(0).toFloatVector()) {
            assertTrue(aMass == 0.0002f || aMass == -0.00006666667f);
        }
    }

    @Test
    public void velocityInit(){
        final Configuration.ParticlesDefinition particlesDefinition = particleHaloInitializer.haloInit();
        final INDArray velocity = particlesDefinition.velocity;
        final INDArray position = particlesDefinition.position;

        assertEquals(defaultHaloParameters.numTotalParticles(), velocity.rows());
        assertEquals(3, velocity.columns());

        assertEquals(defaultHaloParameters.numTotalParticles(), position.rows());
        assertEquals(3, position.columns());
        float totalVelocity = 0.0f;
        for (int i = 0; i < velocity.columns(); i++) {
            for (float aVelocity : velocity.getRow(i).toFloatVector()) {
                totalVelocity += aVelocity;
                assertTrue("aVelocity=" + aVelocity, aVelocity >= -1.0f && aVelocity <= 1.0f);
                if (i > defaultHaloParameters.num_pos_particles) {
                    assertEquals(0.00f, aVelocity, 0.0001f);
                }
            }
        }
        System.out.println(totalVelocity);
    }

    @Test
    public void positionInit(){
        final Configuration.ParticlesDefinition particlesDefinition = particleHaloInitializer.haloInit();
        final INDArray position = particlesDefinition.position;

        assertEquals(defaultHaloParameters.numTotalParticles(), position.rows());
        assertEquals(3, position.columns());
        for (int i = 0; i < position.columns(); i++) {
            for (float aPosition : position.getRow(i).toFloatVector()) {
                if (i > defaultHaloParameters.num_pos_particles) {
                    assertTrue("aPosition=" + aPosition,
                            aPosition >= defaultHaloParameters.limit / 2 - defaultHaloParameters.cube_neg_width / 2
                                    && aPosition <= defaultHaloParameters.limit / 2 + defaultHaloParameters.cube_neg_width / 2);
                }
            }
        }
    }

    @Test
    public void randomPhiDist() {
        final INDArray phis = particleHaloInitializer.randomPhiDist();

        assertEquals(1, phis.rows());
        assertEquals(defaultHaloParameters.num_pos_particles, phis.columns());
        float totalPhis = 0.0f;
        for (float aPhi : phis.getRow(0).toFloatVector()) {
            totalPhis += aPhi;
            assertTrue("aPhi=" + aPhi, aPhi >= 0.0f && aPhi <= Math.PI * 2);
        }
        final float averagePhi = totalPhis / defaultHaloParameters.num_pos_particles;
        //Lacking verification of standard deviation
        assertEquals(3.1415f, averagePhi, 0.1f);
    }

    @Test
    public void randomVectorBetweenMinusOneAndOne() {
        final INDArray vector = particleHaloInitializer.randomOneRowBetweenMinusOneAndOne(defaultHaloParameters.num_pos_particles);
        assertEquals(1, vector.rows());
        assertEquals(defaultHaloParameters.num_pos_particles, vector.columns());
        float totalValues = 0.0f;
        for (float aValue : vector.getRow(0).toFloatVector()) {
            totalValues += aValue;
            assertTrue("aValue=" + aValue, aValue >= -1.0f && aValue <= 1.0f);
        }
        final float averagePhi = totalValues / defaultHaloParameters.num_pos_particles;
        System.out.println("averagePhi=" + averagePhi);
        //Lacking verification of standard deviation
        assertEquals(0.0f, averagePhi, 0.02f);
    }

}