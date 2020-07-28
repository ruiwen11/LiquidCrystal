package MarkImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class ReadImage {

    /**
     * 读取一张图片的RGB值
     * @throws Exception
     */
    public static Pixel[][] getImagePixel(String image) throws Exception {
        int[] rgb = new int[3];
        File file = new File(image);
        BufferedImage bi = null;//
        try {
            bi = ImageIO.read(file);//读取图片
        } catch (Exception e) {
            e.printStackTrace();
        }
        int width = bi.getWidth();  //图片的宽，对应坐标系中的x
        int height = bi.getHeight();//图片的长，对应坐标系中的y
        int minx = bi.getMinX();
        int miny = bi.getMinY();
        System.out.println("width=" + width + ",height=" + height + ".");
        System.out.println("minx=" + minx + ",miniy=" + miny + ".");
        Pixel[][] pixels = new Pixel[height][width];    //将图片的每一个像素都保存在这个二维数组中
        for (int i = minx; i < width; i++) {
            for (int j = miny; j < height; j++) {
                int pixel = bi.getRGB(i, j); //getRGB()获取三原色的值 下面三行代码将一个数字转换为RGB数字
                rgb[0] = (pixel & 0xff0000) >> 16;  //通过 &位运算& 获取红色值
                rgb[1] = (pixel & 0xff00) >> 8;     //通过 &位运算& 获取绿色值
                rgb[2] = (pixel & 0xff);            //通过 &位运算& 获取蓝色值
                pixels[j][i] = new Pixel(rgb[0], rgb[1], rgb[2]);
            }
        }
        return pixels;
    }

    /**
     * 计算液晶所有像素的平均hue值。
     * @param crystalPixel
     */
    public static int getHue(ArrayList<Pixel> crystalPixel){
        return 0;
    }

    /**
     * 返回屏幕色彩值
     * @param x
     * @param y
     * @return
     * @throws AWTException
     */
//    public static int getScreenPixel(int x, int y) throws AWTException { // 函数返回值为颜色的RGB值。
//        Robot rb = null; // java.awt.image包中的类，可以用来抓取屏幕，即截屏。
//        rb = new Robot();
//        Toolkit tk = Toolkit.getDefaultToolkit(); // 获取缺省工具包
//        Dimension di = tk.getScreenSize(); // 屏幕尺寸规格
//        System.out.println(di.width);
//        System.out.println(di.height);
//        Rectangle rec = new Rectangle(0, 0, di.width, di.height);
//        BufferedImage bi = rb.createScreenCapture(rec);
//        int pixelColor = bi.getRGB(x, y);
//
//        return 16777216 + pixelColor; // pixelColor的值为负，经过实践得出：加上颜色最大值就是实际颜色值。
//    }

}