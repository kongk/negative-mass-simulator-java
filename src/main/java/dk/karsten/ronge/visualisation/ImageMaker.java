package dk.karsten.ronge.visualisation;


import dk.karsten.ronge.initialization.Configuration.BasicParameters;
import org.jzy3d.analysis.AWTAbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.chart.factories.AWTChartFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Scale;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.primitives.axis.layout.AxisLayout;
import org.jzy3d.plot3d.primitives.axis.layout.renderers.DefaultDecimalTickRenderer;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Created by kar on 28/01/2019.
 */
public class ImageMaker extends AWTAbstractAnalysis {

    final INDArray positions;
    final BasicParameters basicParameters;
    Scatter scatter = null;

    public ImageMaker(BasicParameters basicParameters, INDArray positions) throws Exception {
        this.positions = positions;
        this.basicParameters = basicParameters;
        AnalysisLauncher.open(this);
    }

    @Override
    public void init() {
        scatter = populateScatter(positions);
        chart = AWTChartFactory.chart(Quality.Advanced());


        AxisLayout axeLayout = chart.getAxisLayout();
        // Set precision of tick values
        axeLayout.setXTickRenderer(new DefaultDecimalTickRenderer());
        axeLayout.setYTickRenderer(new DefaultDecimalTickRenderer());
        axeLayout.setZTickRenderer(new DefaultDecimalTickRenderer());

        BoundingBox3d b = chart.getView().getBounds();
        final int cube_neg_width = basicParameters.cube_neg_width;
        final int center = basicParameters.limit / 2;
        int maxW = center + cube_neg_width / 2;
        int minW = center - cube_neg_width / 2;
        chart.setScale(new Scale(minW, maxW));
        chart.getScene().add(scatter);
        b.setXmax(maxW);
        b.setXmin(minW);
        b.setYmax(maxW);
        b.setYmin(minW);
        b.setZmax(maxW);
        b.setZmin(minW);

        chart.getView().setBackgroundColor(new Color(0.99f, 0.99f, 0.99f, 0.25f));
        chart.getView().shoot();
    }

    public void showPositions(INDArray positions) {
        scatter = populateScatter(positions);
        chart.getView().shoot();
    }

    Scatter populateScatter(INDArray positions) {
        int size = positions.rows();
        float a;

        Coord3d[] points = new Coord3d[size];
        Color[] colors = new Color[size];

        for (int i = 0; i < size; i++) {
            final float x = positions.getFloat(i, 0);
            final float y = positions.getFloat(i, 1);
            final float z = positions.getFloat(i, 2);

            final Coord3d coord3d = new Coord3d(x, y, z);
            points[i] = coord3d;
            a = 0.25f;

            if (i < basicParameters.num_pos_particles) {
                colors[i] = new Color(0.1f, 0.8f, 0.0f, a);
            } else {
                colors[i] = new Color(0.7f, 0.1f, 0.1f, a);
            }

        }
        if (scatter == null) {
            scatter = new Scatter(points, colors);
            scatter.setWidth(5);
        } else {
            scatter.clear();
            scatter.setData(points);
        }
        return scatter;
    }

}
