package com.example.hugolucas.cca;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Class will handle the pre-processing of the input image. This class will not classify the image
 * of the banknote, instead it will perform operations on the image in order to make the
 * classification of the banknote easier.
 *
 * Created by hugolucas on 9/25/17.
 */

public class ImagePreprocessor {


    public void findBankNote(Mat image){
        Mat processedImage = new Mat();

        // Convert image to gray-scale
        Imgproc.cvtColor(image, processedImage, Imgproc.COLOR_BGR2GRAY);

        // Threshold the image to bring out the edges
        Imgproc.threshold(processedImage, processedImage, 100.0, 256.0, Imgproc.THRESH_BINARY);

        // Create a list for storing contours
        List<MatOfPoint> contours = new ArrayList<>();

        // Find image contours
        Mat hierarchy = new Mat();
        Imgproc.findContours(processedImage, contours, hierarchy, Imgproc.RETR_CCOMP,
                Imgproc.CHAIN_APPROX_SIMPLE);

        // Iterate through the contours and find the contour with the largest area
        double largestContourArea = 0.0;
        int largestContourIndex = 0;
        for(int i = 0; i < contours.size(); i ++){
            double contourArea = Imgproc.contourArea(contours.get(i));
            if(contourArea > largestContourArea){
                largestContourArea = contourArea;
                largestContourIndex = i;
            }
        }

        // Draw the largest contour
        Rect largestRect = Imgproc.boundingRect(contours.get(largestContourIndex));
    }
}
