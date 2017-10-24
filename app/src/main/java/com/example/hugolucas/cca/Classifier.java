package com.example.hugolucas.cca;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.highgui.Highgui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * The Classifier class will handle the classification of the banknote image. By the end
 * of the execution of this class the Classifier should return the denomination and country of
 * origin of the banknote.
 *
 * Created by hugo on 10/5/17.
 */

public class Classifier {

    private static String TAG = "hugo.Classifier";

    private Context mContext;

    public Classifier(Context c){
        mContext = c;
    }

    public String classify(Mat image){
        MatOfKeyPoint keyPoints = detectFeatures(image);
        MatOfKeyPoint descriptors = getDescriptors(image, keyPoints);

        /*Mat output = drawKeyPoints(image, keyPoints);*/
        return featureMatching(descriptors);
    }

    /**
     * Applies a Scale-Invariant Feature Transform (SIFT) to the input image.
     *
     * @param image     the image to apply SIFT to
     * @return          a MatOfKeyPoint representing the features detected
     */
    private MatOfKeyPoint detectFeatures(Mat image){
        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.SIFT);
        detector.detect(image, keyPoints);

        return keyPoints;
    }

    private MatOfKeyPoint getDescriptors(Mat image, MatOfKeyPoint keyPoints){
        MatOfKeyPoint descriptors = new MatOfKeyPoint();
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
        extractor.compute(image, keyPoints, descriptors);

        return descriptors;
    }

    private String featureMatching(MatOfKeyPoint targetDescriptors){
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);

        String [] fileNames = loadImageDatabase();
        int bestFit = -1;
        String bestFitFilename = null;

        if (fileNames == null){
            return null;
        }else {
            for (String fileName : fileNames) {
                List<MatOfDMatch> matches = new LinkedList<>();

                Mat image = loadImageAsset(fileName);

                Log.v(TAG, fileName + " cols = " + image.cols());
                Log.v(TAG, fileName + " rows = " + image.rows());

                MatOfKeyPoint keyPoints = detectFeatures(image);
                MatOfKeyPoint databaseDescriptors = getDescriptors(image, keyPoints);

                matcher.knnMatch(targetDescriptors, databaseDescriptors, matches, 2);

                LinkedList<DMatch> good_matches = new LinkedList<>();
                for (Iterator<MatOfDMatch> iterator = matches.iterator(); iterator.hasNext();) {
                    MatOfDMatch matOfDMatch = iterator.next();
                    if (matOfDMatch.toArray()[0].distance / matOfDMatch.toArray()[1].distance < 0.9) {
                        good_matches.add(matOfDMatch.toArray()[0]);
                    }
                }

                if (good_matches.size() > bestFit){
                    bestFit = good_matches.size();
                    bestFitFilename = fileName;
                }
            }

            Log.v(TAG, "*** BEST FIT ***");
            Log.v(TAG, bestFitFilename + " " + bestFit);
            Log.v(TAG, "*** BEST FIT ***");

            return bestFitFilename;
        }
    }

    private Mat drawKeyPoints(Mat image, MatOfKeyPoint keyPoints){
        Mat outputImage = image.clone();
        Features2d.drawKeypoints(image, keyPoints, outputImage, new Scalar(255, 0, 0),
                Features2d.DRAW_OVER_OUTIMG);

        return outputImage;
    }

    private Mat loadImageAsset(String imageName){
        AssetManager manager = mContext.getAssets();
        try {
            InputStream inputStream = manager.open("currency_images/" + imageName);
            return readInputStreamIntoMat(inputStream);

        } catch (IOException e) {
            return null;
        }
    }

    private String [] loadImageDatabase(){
        AssetManager manager = mContext.getAssets();
        try{
            return manager.list("currency_images");
        }catch (IOException e){
            return null;
        }
    }

    /* ****************************************************************************************** */
    /* Code taken from: stackoverflow.com/questions/29232220/android-read-image-using-opencv      */
    /* ****************************************************************************************** */

    private static Mat readInputStreamIntoMat(InputStream inputStream) throws IOException {
        byte[] temporaryImageInMemory = readStream(inputStream);
        return Highgui.imdecode(new MatOfByte(temporaryImageInMemory),
                Highgui.IMREAD_GRAYSCALE);
    }

    private static byte[] readStream(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = stream.read(data, 0, data.length)) != -1)
            buffer.write(data, 0, nRead);

        buffer.flush();
        byte[] temporaryImageInMemory = buffer.toByteArray();
        buffer.close();
        stream.close();
        return temporaryImageInMemory;
    }
}
