package dk.karsten.ronge.initialization;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by kar on 10/01/2019.
 */
public class ConfigurationTest {
    @Test
    public void createDefaultHaloParameters() throws Exception {
        final Configuration.BasicParameters defaultHaloParameters = new Configuration().createDefaultHaloParameters();
    }

    @Test
    public void createDefaultStructureParameters() throws Exception {
    }

}