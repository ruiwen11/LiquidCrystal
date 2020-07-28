package Fitting;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import tech.tablesaw.api.Table;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class PrepareData {
    private double[] x;
    private double[] y;
    private double[] elements;
    private static BufferedWriter writer = null;

    //从csv文件的数据已表格的形式读入。
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

    public void calculatePoints(String file, int numberOfPoints, double startX, double endX) throws IOException {
        writeToCSV(file,"y,x\n");
        double slot = (endX - startX)/numberOfPoints;
        for (int i=0; i<numberOfPoints; i++){
            double x = startX + slot*i;
            double y = function(x);
            y = y<0 ? 0 : y;
            y = y>1.2 ? 1.2 : y;
            writeToCSV(file, y + "," + x + "\n");
        }
    }

    /**
     * 每次写入数据都要新建一个writer，开销比较大。
     * 这边后期建议改完单例模式。
     * @param file
     * @param content
     */
    //todo
    public static void writeToCSV(String file, String content) throws IOException {
        try {
            if(writer == null){
                writer = new BufferedWriter(new FileWriter(file));
            }
            writer.write(content);
            writer.flush();
        }catch (IOException e) {
            writer.close();
            writer = null;
            e.printStackTrace();
        } finally {
        }
    }

    /**
     * 进行拟合，引入的是commons-math3这个包。
     * 使用很简单，将每一对坐标放进WeightedObservedPoints对象中。
     * 然后new一个多项拟合器PolynomialCurveFitter。fit这些点。
     * 使用的commons-math3版本是3.6.1，还支持正弦拟合和高斯拟合。
     */
    public void train(int degree){
        WeightedObservedPoints points = new WeightedObservedPoints();

        for(int i=0; i<x.length; i++){
            points.add(x[i], y[i]);
        }

        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(degree);
        elements = fitter.fit(points.toList());

        printFormula(elements);
    }

    /**
     * 打印函数方程
     * @param elements
     * @return
     */
    public String printFormula(double[] elements){
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
    public double function(double x){
        double y = 0;
        for(int i=0; i<elements.length; i++){
            y += elements[i] * Math.pow(x, i);
        }

        return y;
    }

    /**
     * 各项系数保留10位小数，避免科学计数法。
     * @param d
     * @return
     */
    public String keep10digits(double d){
        DecimalFormat format = new DecimalFormat("0.0000000000");
        return format.format(d);
    }
}
