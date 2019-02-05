package dk.karsten.ronge.initialization;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

/**
 * Created by kar on 11/01/2019.
 */
public class HernquistModelProvider {
    public INDArray hernquist_ppf(INDArray r, float a_scale) {
        final double a_scale_sqrd = Math.pow(a_scale, 2.0f);
        INDArray sqr = r.mul(a_scale_sqrd).neg().add(a_scale_sqrd);
        INDArray ppf = r.mul(a_scale).neg().add(a_scale).add(Transforms.sqrt(sqr)).div(r);
        return ppf;
    }

    public INDArray hernquist_vcirc(INDArray r, float a_scale, INDArray m, float G) {
        final INDArray inverse_pow = Transforms.pow(r.add(a_scale), -2);
        return Transforms.sqrt(m.mul(G).mul(r).mul(inverse_pow));
    }

}
