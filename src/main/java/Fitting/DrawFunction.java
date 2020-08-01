package Fitting;

/**
 * 本类从resources路径下读取csv文件中计算的数据并
 * 绘制成函数图像
 * @author ruiwen
 * @version 2020-7-10
 */

import tech.tablesaw.api.Table;
import javax.swing.*;
import java.awt.Graphics;
import java.io.IOException;
import java.util.ArrayList;

public class DrawFunction extends JFrame {
    //实验数据和拟合出来的数据
    static String experimentData = "src/main/resources/collectedData.csv";
    static String calculatedData = "src/main/resources/calculatedData.csv";
    private static PrepareData data = new PrepareData();
    static int W = 800, H = 600;    //图片的长宽
    static int side = 50;   //留白的长度
    static int x0 = side, y0 = H - side; //原点坐标
    static double minHue = 0, maxHue = 255; //hue值的最大最小值
    static double minPower = 0, maxPower = 1.2; //hue值的最大最小值

    Graphics G;
    private static ArrayList<Double> pointsX = new ArrayList<>();
    private static ArrayList<Double> pointsY = new ArrayList<>();

    /**
     * 读取已经计算完成的点。
     */
    public static void readCalculatedData(String file){
        try {
            Table table = Table.read().csv(file);
            int n = table.rowCount();//表格的行数
            for(int i=0; i<n; i++){
                //分别获取x列和y列的数据，一行行的拿，并放进数组中
                pointsX.add(Double.parseDouble(table.column("x").getString(i)));
                pointsY.add(Double.parseDouble(table.column("y").getString(i)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 画坐标轴
     */
    public void setOrigin() {
        //画x轴
        G.drawLine(0, y0, W, y0);
        //画y轴
        G.drawLine(x0, 0, x0, H);
        G.drawString("hue", W/2, y0+20);
        G.drawString("power", x0-20, H/2);
    }

    /**
     * 构造函数，调用内部类NewPanel的构造函数完成画图。
     */
    public DrawFunction() {
        add(new NewPanel());
    }

    /**
     * 将hue值转化为在图中的位置。
     * minX和maxX是hue值的取值范围。
     * @param x
     * @return
     */
    public int convertX(double x){
        int length = W - side;
        return (int)((x - minHue)/maxHue * length) + x0;
    }

    /**
     * 将功率值转换为在图中的位置。
     * minY和maxY是功率的取值范围。
     * @param y
     * @return
     */
    public int convertY(double y){
        int length = H - side;
        return (int) (y0 - ((y-minPower)/maxPower * length));
    }

    public static void main(String[] args) throws IOException {
        //拟合并计算出足够多的点，比如一万个，只要把这些点画到图中就画出函数图像了。
//        data.readCollectedData(experimentData);
//        data.train(3);
//        //data.calculatePoints(calculatedData,10000, 0, 255);

        //画函数图像
        readCalculatedData(calculatedData);
        DrawFunction frame = new DrawFunction();
        frame.setTitle("DrawFunction");
        frame.setSize(W, H);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
    }


    /**
     * 内部类
     */
    class NewPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            G = g;
            setOrigin();
            // in the following , draw what you want draw!
            for (int i=0; i<pointsX.size(); i++) {
                drawPoints(convertX(pointsX.get(i)), convertY(pointsY.get(i)));
            }
        }
    }

    public void drawPoints(int x, int y) {
        G.drawLine(x, y, x, y);
    }
}