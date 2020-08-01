package MarkImage;

/**
 * 每标注一个圆就创建一个本类的对象，用于记录在同一张图片中的标注信息。
 * 避免重复标注。
 * @author ruiwen
 * @version 2020-8-1
 */
public class Circle {
    int centerX;
    int centerY;
    int radius;   //计算出来的圆的属性。

    public Circle(int centerX, int centerY, int radius) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    public void setCenterY(int centerY) {
        this.centerY = centerY;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    @Override
    public String toString() {
        return "{" +
                "圆心X轴坐标=" + centerX +
                ", 圆心Y轴坐标=" + centerY +
                ", 半径=" + radius +
                "}\n";
    }
}
