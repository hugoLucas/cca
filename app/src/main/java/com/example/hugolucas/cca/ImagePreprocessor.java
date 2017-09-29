package com.example.hugolucas.cca;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Rect;
import org.opencv.core.Point;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.*;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.imread;

/**
 * Class will handle the pre-processing of the input image. This class will not classify the image
 * of the banknote, instead it will perform operations on the image in order to make the
 * classification of the banknote easier.
 *
 * Created by hugolucas on 9/25/17.
 */

public class ImagePreprocessor {

    private final String TAG = "cca.preprocessor";

    public Mat loadImage(String path){
        Mat image = imread(path);
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB);

        Log.v(TAG, "Image Rows: " + image.rows());
        Log.v(TAG, "Image Columns: " + image.cols());
        if (image.rows() < image.cols())
            Core.flip(image.t(), image, 1);

        return image;
    }

    public Mat findBankNote(Mat image){
        Mat processedImage = new Mat();

        // Convert image to gray-scale
        Imgproc.cvtColor(image, processedImage, Imgproc.COLOR_BGR2GRAY);

        // Blur the image
        Imgproc.blur(processedImage, processedImage, new Size(3, 3));

        // Threshold the image to bring out the edges
        Imgproc.threshold(processedImage, processedImage, 120.0, 256.0, Imgproc.THRESH_BINARY);

        // Edge Detection
        // Mat edges = new Mat(processedImage.size(), CvType.CV_8UC1);
        // Imgproc.Canny(processedImage, edges, 70, 100);

        // Create a list for storing contours
        List<MatOfPoint> contours = new ArrayList<>();

        // Find image contours
        Mat hierarchy = new Mat();
        Imgproc.findContours(processedImage, contours, hierarchy, Imgproc.RETR_TREE,
                Imgproc.CHAIN_APPROX_NONE);

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
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(largestContourIndex).toArray());
        double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
        Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
        MatOfPoint points = new MatOfPoint(approxCurve.toArray());
        Rect rectangle = Imgproc.boundingRect(points);
        Imgproc.rectangle(image, new Point(rectangle.x, rectangle.y),
                new Point(rectangle.x + rectangle.width, rectangle.y + rectangle.height),
                new Scalar(0, 255, 0), 3);

        return image;
    }
}
