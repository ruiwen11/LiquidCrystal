package MarkImage;

/**
 * 每读取一张图片，就保存该图片中的所有像素，便于后期处理色调值。
 */
public class Pixel {

    public int red;
    public int green;
    public int blue;

    public Pixel(int red, int green, int blue){
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public String toString(){
        return "(" + this.red + "," + this.green + "," + this.blue + ")";
    }
}
