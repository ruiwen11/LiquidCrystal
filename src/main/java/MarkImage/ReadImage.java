package MarkImage;

/**
 * 本类只提供了两个接口：
 * 1. 读取图片的所有像素点
 * 2. 计算标注的圆内的像素的平均hue值。
 *
 * @author ruiwen
 * @version 2020-7-15
 */

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class ReadImage {

    /**
     * 读取一张图片的RGB值
     * @throws Exception
     */
    public static Pixel[][] getImagePixel(String image) {
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
    public static double getAverageHue(ArrayList<Pixel> crystalPixel){
        double hueSum = 0;
        for(Pixel pixel : crystalPixel)
            hueSum += calculateHue(pixel);
        return hueSum/crystalPixel.size();
    }
    /**
     * 计算一个像素hue值的逻辑
     * @param pixel
     * @return
     */
    private static double calculateHue(Pixel pixel){
        double max = Math.max(pixel.blue, Math.max(pixel.green, pixel.red));
        double min = Math.min(pixel.blue, Math.min(pixel.green, pixel.red));
        if(max == min)
            return 0;

        //为了增加精度，hue值保留6位小数。
        DecimalFormat format = new DecimalFormat("0.000000");
        double hue = 0;
        if(max == pixel.red){
            System.out.println(format.format((pixel.green-pixel.blue) / (max-min)));
            hue = Double.parseDouble(format.format((pixel.green-pixel.blue) / (max-min)));
        }
        else if(max == pixel.green)
            hue = 2 + Double.parseDouble(format.format((pixel.blue-pixel.red) / (max-min)));
        else if(max == pixel.blue)
            hue = 4 + Double.parseDouble(format.format((pixel.red-pixel.green) / (max-min)));
        hue = hue * 60;
        hue = hue<0? hue+360 : hue;
        return hue;
    }

    /**
     * 写个案例，测试一下hue值的逻辑对不对。
     */
    @Test
    public void test(){
        Pixel pixel = new Pixel(255, 206, 227);
        System.out.println(calculateHue(pixel));
    }

}