package Fitting;

/**
 * 本类从resources路径下读取csv文件中计算的数据并
 * 绘制成函数图像
 * @author ruiwen
 * @version 2020-7-10
 */

import tech.tablesaw.api.Table;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class DrawFunctionPicture extends JFrame {
    //实验数据和拟合出来的数据
    public final static String experimentData = "src/main/resources/collectedData.csv";
    public final static String calculatedData = "src/main/resources/calculatedData.csv";
    public final static String CSVOfHue = "src/main/resources/hueAndPower.csv";
    public final static String savePicture = "src/main/resources/函数图.jpg";
    final static int W = 800, H = 600;    //图片的长宽
    final static int side = 50;   //留白的长度
    final static int numOfSides = 3;    //为了美观，坐标轴不会画到顶，此处设置坐标轴空出多少倍的留白。
    final static int archOfAxisX = 10;
    final static int archOfAxisY = 5;   //这两个常数是坐标轴箭头的长度
    final static int lengthOfScale = 4; //坐标轴上的刻度的高度
    final static int numOfScale = 10;   //画多少个坐标轴刻度
    final static int x0 = side, y0 = H-side; //原点坐标
    final static int sizeOfMarkPoints = 3;  //将手动标注的液晶对应的点画到图上，这是标注的点的半径
    final static double minHue = 0, maxHue = 255; //hue值的最大最小值
    final static double minPower = 0, maxPower = 200; //hue值的最大最小值

    Graphics G;
    private static ArrayList<Double> pointsX = new ArrayList<>();
    private static ArrayList<Double> pointsY = new ArrayList<>();   //用来画图的点
    private static ArrayList<Double> markPointsX = new ArrayList<>();
    private static ArrayList<Double> markPointsY = new ArrayList<>();//标注的液晶的色调值和功率。

    private static PrepareData data = new PrepareData();

    /**
     * 读取已经计算完成的点。
     */
    public static void readData(){
        try {
            //读取用来绘图的点。
            Table calData = Table.read().csv(calculatedData);
            int n = calData.rowCount();//表格的行数
            for(int i=0; i<n; i++){
                //分别获取x列和y列的数据，一行行的拿，并放进数组中
                pointsX.add(Double.parseDouble(calData.column("x").getString(i)));
                pointsY.add(Double.parseDouble(calData.column("y").getString(i)));
            }

            //读取标注的点。
            Table markData = Table.read().csv(CSVOfHue);
            n = markData.rowCount();
            for(int i=0; i<n; i++){
                markPointsX.add(Double.parseDouble(markData.column("x").getString(i)));
                markPointsY.add(Double.parseDouble(markData.column("y").getString(i)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 画坐标轴和刻度线
     */
    public void setOrigin() {
        G.setColor(Color.BLACK);    //画笔切换成黑色。
        G.setFont(new Font(null, Font.BOLD, 3));

        //画x轴
        G.drawLine(0, y0, W-side, y0);
        G.drawLine(W-side-archOfAxisX, y0-archOfAxisY, W-side, y0);
        G.drawLine(W-side-archOfAxisX, y0+archOfAxisY, W-side, y0);   //画x轴的箭头
        //画y轴
        G.drawLine(x0, side, x0, H);
        G.drawLine(x0+archOfAxisY, side+archOfAxisX, x0, side);
        G.drawLine(x0-archOfAxisY, side+archOfAxisX, x0, side);   //画y轴的箭头

        //标x轴的刻度
        int length = W - side*numOfSides;
        double slot = ((double) length)/numOfScale;
        /*---------------------刻度值暂时只标第一个，看看效果，效果好后期再加---------------------*/
        G.setFont(new Font(null, Font.PLAIN, 12));
        G.drawString(""+(maxHue-minHue)/numOfScale, (int)(x0+slot)-15, y0+15);
        /*--------------------------------------------------------------------------------*/
        G.setFont(new Font(null, Font.BOLD, 5));
        for (int i=1; i<=numOfScale; i++){
            G.drawLine((int)(x0+i*slot), y0, (int)(x0+i*slot), y0-lengthOfScale);
        }
        //标y轴的刻度
        length = H - side*numOfSides;
        slot = ((double) length)/numOfScale;
        /*--------------------------------------------------------------------------------*/
        G.setFont(new Font(null, Font.PLAIN, 12));
        G.drawString(""+(maxPower-minPower)/numOfScale, x0-25, (int)(y0-slot+5));
        /*--------------------------------------------------------------------------------*/
        G.setFont(new Font(null, Font.BOLD, 5));
        for (int i=1; i<=numOfScale; i++){
            G.drawLine(x0, (int)(y0-i*slot), x0+lengthOfScale, (int)(y0-i*slot));
        }

        G.setFont(new Font(null, Font.BOLD, 16));
        G.drawString("Hue", W/2, y0+20);
        G.drawString("Power", x0-50, H/2);
    }

    /**
     * 构造函数，调用内部类NewPanel的构造函数完成画图。
     */
    public DrawFunctionPicture() {
        add(new NewPanel());
    }

    /**
     * 将hue值转化为在图中的位置。
     * minX和maxX是hue值的取值范围。
     * @param x
     * @return
     */
    public int convertX(double x){
        int length = W - side*numOfSides;
        return (int)((x - minHue)/maxHue * length) + x0;
    }

    /**
     * 将功率值转换为在图中的位置。
     * minY和maxY是功率的取值范围。
     * @param y
     * @return
     */
    public int convertY(double y){
        int length = H - side*numOfSides;
        return (int) (y0 - ((y-minPower)/maxPower * length));
    }

    public static void main(String[] args) throws IOException {
        //拟合并计算出足够多的点，比如一万个，只要把这些点画到图中就画出函数图像了。
//        data.readCollectedData(experimentData);
//        data.train(3);
//        //data.calculatePoints(calculatedData,10000, 0, 255);

        //画函数图像
        readData();
        DrawFunctionPicture frame = new DrawFunctionPicture();
        frame.setTitle("DrawFunction");
        frame.setLocation(0,0);
        frame.setSize(W+10, H+30);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);

        //选择是否保存图片
        int yes = JOptionPane.showConfirmDialog(null, "是否保存图片？", "提示", JOptionPane.YES_NO_OPTION);
        if(yes == JOptionPane.YES_OPTION){
            PrepareData.savePicture(frame, savePicture);
        }
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
            for (int i=0; i<pointsX.size(); i++)
                drawPoints(convertX(pointsX.get(i)), convertY(pointsY.get(i)));

            for (int i=0; i<markPointsX.size(); i++)
                drawMarkPoints(convertX(markPointsX.get(i)), convertY(markPointsY.get(i)));
        }
    }

    public void drawPoints(int x, int y) {
        G.setColor(Color.BLACK);
        G.drawLine(x, y, x, y);
    }

    public void drawMarkPoints(int x, int y){
        G.setColor(Color.red);
        G.drawOval(x-sizeOfMarkPoints, y-sizeOfMarkPoints, sizeOfMarkPoints*2, sizeOfMarkPoints*2);
    }
}