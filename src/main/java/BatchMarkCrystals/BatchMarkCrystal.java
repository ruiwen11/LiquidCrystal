package BatchMarkCrystals;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 更新：读取某个文件夹路径下的全部图片，并另存标注的信息。
 *
 * @author 小芮芮
 * @date 2021.2.2
 */
public class BatchMarkCrystal {
    //文件夹路径
    public static final String IMAGE_ROUTINE = "src\\main\\resources\\UnMarkedPictures";

    private List<ImageCache> batchImages = new ArrayList<>();    //如果需要批量读入图片并处理，就需要一个数组

    private int imageIndex;
    private int imageCount;
    private ImageCache currentImage;

    public BatchMarkCrystal(String routine) {
        File[] pictures = new File(routine).listFiles();    //将该路径下的所有文件读入内存中
        for (File picture : pictures){
            String path = picture.getPath();
            batchImages.add(new ImageCache(path));
        }

        //初始化时，展示第一张图片
        imageIndex = 0;
        imageCount = pictures.length;
        currentImage = batchImages.get(imageIndex);
        currentImage.showImage();

        //选择下一张图片的按钮
        JButton nextOne = new JButton("下一张");
        nextOne.setSize(100, 200);
        nextOne.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (imageIndex == imageCount-1){
                    //已是最后一张图片，跳出提示框
                    JOptionPane.showConfirmDialog(null, "已是最后一张图片", "", JOptionPane.DEFAULT_OPTION);
                } else {
                    //展示下一张图片
                    //todo 关闭当前图片
                    currentImage.dispose();
                    imageIndex++;
                    batchImages.get(imageIndex).showImage();
                    currentImage = batchImages.get(imageIndex);
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });

        //选择上一张页面的按钮
        JButton lastOne = new JButton("上一张");
        lastOne.setSize(100, 200);
        lastOne.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (imageIndex == 0){
                    //已是第一张图片，跳出提示框
                    JOptionPane.showConfirmDialog(null, "已是第一张图片", "", JOptionPane.DEFAULT_OPTION);
                } else {
                    //展示上一张图片
                    //todo 关闭当前图片
                    currentImage.dispose();
                    imageIndex--;
                    batchImages.get(imageIndex).showImage();
                    currentImage = batchImages.get(imageIndex);
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });

        JPanel panel = new JPanel();
        panel.add(nextOne);
        panel.add(lastOne);

        JFrame frame = new JFrame("选择图片");
        frame.add(panel);
        frame.setSize(300, 200);
        frame.setVisible(true);
        frame.setResizable(false);
        //选择框置于屏幕中央
        frame.setLocationRelativeTo(null);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //保存所有被标注了的图片
                for (ImageCache image : batchImages){
                    if (image.paintedCircles.size() > 0){
                        image.saveAs();
                    }
                }
                //退出程序
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) throws Exception {
        //调用Main的构造函数，第35行jFrame.setVisible(true)会将画板展示出来。
        new BatchMarkCrystal(IMAGE_ROUTINE);
    }

}
