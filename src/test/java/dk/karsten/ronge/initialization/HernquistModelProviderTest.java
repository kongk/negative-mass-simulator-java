package dk.karsten.ronge.initialization;

import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import static org.junit.Assert.assertEquals;

/**
 * Created by kar on 17/01/2019.
 */
public class HernquistModelProviderTest {
    @Test
    public void hernquist_ppf(){
        final INDArray testData = createTestArray();
        final INDArray hernquistPpf = new HernquistModelProvider().hernquist_ppf(testData, 1.0f);
        assertEquals(0.6327f, hernquistPpf.getFloat(0, 0), 0.0001f);
        assertEquals(1.7838f, hernquistPpf.getFloat(0, 1), 0.0001f);
        assertEquals(9.7305f, hernquistPpf.getFloat(0, 2), 0.0001f);
    }

    @Test
    public void hernquist_vcirc() {
        final INDArray testData = createTestArrayVCirc();
        final INDArray hernquistVcirc = new HernquistModelProvider().hernquist_vcirc(testData, 1.0f, createTestArrayPosMasses(), 1.0f);
        assertEquals(0.01529685f, hernquistVcirc.getFloat(0, 0), 0.0001f);
        assertEquals(0.01860979f, hernquistVcirc.getFloat(0, 1), 0.0001f);
        assertEquals(0.02202672f, hernquistVcirc.getFloat(0, 2), 0.0001f);

    }

    public static INDArray createTestArray() {
        final INDArray testData = Nd4j.zeros(1, 3);
        testData.put(0, 0, 0.84982134f);
        testData.put(0, 1, 0.589395495f);
        testData.put(0, 2, 0.17770009f);
        return testData;
    }

    INDArray createTestArrayVCirc() {
        final INDArray testData = Nd4j.zeros(1, 3);
        testData.put(0, 0, 0.156475865f);
        testData.put(0, 1, 3.48825509f);
        testData.put(0, 2, 0.706215621f);
        return testData;
    }

    INDArray createTestArrayPosMasses() {
        final INDArray testData = Nd4j.create(1, 3);
        testData.addi(0.002f);
        return testData;
    }

    @Test
    public void addition() {
        final INDArray add = createTestArrayPosMasses().add(0.0053f);
        for (float val : add.getRow(0).toFloatVector()) {
            assertEquals(0.0073f, val, 0.00001f);
        }
    }
}
