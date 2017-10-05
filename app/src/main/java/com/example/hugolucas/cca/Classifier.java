package com.example.hugolucas.cca;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;

/**
 * The Classifier class will handle the classification of the banknote image. By the end
 * of the execution of this class the Classifier should return the denomination and country of
 * origin of the banknote.
 *
 * Created by hugo on 10/5/17.
 */

public class Classifier {

    public void classify(Mat image){
        MatOfKeyPoint keyPoints = detectFeatures(image);
        MatOfKeyPoint descriptors = getDescriptors(image, keyPoints);

        Mat output = drawKeyPoints(image, keyPoints);
    }

    /**
     * Applies a Scale-Invariant Feature Transform (SIFT) to the input image.
     *
     * @param image     the image to apply SIFT to
     * @return          a MatOfKeyPoint representing the features detected
     */
    public MatOfKeyPoint detectFeatures(Mat image){
        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.SIFT);
        detector.detect(image, keyPoints);

        return keyPoints;
    }

    public MatOfKeyPoint getDescriptors(Mat image, MatOfKeyPoint keyPoints){
        MatOfKeyPoint descriptors = new MatOfKeyPoint();
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
        extractor.compute(image, keyPoints, descriptors);

        return descriptors;
    }

    public void featureMatching(){

    }

    public Mat drawKeyPoints(Mat image, MatOfKeyPoint keyPoints){
        Mat outputImage = image.clone();
        Features2d.drawKeypoints(image, keyPoints, outputImage, new Scalar(255, 0, 0),
                Features2d.DRAW_OVER_OUTIMG);

        return outputImage;
    }
}
