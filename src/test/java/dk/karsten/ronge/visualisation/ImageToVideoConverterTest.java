package dk.karsten.ronge.visualisation;


import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created by kar on 06/01/2019.
 */
public class ImageToVideoConverterTest {
    @Test
    public void convert() throws Exception {
        File file = new File("DATA/img3d-0.png");
        System.out.println(file.canRead());
        BufferedImage bufferedImage = ImageIO.read(file);
        System.out.println(bufferedImage.getHeight());
        System.out.println(bufferedImage.getWidth());
        file = new File("DATA/img3d-1.png");
        bufferedImage = ImageIO.read(file);
        System.out.println(bufferedImage.getHeight());
        System.out.println(bufferedImage.getWidth());


        new ImageToVideoConverter().convert(2);
    }

}