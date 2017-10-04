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
 * Performs the following operations for pre-processing:
 *  1. Removes noise using bilateral filter
 *  2. Applies a Contrast Limited Adaptive Histogram Equalizer (CLAHE) to increase contrast
 *
 * Algorithm taken from:  Recognition of Banknotes in Multiple Perspectives Using Selective Feature
 * Matching and Shape Analysis
 *
 * Created by hugolucas on 9/25/17.
 */

public class ImagePreprocessor {

    private final String TAG = "cca.preprocessor";

    /**
     * Method coordinates all the pre-processing steps of the image. First it finds the largest
     * rectangular contour in order to locate the banknote. Next it takes the banknote and applies
     * the pre-processing steps outlined in the paper outlining the classification algorithm.
     *
     * @param path      String path to the image taken by the application
     * @return          returns a Mat of the banknote that has been filtered and equalized
     */
    public Mat preprocessImage(String path){
        /* Load image into application */
        Mat image = loadImage(path);

        /* Extract the banknote from the image */
        Mat bankNote = findBankNote(image);

        /* Remove image noise */
        Mat filteredImage = removeImageNoise(bankNote);

        /* Prepare image for classification */
        Mat equalized = equalize(filteredImage);

        return equalized;
    }

    /**
     * Applies a Contrast Limited Adaptive Histogram Equalizer to a Mat that has been filtered.
     *
     * @param image     filtered Mat image
     * @return          equalized Mat
     */
    private Mat equalize(Mat image){
        Mat equalized = new Mat();

        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
        CLAHE claghe = Imgproc.createCLAHE(2.0, new Size(8, 8));
        claghe.apply(image, equalized);

        return equalized;
    }

    /**
     * Applies a bilateral filter to the image in order to reduce noise.
     *
     * @param image     Mat to be filtered, banknote preferably
     * @return          filtered Mat
     */
    private Mat removeImageNoise(Mat image){
        Mat filterdImage = new Mat();
        int bilateralFilterDistance = 8, bilateralFilterSigmaColor = 18,
                bilateralFilterSigmaSpace = 12;
        Imgproc.bilateralFilter(image, filterdImage, bilateralFilterDistance,
                bilateralFilterSigmaColor, bilateralFilterSigmaSpace);

        return filterdImage;
    }

    /**
     * Loads image given a path to the image on the Android device.
     *
     * @param path      path to image file
     * @return          a Mat of the image file
     */
    private Mat loadImage(String path){
        Mat image = imread(path);
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB);

        Log.v(TAG, "Image Rows: " + image.rows());
        Log.v(TAG, "Image Columns: " + image.cols());
        if (image.rows() < image.cols())
            Core.flip(image.t(), image, 1);

        return image;
    }

    /**
     * Finds banknote in image via blurring, thresholding, contour finding, and cropping.
     *
     * @param image     image containing banknote
     * @return          Mat of bounding rectangle of banknote
     */
    private Mat findBankNote(Mat image){
        Mat processedImage = image.clone();
        processedImage = blurAndThreshold(processedImage);
        List<MatOfPoint> imageContours = findAllContours(processedImage);
        int largestContourIndex = findLargestContour(imageContours);

        // drawLargestContour(imageContours, largestContourIndex, image);
        return extractLargestContour(imageContours, largestContourIndex, image);
    }

    /**
     * Using a list of contours previously calculated, the index of the largest contour by area,
     * and the original un-processed image this method will extract the largest rectangle found in
     * the image. This rectangle should hopefully be the banknote needing classification.
     *
     * @param contourList           a list of contours
     * @param largestContourIndex   the index which contains the largest contour by area
     * @param image                 the unprocessed image used to generate the contour list
     * @return                      a Mat reference to the portion of the image containing the
     *                              largest contour
     */
    private Mat extractLargestContour(List<MatOfPoint> contourList, int largestContourIndex,
                                      Mat image){

        MatOfPoint2f approxCurve = new MatOfPoint2f();
        MatOfPoint2f contour2f = new MatOfPoint2f(contourList.get(largestContourIndex).toArray());
        double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
        Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
        MatOfPoint points = new MatOfPoint(approxCurve.toArray());
        Rect rectangle = Imgproc.boundingRect(points);

        return new Mat(image, rectangle);
    }

    /**
     * Looks through a list of contours and finds the largest contour by area.
     *
     * @param contourList   a MatOfPoint list containing all contours found inside an image
     * @return              an index inside contourList that contains the largest contour by area
     */
    private int findLargestContour(List<MatOfPoint> contourList){
        double largestContourArea = 0.0;
        int largestContourIndex = 0;

        for(int i = 0; i < contourList.size(); i ++){
            double contourArea = Imgproc.contourArea(contourList.get(i));
            if(contourArea > largestContourArea){
                largestContourArea = contourArea;
                largestContourIndex = i;
            }
        }
        return largestContourIndex;
    }

    /**
     * Finds all the contours of the image.
     *
     * @param image     binary image
     * @return          a list of MatOfPoint that contains all the thresholds located
     */
    private List<MatOfPoint> findAllContours(Mat image){
        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_CCOMP,
                Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }

    /**
     * Converts the image to gray-scale and applies a Gaussian blur with a 3x3 kernel. After the
     * image has been blurred the image is made into a binary image via thresholding.
     *
     * @param image     input image
     * @return          image after being blurred and thresholded
     */
    private Mat blurAndThreshold(Mat image){
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(image, image, new Size(5, 5));
        Imgproc.threshold(image, image, 100, 256.0, Imgproc.THRESH_BINARY);

        return image;
    }

    /**
     * Utility function used for debugging the banknote extraction process. Using a list of contours
     * previously calculated, the index of the largest contour by area, and the original
     * un-processed image this method will draw the largest rectangle found in the image. This
     * rectangle should hopefully be the banknote needing classification.
     *
     * @param contourList           a list of contours
     * @param largestContourIndex   the index which contains the largest contour by area
     * @param image                 the unprocessed image used to generate the contour list
     */
    private void drawLargestContour(List<MatOfPoint> contourList, int largestContourIndex,
                                    Mat image){

        MatOfPoint2f approxCurve = new MatOfPoint2f();
        MatOfPoint2f contour2f = new MatOfPoint2f(contourList.get(largestContourIndex).toArray());
        double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
        Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
        MatOfPoint points = new MatOfPoint(approxCurve.toArray());
        Rect rectangle = Imgproc.boundingRect(points);
        Imgproc.rectangle(image, new Point(rectangle.x, rectangle.y),
                new Point(rectangle.x + rectangle.width, rectangle.y + rectangle.height),
                new Scalar(0, 255, 0), 3);
    }
}
