package dk.karsten.ronge.serialization;

import dk.karsten.ronge.initialization.Configuration;
import org.nd4j.shade.jackson.annotation.JsonCreator;
import org.nd4j.shade.jackson.annotation.JsonProperty;

/**
 * Created by kar on 28/01/2019.
 */
public class BasicParametersJsonImpl extends Configuration.BasicParameters{
    @JsonCreator
    public BasicParametersJsonImpl(@JsonProperty("limit") int limit, @JsonProperty("radius") int radius, @JsonProperty("num_pos_particles") int num_pos_particles, @JsonProperty("num_neg_particles") int num_neg_particles, @JsonProperty("time_steps") int time_steps,
                                   @JsonProperty("chunks") int chunks, @JsonProperty("G") float G, @JsonProperty("epsilon") float epsilon, @JsonProperty("M_pos") float M_pos, @JsonProperty("M_neg") float M_neg, @JsonProperty("cube_neg_width") int cube_neg_width,
                                   @JsonProperty("cube_pos_width") int cube_pos_width, @JsonProperty("a_scale") float a_scale, @JsonProperty("gauss_velocity_comp") float gauss_velocity_comp,
                                   @JsonProperty("simType") Configuration.BasicParameters.SimType simType) {
        super(limit, radius, num_pos_particles, num_neg_particles, time_steps, chunks, G, epsilon, M_pos, M_neg, cube_neg_width, cube_pos_width, a_scale, gauss_velocity_comp, simType);
    }
}
