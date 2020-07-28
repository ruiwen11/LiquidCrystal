package MarkImage; /**
 * @author 小芮芮
 * @date 2020.7.2
 * 画圆的逻辑：捕捉两个鼠标动作，一个是按下鼠标，一个是释放鼠标。
 * 连接按下鼠标和释放鼠标时鼠标所在的两个点，画出的线就是圆的直径。
 * 方法getPixelsOfCrystal将会返回圆内的所有像素点。
 * 需要计算功率的话，在第104行调用一个计算功率的接口。
 */

import Fitting.PrepareData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {
    //图片路径
    static String imageRoutine = "src/main/resources/liquid2.png";
    static String CSVOfHue = "src/main/resources/hue.csv";
    private int width;  //图片的宽
    private int height; //图片的长
    private int centerX, centerY, radius;   //计算出来的圆的属性。
    private int x1, y1, x2, y2; //鼠标按下和释放时的两个点。
    private Pixel[][] pixels;   //用ReadImage中提供的方法获取整个图片的所有像素，以便查找圆内的所有像素。
    private ArrayList<Integer> hues;    //保存每次标注得出的hue值
    //不再新建画板，而是直接由本类继承jFrame，那么本类就获得了jFrame的所有属性。
    //JFrame jFrame;      //画板，图片会被放置到该画板上，画圆也是在该画板上操作。
    private ImageIcon image;    //将图片保存为ImageIcon类的对象，便于操作。

    /**
     * 该image作为图片的缓存，保证画圆的轨迹流畅，避免闪屏。
     * 为什么会闪屏：我们鼠标拖动时，后台会根据鼠标经过的点，并重新圆的轨迹。
     * 这部分算法有一定的时间复杂度。我们必须先清空原来的圆，再计算新的圆，再展示。
     * 所以中间有一段时间无法展示圆，出现了闪屏。
     *
     * 解决方法：双缓存技术。
     * 原理详见：https://blog.csdn.net/weixin_44552215/article/details/98748436
     * 原本我们在内存中，只有一张图片的缓存，也就是26行的对象image。
     * 现在我们用doubleBuffer作为第二缓存。两张图片交替展示。
     * 当在一张图片上计算圆轨迹时，我们就展示另一张图片。
     * 当算法计算完毕并画出圆时，我们就展示这张图片。然后在另一张图片上重新开始计算。
     */
    Image doubleBuffer;

    /**
     * 接下来这个数组，是我自己的思路，效果不好，被注释了。
     * 也是缓存的思路：用一个数组记录鼠标拖动时经过的点。
     * 鼠标拖动之后不会立刻重新画圆，而是保持原来的圆不变，然后后台计算新的圆。
     * 当缓存大小大于2，也就是鼠标经过了两个点之后重新画圆。
     *
     * 同样是缓存的原理，但效果不好，因为拖动的速度有快有慢，想要保证圆的流畅，
     * 必须修改缓存的数量，这很难控制。
     * @param routine
     * @throws Exception
     */
    //ArrayList<Point> passedPoints = new ArrayList<>();

    public Main(String routine) throws Exception {
        image = new ImageIcon(routine); //读取图片
        pixels = ReadImage.getImagePixel(routine);  //获取图片的像素矩阵。
       // hues = new ArrayList<>();

        width = image.getIconWidth();
        height = image.getIconHeight();
        super.setSize(width, height);  //画板尺寸与图片尺寸相同，那么像素在图片上的位置就是在画板上的位置，便于坐标的计算。
        super.add(new JLabel(image));  //将图片放到画板上。
        super.setVisible(true);        //显示画板。
        super.setResizable(false);

        super.addWindowListener(new WindowAdapter() {
            /**
             * 添加一个窗口监听器。当窗口关闭时，调用System.exit结束程序。
             * @param e
             */
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        //对画板增加一个监听器MouseListener，监听鼠标动作。
        //MouseListener是一个接口，其中方法的具体逻辑需要我们自己实现。
        super.addMouseListener(new MouseListener() {
            /**
             * 监听到鼠标点击的动作。鼠标点击时，清空画板和之前标注的数据。
             * @param e
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                clearData();//清楚标注数据。
                int x = e.getX();
                int y = e.getY();//用点击的坐标去查询像素矩阵。
                System.out.println("坐标(" + x + ", " + y + "): " + pixels[y][x]);
            }
            /**
             * 监听到按下鼠标的动作。此时，鼠标坐标就是圆的初始坐标。
             * @param e
             */
            @Override
            public void mousePressed(MouseEvent e) {
                clearData();
                x1 = e.getX();
                y1 = e.getY();
            }
            /**
             * 监听到鼠标释放的动作。说明圆已经画完了。该动作与鼠标点击组合就可以画圆。
             * @param e
             */
            @Override
            public void mouseReleased(MouseEvent e) {
                x2 = e.getX();
                y2 = e.getY();
                prepareCircle(x1, y1, x2, y2);
//                getGraphics().drawOval(centerX-radius, centerY-radius, radius*2, radius*2);
                //repaint();  //该方法会清空原来画的圆，并在底层调用163行的paint方法。
                //将最终画成的圆内的像素点全部加入一个数组中并返回。
                print(getPixelsOfCrystal(centerX, centerY, radius));

                //标注完液晶之后，跳出一个弹框提示是否保存液晶。
                int yes = JOptionPane.showConfirmDialog(null, "是否保存该液晶？", "提示", JOptionPane.YES_NO_OPTION);
                if(yes == JOptionPane.YES_OPTION){
                    int hue = ReadImage.getHue(getPixelsOfCrystal(centerX, centerY, radius));
                    try {
                        PrepareData.writeToCSV(CSVOfHue, hue+"\n");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }

                }
            }
            /**
             * 下面两个方法暂时用不到，没有实现逻辑。
             * mouseEntered，鼠标进入组件（我们只有一个组件，就是图片），但没有任何操作时触发。
             * mouseExited，鼠标离开组件时触发。
             * @param e
             */
            @Override
            public void mouseEntered(MouseEvent e) {
            }
            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        /**
         * 为了实现显示画图轨迹的功能，画板必须实现另一个接口，MouseMotionListener。
         * 里面声明了两个方法。mouseDragged和mouseMoved。
         */
        super.addMouseMotionListener(new MouseMotionAdapter() {
            /**
             * 鼠标按下并拖动时触发
             * @param e
             */
            @Override
            public void mouseDragged(MouseEvent e) {
                x2 = e.getXOnScreen();
                y2 = e.getYOnScreen();
//                Point point = new Point(x2, y2);
//                //passedPoints是鼠标拖动时经过的点。保存这些点为了实现缓冲效果。
//                //拖动鼠标，但圆是无法改变的，只能把圆清除，再重新画一个。
//                //但是重画的时候，需要计算圆的直径，有一定延时
//                if(passedPoints.size() < 2){
//                    passedPoints.add(point);
//                    drawCircle(x1, y1, x2, y2);
//                } else {
//                    passedPoints.clear();
//                    passedPoints.add(point);
                prepareCircle(x1, y1, x2, y2);
                repaint();
                //}
            }
            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });
    }

    /**
     * 重载JFrame的paint方法，也就是画图的方法，来实现双缓存。
     * @param g
     */
    @Override
    public void paint(Graphics g){
        if (doubleBuffer == null)   //初始化第二缓存
            doubleBuffer = this.createImage(width, height);
        Graphics gImage = doubleBuffer.getGraphics();   //获取第二缓存的画笔，我们就是用这个对象来画圆和直径的。
        //将液晶图片放到第二缓存上。
        super.paint(gImage);
        //画圆和直径。
        gImage.drawLine(x1, y1, x2, y2);
        gImage.drawOval(centerX-radius, centerY-radius, radius*2, radius*2);
        //交替当前屏幕上的图片和第二缓存。
        g.drawImage(doubleBuffer, 0, 0, this);
    }

    /**
     * 计算圆的属性
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    private void prepareCircle(int x1, int y1, int x2, int y2){
        //计算圆心坐标和半径
        this.centerX = (x1 + x2)/2;
        this.centerY = (y1 + y2)/2;
        this.radius = (int)(calculateDistance(x1, y1, x2, y2)/2);
    }

    /**
     * 从像素矩阵中找到圆内的所有像素。
     * @param centerX
     * @param centerY
     * @param radius
     * @return
     */
    public ArrayList<Pixel> getPixelsOfCrystal(int centerX, int centerY, int radius){
        ArrayList<Pixel> crystalPixel = new ArrayList<>();

        //该圆最上面的点坐标为(centerX, centerY-radius)
        //然后遍历顶点所在行的 下一行，找出所有在圆中的像素。
        //然后再遍历下一行，直到该行不再经过圆。
        //判断依据是该点到圆心的距离小于半径。
        int topY = centerY - radius;
        crystalPixel.add(pixels[centerX][topY]);
        //外循环确定行，也就是height。
        for(int i=1; i<radius*2; i++){
            //内循环遍历该行中的所有像素，判断其是否在圆内。
            for(int j=0; j<width; j++){
                if(isInCircle(j,topY+i, centerX, centerY, radius))
                    crystalPixel.add(pixels[topY+i][j]);
            }
        }
        return crystalPixel;
    }

    /**
     * 判断坐标(x, y)是否在圆内。
     * @param x
     * @param y
     * @param centerX
     * @param centerY
     * @param radius
     * @return
     */
    private boolean isInCircle(int x, int y, int centerX, int centerY, int radius){
        double distanceFromCenter = calculateDistance(x, y, centerX, centerY);
        //简写
        return !(distanceFromCenter > radius);
    }

    /**
     * 计算两点(x1, y1)，(x2, y2)的距离。
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    private double calculateDistance(int x1, int y1, int x2, int y2){
        int tmpX = (x1>x2) ? x1-x2 : x2-x1;
        int tmpY = (y1>y2) ? y1-y2 : y2-y1;
        return Math.sqrt(tmpX*tmpX + tmpY*tmpY);
    }

    private void clearData(){
        this.x1 = -1;
        this.y1 = -1;
        this.x2 = -1;
        this.y2 = -1;
        this.centerX = -1;
        this.centerY = -1;
        this.radius = -1;
    }

    public void print(List list){
        for (Object object : list)
            System.out.println(object);
    }

    public static void main(String[] args) throws Exception {
        //调用Main的构造函数，第35行jFrame.setVisible(true)会将画板展示出来。
        new Main(imageRoutine);
    }

}
