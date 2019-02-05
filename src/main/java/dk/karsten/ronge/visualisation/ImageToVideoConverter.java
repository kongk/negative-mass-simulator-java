package dk.karsten.ronge.visualisation;

import org.jcodec.api.awt.AWTSequenceEncoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by kar on 06/01/2019.
 */
public class ImageToVideoConverter {
    public void convert(int nofImagesToConvert) throws IOException {

        AWTSequenceEncoder encoder = AWTSequenceEncoder.createSequenceEncoder(new File("DATA/video1.mp4"), 25); // 25 fps
        for (int i=0;i<nofImagesToConvert;i++) {
            encoder.encodeImage(getImagesFromSomewhere(i));
        }
        encoder.finish();
    }

    private BufferedImage getImagesFromSomewhere(int index) throws IOException {
        final BufferedImage bufferedImage = ImageIO.read(new File("DATA/img3d-" + index + ".png"));
        int w = bufferedImage.getWidth();
        int h = bufferedImage.getHeight();
        BufferedImage after = new BufferedImage(1906, 1906, BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale(2.0, 2.0);
        AffineTransformOp scaleOp =
                new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        after = scaleOp.filter(bufferedImage, after);
        return after;
//        return bufferedImage;
    }

}
