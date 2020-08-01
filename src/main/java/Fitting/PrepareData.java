package Fitting;

/**
 * 本类用于计算用于拟合实验数据，输出拟合出来的多项式，并计算画图所需要的点。
 * @author ruiwen
 * @version 2020-7-10
 */

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import tech.tablesaw.api.Table;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class PrepareData {
    //变量全部声明为static，这样就不必每次使用时都创建该类的对象。
    private static double[] x;
    private static double[] y;
    private static double[] elements;
    //创建一个单例的writer，用于向文件中写数据。用单例模式减少开销。
    private static BufferedWriter writer = null;
    //保存拟合出来的多项式系数，方便以后调用
    private static String coefficient = "src/main/resources/coefficient.csv";

    /**
     * 从csv文件的数据以表格的形式读入实验数据。
     */
    public void readCollectedData(String file){
        try {
            Table table = Table.read().csv(file);
            int n = table.rowCount();//表格的行数
            x = new double[n];
            y = new double[n];
            for(int i=0; i<n; i++){
                //分别获取x列和y列的数据，一行行的拿，并放进数组中
                x[i] = Double.parseDouble(table.column("x").getString(i));
                y[i] = Double.parseDouble(table.column("y").getString(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据拟合出对的多项式，就算出n个在x轴上分布均匀的点用于函数图像绘制。
     * @param file 保存数据的路径
     * @param numberOfPoints 画的点的个数
     * @param startX
     * @param endX hue值的范围，用于计算每个点的hue
     * @throws IOException
     */
    public void calculatePoints(String file, int numberOfPoints, double startX, double endX) throws IOException {
        writeToCSV(file,"y,x\n");
        double slot = (endX - startX)/numberOfPoints;
        for (int i=0; i<numberOfPoints; i++){
            double x = startX + slot*i;
            double y = function(x);
            y = y<0 ? 0 : y;
//            y = y>1.2 ? 1.2 : y;
            writeToCSV(file, y + "," + x + "\n");
        }
    }

    /**
     * 每次写入数据都要新建一个writer，开销比较大。
     * 这边后期建议改完单例模式。
     * @param fileName
     * @param content
     */
    //todo
    public static void writeToCSV(String fileName, String content) throws IOException {
        try {
            File file = new File(fileName);
            if(!file.exists())
                file.createNewFile();

            writer = new BufferedWriter(new FileWriter(file, true));//该true表示向已存在的文件续写内容
            writer.write(content);
            writer.flush();
            writer.close();
        }catch (IOException e) {
            writer.close();
            writer = null;
            e.printStackTrace();
        } finally {
        }
    }

    /**
     * 当需要重新计算数据时，删除文件。
     * @param routine
     * @return
     */
    public static boolean deleteFile(String routine){
        File file = new File(routine);
        return file.delete();
    }

    /**
     * 保存图片
     * @param jFrame
     * @param fileName
     */
    public static void savePicture(JFrame jFrame, String fileName){
        //获得窗口的内容面板
        Container content = jFrame.getContentPane();
        //创建图片缓冲对象
        BufferedImage image = new BufferedImage(jFrame.getWidth()-10, jFrame.getHeight()-30, BufferedImage.TYPE_3BYTE_BGR);
        //获得图片对象
        Graphics graphics = image.createGraphics();
        //将窗口内容输出到图形对象中
        content.printAll(graphics);
        //保存为图片
        File file = new File(fileName);
        try {
            ImageIO.write(image, "jpg", file);
        } catch (IOException e){
            e.printStackTrace();
        }
        graphics.dispose();
    }

    /**
     * 进行拟合，引入的是commons-math3这个包。
     * 使用很简单，将每一对坐标放进WeightedObservedPoints对象中。
     * 然后new一个多项拟合器PolynomialCurveFitter。fit这些点。
     * 使用的commons-math3版本是3.6.1，还支持正弦拟合和高斯拟合。
     */
    public void train(int degree) throws IOException {
        WeightedObservedPoints points = new WeightedObservedPoints();

        for(int i=0; i<x.length; i++){
            points.add(x[i], y[i]);
        }

        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(degree);
        elements = fitter.fit(points.toList());

        //将多项式拟合的结果保存到文件中，以备下次使用。
        writeToCSV(coefficient, "coefficient\n");
        for (double d : elements)
            writeToCSV(coefficient, d+"\n");

        printFormula(elements);
    }

    /**
     * 打印函数方程
     * @param elements
     * @return
     */
    public static String printFormula(double[] elements){
        StringBuilder equation = new StringBuilder("y = ");
        for(int i=elements.length-1; i>0; i--){
            if(elements[i] < 0){  //如果系数小于0，加上-
                if(i < elements.length-1)
                    equation.append(" - ");
                else if(i == elements.length-1)
                    equation.append(" -");

            } else if(elements[i] > 0){   //如果系数大于0，加上+
                if (i < elements.length-1)    //但如果是首位就不加+
                    equation.append(" + ");
            }
            equation.append(keep10digits(Math.abs(elements[i])));
            equation.append("x");
            if (i > 1){ //多项式阶数为1时，省略1。即x^1 -> x。
                equation.append("^" + i);
            }
        }
        //加上常数项
        if(elements[0] < 0)
            equation.append(" - ");
        else
            equation.append(" + ");
        equation.append(keep10digits(Math.abs(elements[0])));

        System.out.println(equation.toString());
        return equation.toString();
    }

    /**
     * 计算函数方程。
     * 输入x，求出y。
     * @param x
     * @return
     */
    public static double function(double x) throws IOException {
        //如果之前训练过，就可以直接从csv中读取数据，而不必重新训练。
        if (elements == null || elements.length < 1){
            Table table = Table.read().csv(coefficient);
            elements = new double[table.rowCount()];
            for (int i=0; i<elements.length; i++){
                elements[i] = Double.parseDouble(table.column(0).getString(i));
            }
        }

        double y = 0;
        for(int i=0; i<elements.length; i++){
            y += elements[i] * Math.pow(x, i);
        }
        y = y<0 ? 0 : y;
//        y = y>1.2 ? 1.2 : y;
        return y;
    }

    /**
     * 各项系数保留10位小数，避免科学计数法。
     * @param d
     * @return
     */
    public static String keep10digits(double d){
        DecimalFormat format = new DecimalFormat("0.0000000000");
        return format.format(d);
    }
}
