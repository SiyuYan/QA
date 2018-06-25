package com.PictureSmooth;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Smooth {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void smooth(String filename){
        Mat image=new Mat();
        Mat compareImage = image.clone();
        image= Imgcodecs.imread(filename);
        if(image!=null){
            Imgproc.blur(image,compareImage,new Size(9,9));
            Imgcodecs.imwrite("compare/smoothImage.png", compareImage);
            System.out.println("Smooth Done!");
        }
    }

    public static void main(String[] args){
        String filename = "temp/lena1.png";
        smooth(filename);
    }
}
