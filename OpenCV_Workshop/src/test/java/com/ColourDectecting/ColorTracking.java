package com.ColourDectecting;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;

// http://docs.opencv.org/2.4/doc/tutorials/imgproc/shapedescriptors/bounding_rects_circles/bounding_rects_circles.html#bounding-rects-circles
public class ColorTracking {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    static Mat imag = null;

    public static void main(String[] args) {
        JFrame jframe = new JFrame("Color Tracking");  //create a new frame
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //exit action
        JLabel vidpanel = new JLabel(); //create a lable
        jframe.setContentPane(vidpanel);
        jframe.setSize(640, 480);
        jframe.setVisible(true);
        Mat frame = new Mat();
        Mat outerBox = new Mat();
        VideoCapture camera = new VideoCapture(0);
        Size sz = new Size(640, 480);

        while (true) {
            if (camera.read(frame)) {
                Imgproc.resize(frame, frame, sz);
                imag = frame.clone();

                outerBox = new Mat();

                Imgproc.cvtColor(frame, outerBox, Imgproc.COLOR_BGR2HSV);

                Mat yellow = new Mat();
                Mat red = new Mat();
                Mat blue = new Mat();
                Mat colors = new Mat();
                Core.inRange(outerBox,new Scalar(20,100,100), new Scalar(30, 255, 255), yellow);
                Core.inRange(outerBox,new Scalar(160,70,150), new Scalar(185, 255, 255), red);
                Core.inRange(outerBox,new Scalar(85,45,180), new Scalar(110, 80, 220), blue);
                Core.addWeighted(yellow,1,red,1,0,colors);
                Core.addWeighted(colors,1,blue,1,0,colors);

                Imgproc.GaussianBlur(colors,colors,new Size(9,9),0,0);

                detection_contours(colors);

                ImageIcon image = new ImageIcon(Mat2bufferedImage(imag));
                vidpanel.setIcon(image);
                vidpanel.repaint();

            }
        }
    }

    public static BufferedImage Mat2bufferedImage(Mat image) {
        MatOfByte bytemat = new MatOfByte();
        Imgcodecs.imencode(".jpg", image, bytemat);
        byte[] bytes = bytemat.toArray();
        InputStream in = new ByteArrayInputStream(bytes);
        BufferedImage img = null;
        try {
            img = ImageIO.read(in);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return img;
    }

    public static ArrayList<Rect> detection_contours(Mat outmat) {
        Mat v = new Mat();
        Mat vv = outmat.clone();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(vv, contours, v, Imgproc.RETR_LIST,
                CHAIN_APPROX_SIMPLE);

        double maxArea = 4000;
        int maxAreaIdx = -1;
        Rect r = null;
        ArrayList<Rect> rect_array = new ArrayList<Rect>();

        for (int idx = 0; idx < contours.size(); idx++) {
            Mat contour = contours.get(idx);
            double contourarea = Imgproc.contourArea(contour);
            if (contourarea > maxArea) {
                // maxArea = contourarea;
                maxAreaIdx = idx;
                r = Imgproc.boundingRect(contours.get(maxAreaIdx)); //Calculates the up-right bounding rectangle of a point set
                rect_array.add(r);;
//                Imgproc.rectangle(imag,r.tl(),r.br(),new Scalar(0,0,255),3,8,0);
                Imgproc.drawContours(imag,contours,idx,new Scalar(0,0,255),3);
            }
        }
        v.release();

        return rect_array;
    }
}
