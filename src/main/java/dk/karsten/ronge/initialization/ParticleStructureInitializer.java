package dk.karsten.ronge.initialization;

/**
 * Created by kar on 11/01/2019.
 */
public class ParticleStructureInitializer {

//    def particle_structure_init(G, num_pos_particles, num_neg_particles, num_tot_particles, limit, m_pos, m_neg, cube_pos_width, cube_neg_width):
//            """Initialise the positions, velocities, and masses of all particles for a structure formation simulation.
//
//    Args:
//    G (float): gravitational constant.
//    num_pos_particles (float): number of positive mass particles.
//            num_neg_particles (float): number of negative mass particles.
//            num_tot_particles (float): total number of particles.
//    limit (float): width of the simulated universe.
//            m_pos (float): the total positive mass in the simulated universe.
//    m_neg (float): the total negative mass in the simulated universe.
//    cube_pos_width (float): width of the uniformly distributed positive mass cube.
//    cube_neg_width (float): width of the uniformly distributed negative mass cube.
//
//    Returns:
//    position: numpy array of all particle positions in cartesian coordinates.
//            velocity: numpy array of all particle velocities in cartesian coordinates.
//            mass: numpy array of all particle masses.
//            """
//            # Set all the masses:
//            if num_pos_particles > 0:
//    mass_pos = np.random.uniform(m_pos/num_pos_particles, m_pos/num_pos_particles, num_pos_particles)
//            else:
//    mass_pos = np.array([])
//            if num_neg_particles > 0:
//    mass_neg = np.random.uniform(m_neg/num_neg_particles, m_neg/num_neg_particles, num_neg_particles)
//            else:
//    mass_neg = np.array([])
//    mass = np.concatenate((mass_pos, mass_neg), axis=0)
//            if len(mass) == 0:
//    print("ERROR: No particles included in the simulation.")
//    # Initially set all velocities to zero:
//    velocity = 0.0*np.random.randn(num_tot_particles, 3)
//            # For the positive masses (distributed as a uniformly distributed cube):
//    x = np.random.uniform((limit/2.0)-(cube_pos_width/2.0), (limit/2.0)+(cube_pos_width/2.0), num_pos_particles)
//    y = np.random.uniform((limit/2.0)-(cube_pos_width/2.0), (limit/2.0)+(cube_pos_width/2.0), num_pos_particles)
//    z = np.random.uniform((limit/2.0)-(cube_pos_width/2.0), (limit/2.0)+(cube_pos_width/2.0), num_pos_particles)
//            # For the negative masses (distributed as a uniformly distributed cube):
//    x_neg = np.random.uniform((limit/2.0)-(cube_neg_width/2.0), (limit/2.0)+(cube_neg_width/2.0), num_neg_particles)
//    y_neg = np.random.uniform((limit/2.0)-(cube_neg_width/2.0), (limit/2.0)+(cube_neg_width/2.0), num_neg_particles)
//    z_neg = np.random.uniform((limit/2.0)-(cube_neg_width/2.0), (limit/2.0)+(cube_neg_width/2.0), num_neg_particles)
//            # Combine the positive and negative masses together:
//    x = np.concatenate((x, x_neg), axis=0)
//    y = np.concatenate((y, y_neg), axis=0)
//    z = np.concatenate((z, z_neg), axis=0)
//    position = np.column_stack((x, y, z))
//            # Set the type to float32, in order to reduce the memory requirements:
//    position = position.astype(np.float32, copy=False)
//    velocity = velocity.astype(np.float32, copy=False)
//    mass = mass.astype(np.float32, copy=False)
//            return position, velocity, mass

}
