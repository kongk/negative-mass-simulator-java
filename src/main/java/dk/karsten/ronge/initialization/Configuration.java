package dk.karsten.ronge.initialization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.nd4j.linalg.api.ndarray.INDArray;

import static dk.karsten.ronge.initialization.Configuration.BasicParameters.SimType.HALO;
import static dk.karsten.ronge.initialization.Configuration.BasicParameters.SimType.STRUCTURE;

/**
 * Created by kar on 10/01/2019.
 */
public class Configuration {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ParticlesDefinition {
        final INDArray mass, velocity, position;
    }

    @Builder
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class BasicParameters {
        public enum SimType {HALO, STRUCTURE}

        public final int limit, radius, num_pos_particles, num_neg_particles, time_steps, chunks;
        public final float G, epsilon;
        public final float M_pos, M_neg;
        public final int cube_neg_width;
        public final int cube_pos_width;
        public final float a_scale, gauss_velocity_comp;
        public final SimType simType;

        public int numTotalParticles() {
            return num_pos_particles + num_neg_particles;
        }

        /**
         * @param chunkNo zero based number of chunks, e.g. first chunk has chunkNo=0
         * @return inclusive from index
         */
        public int chunkFrom(int chunkNo) {
            return chunkNo * chunkSize();
        }

        /**
         * @param chunkNo zero based number of chunks, e.g. first chunk has chunkNo=0
         * @return exclusive to index
         */
        public int chunkTo(int chunkNo) {
            return chunkNo * chunkSize() + chunkSize();
        }

        int chunkSize() {
            return numTotalParticles() / chunks;
        }
    }

    //TODO: These could be read from properties file
    public BasicParameters createDefaultHaloParameters() {
        return createBasicHaloParametersBuilder()
                .num_pos_particles(2000)
                .num_neg_particles(18000)
                .build();
    }

    public BasicParameters.BasicParametersBuilder createBasicHaloParametersBuilder() {
        return BasicParameters.builder()
                .simType(HALO)
                .G(1.0f).epsilon(0.07f).limit(80000).radius(4)
                .time_steps(12).chunks(4)
                .M_pos(1.0f).M_neg(-3.0f).cube_neg_width(200)
                .a_scale(1.0f).gauss_velocity_comp(0.3f);
    }

    public BasicParameters createDefaultStructureParameters() {
        return BasicParameters.builder()
                .simType(STRUCTURE)
                .G(1.0f).epsilon(0.07f).limit(80000).radius(4).num_pos_particles(25000).num_neg_particles(25000)
                .time_steps(1000).chunks(4)
                .M_pos(1.0f).M_neg(-1.0f).cube_neg_width(200)
                .cube_pos_width(200).build();
    }

}
