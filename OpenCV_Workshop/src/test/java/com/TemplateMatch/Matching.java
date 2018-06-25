package com.TemplateMatch;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


// http://docs.opencv.org/2.4/doc/tutorials/imgproc/histograms/template_matching/template_matching.html#template-matching
public class Matching {

    public static int[] getMatchingLocation(String... args) throws Exception {
        // Read source image and template image
        Mat src = Imgcodecs.imread(args[0], 0);
        Mat tmp = Imgcodecs.imread(args[1], 0);

        // Create the result matrix
        int result_cols = src.cols() - tmp.cols() +1;
        int result_rows = src.rows() - tmp.rows() +1;
        Mat result = new Mat(result_cols,result_rows, CvType.CV_32FC1);



        // Match Template Function from OpenCV
        int method = Imgproc.TM_SQDIFF_NORMED; // select a matching method
        //*****     matching and get the result matrix     ********

        //****     get the min and max  values and their locations    *****
        Core.MinMaxLocResult mmr = null;



        // Got location and match percentage
        Point matchLoc;
        double matchPercentage;
        if(method == Imgproc.TM_SQDIFF_NORMED || method == Imgproc.TM_SQDIFF){
            matchLoc =  mmr.minLoc;
            matchPercentage = 1-mmr.minVal;
        }
        else {
            matchLoc =  mmr.maxLoc;
            matchPercentage = mmr.maxVal;
        }


        //show what we got
        //***** get the endPoint  *****
        Point endPoint;
        //***** draw a rectangle from the matchLoc to endPoint;   *****

        //write the compare result
        Imgcodecs.imwrite("compare/lena_face.png", src);



        //return location
        Double x1 = matchLoc.x;
        Double y1 = matchLoc.y;
        int x = x1.intValue() + tmp.cols()/2;
        int y = y1.intValue() + tmp.rows()/2;

        System.out.println(matchPercentage);
        if(matchPercentage > 0.95) {
            return new int[]{x, y};
        }
        else{
            return null;
        }
    }
}
