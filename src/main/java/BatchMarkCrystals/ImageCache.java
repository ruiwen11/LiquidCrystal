package BatchMarkCrystals;

import Fitting.PrepareData;
import MarkImage.Circle;
import MarkImage.Pixel;
import MarkImage.ReadImage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * 功能描述:
 * 该类提供给BatchMarkCrystal，读取一张图片的各项参数并缓存在内存中。
 * 当需要在屏幕中显示图片的时候，初始化JFrame就行。
 * 因为要实现双缓存必须重写JFrame中的paint方法，因此本类必须继承JFrame，而不能依赖JFrame。
 *
 * @Author: 小芮芮
 * @Date: 2021/2/3 9:42
 */
public class ImageCache extends JFrame{
    //图片基础参数
    private String routine;     //图片路径
    private String imageName;   //图片的名字
    private int width;          //图片宽
    private int height;         //图片长
    private ImageIcon image;    //图片缓存
    private Pixel[][] pixels;   //用ReadImage中提供的方法获取整个图片的所有像素，以便查找圆内的所有像素。

    //JFrame画图需要的参数
    private int x1, y1, x2, y2;                 //鼠标按下和释放时的两个点。
    private int centerX, centerY, radius;       //计算出来的圆的属性。
    public ArrayList<Circle> paintedCircles;   //将曾经画过的圆保存起来。
    private Image doubleBuffer;                 //第二缓存

    //保存标注数据的csv文件
//    private static final String CSV_OF_HUE = DrawFunctionPicture.CSVOfHue;

    /**
     * 构造函数，初始化图片的各项参数
     * @param routine 图片路径
     */
    public ImageCache(String routine) {
        this.image = new ImageIcon(routine);
        this.width = image.getIconWidth();
        this.height = image.getIconHeight();
        this.routine = routine;

        //从路径中解析出文件名
        String[] tmp = routine.split("\\\\");
        //去除后缀名
        this.imageName = tmp[tmp.length-1].split("\\.")[0];

        pixels = ReadImage.getImagePixel(routine);
        paintedCircles = new ArrayList<>();

        //添加窗口监听器，只实现了一个接口，就是关闭窗口时点选是否保存图片
        super.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //跳出弹框，点选是否另存图片
                int yes = JOptionPane.showConfirmDialog(null, "是否保存标注信息并另存图片？", "提示", JOptionPane.YES_NO_OPTION);
                if (yes == JOptionPane.YES_OPTION){
                    saveAs();
                }
            }
        });

        //添加鼠标监听器，当发生鼠标操作时触发。
        super.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {}

            /**
             * 监听到按下鼠标的动作。此时，鼠标坐标就是圆的初始坐标。
             * @param e 鼠标按住操作
             */
            @Override
            public void mousePressed(MouseEvent e) {
                x1 = e.getX();
                y1 = e.getY();
            }
            /**
             * 监听到鼠标释放的动作。说明圆已经画完了。该动作与鼠标点击组合就可以画圆。
             * @param e 鼠标释放操作
             */
            @Override
            public void mouseReleased(MouseEvent e) {
                x2 = e.getX();
                y2 = e.getY();
                prepareCircle(x1, y1, x2, y2);
                //计算圆内所有像素点的平均hue值
                double hue = ReadImage.getAverageHue(getPixelsOfCrystal(centerX, centerY, radius));
                //保存已经画出来的圆，这样画第二个圆的时候，之前的圆就不会被清除。
                paintedCircles.add(new Circle(centerX, centerY,radius, PrepareData.keep2digits(hue)));
                repaint();

                //标注完液晶之后，跳出一个弹框提示是否保存液晶。
//                int yes = JOptionPane.showConfirmDialog(null, "是否将该标注保存到csv文件中？", "", JOptionPane.YES_NO_OPTION);
//                if (yes == JOptionPane.YES_OPTION) {
//                    //根据圆内的所有像素点计算平均hue值
//                    try {
//                        //根据hue值计算功率，并写入csv文件中
//                        PrepareData.writeToCSV(CSV_OF_HUE, hue+","+PrepareData.function(hue)+"\n");
//                    } catch (IOException ioException) {
//                        ioException.printStackTrace();
//                    }
//                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });

        /**
         * 为了实现显示画图轨迹的功能，画板必须实现另一个接口，MouseMotionListener。
         * 里面声明了两个方法。mouseDragged和mouseMoved。
         */
        super.addMouseMotionListener(new MouseMotionAdapter() {
            /**
             * 鼠标按下并拖动时触发
             * @param e 鼠标拖动操作
             */
            @Override
            public void mouseDragged(MouseEvent e) {
                x2 = e.getX();
                y2 = e.getY();
                prepareCircle(x1, y1, x2, y2);
                //刷新缓存
                repaint();
            }
            @Override
            public void mouseMoved(MouseEvent e) {}
        });
    }


    /**
     * 初始化JFrame，在屏幕中展示图片
     */
    public void showImage(){
        super.setSize(width, height);
        super.add(new JLabel(image));
        super.setVisible(true);
        super.setResizable(false);
        super.setTitle(imageName + " Marking……");
        recoverMarks(this.getGraphics(), 0, 0);
    }


    /**
     * 另存图片的标注信息，默认文件名为：原文件名+已标注
     */
    public void saveAs(){
        String saveRoutine = routine
                //已标注的图片保存在MarkedPictures目录下。
                .replace("UnMarkedPictures", "MarkedPictures")
                //修改文件名。
                .replace(imageName, imageName+"已标注");
        PrepareData.savePicture(this, saveRoutine);
    }


    /**
     * 重写JFrame的paint方法，也就是画图的方法，来实现双缓存。
     * @param g
     */
    @Override
    public void paint(Graphics g){
        if (doubleBuffer == null){   //初始化第二缓存
            doubleBuffer = this.createImage(width, height);
        }
        Graphics gImage = doubleBuffer.getGraphics();   //获取第二缓存的画笔，我们就是用这个对象来画圆和直径的。
        //将液晶图片放到第二缓存上。
        super.paint(gImage);
        //画直径。
//        gImage.drawLine(x1, y1, x2, y2);
        //画圆
        gImage.drawOval(centerX-radius, centerY-radius, radius*2, radius*2);

        //恢复历史标注
        recoverMarks(gImage, 0, 0);

        //交替当前屏幕上的图片和第二缓存。
        g.drawImage(doubleBuffer, 0, 0, this);
    }

    /**
     * 因为每次拖动鼠标时，都会先把图片清空，根据鼠标被拖到的位置再画一个圆。
     * 这样之前的标注也会被清除。我们把之前的圆保存在数组里，每次画图时都把他们重新画出来。
     */
    public void recoverMarks(Graphics gImage, int offsetX, int offsetY){
        for(Circle circle : paintedCircles){
            gImage.drawOval(circle.centerX-circle.radius-offsetX, circle.centerY-circle.radius-offsetY, circle.radius*2, circle.radius*2);
        }
        //保存标注hue值
        for (Circle circle : paintedCircles){
            Font font = new Font("宋体", Font.PLAIN, 15);
            gImage.setFont(font);
            gImage.setColor(Color.red);

            //计算文字长度和宽度
            FontMetrics fm = gImage.getFontMetrics(font);
            int txtWidth = fm.stringWidth(circle.hue);
            int txtHeight = fm.getHeight();

            //标注
            gImage.drawString(circle.hue, circle.centerX-txtWidth/2-offsetX, circle.centerY+txtHeight/2-offsetY);
        }
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
}
