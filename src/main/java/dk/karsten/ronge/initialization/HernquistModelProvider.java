package dk.karsten.ronge.initialization;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

/**
 * Created by kar on 11/01/2019.
 */
public class HernquistModelProvider {
    public INDArray hernquist_ppf(INDArray r, float a_scale) {
        //    ppf = (a_scale-(a_scale*r)+np.sqrt(a_scale**2 - (r*(a_scale**2))))/r
        final double a_scale_sqrd = Math.pow(a_scale, 2.0f);
        INDArray sqr = r.mul(a_scale_sqrd).neg().add(a_scale_sqrd);
        return r.mul(a_scale).neg().add(a_scale).add(Transforms.sqrt(sqr)).div(r);
    }

    public INDArray hernquist_vcirc(INDArray r, float a_scale, INDArray m, float G) {
        final INDArray inverse_pow = Transforms.pow(r.add(a_scale), -2);
        return Transforms.sqrt(m.mul(G).mul(r).mul(inverse_pow));
    }

}
