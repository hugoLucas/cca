package com.example.hugolucas.cca;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;

import java.io.ByteArrayOutputStream;
import java.io.File;
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

    public static final int mCurrencyCodeIndex = 0;
    public static final int mCurrencyValueIndex = 1;

    private Context mContext;
    private String mBasePath;

    private String [] mDBFileList;
    private int mCurrentFileIndex;
    private MatOfKeyPoint mDescriptors;
    private MatOfKeyPoint mKeyPoints;
    private List<KeyPoint> mKeyPointListSource;
    private DescriptorMatcher mMatcher;


    private String mBestFitFileName;
    private int mBestFitMatches;

    Classifier(Context c){
        mContext = c;
        mCurrentFileIndex = 0;

        mBestFitFileName = null;
        mBestFitMatches = -1;

        mDescriptors = new MatOfKeyPoint();
        mKeyPoints = new MatOfKeyPoint();

        mMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
    }

    void compareAgainstUserDB(){
        String [] files = loadUserImageDatabase();

        if (files != null && files.length > 0) {
            for (String file : files) {
                String filePath = mBasePath + "/" + file;
                List<MatOfDMatch> matches = new LinkedList<>();
                Mat image = Highgui.imread(filePath);

                Log.v(TAG, "Matching" + filePath + "features...");

                MatOfKeyPoint keyPoints = detectFeatures(image, null);
                MatOfKeyPoint databaseDescriptors = getDescriptors(image, keyPoints);
                mMatcher.knnMatch(mDescriptors, databaseDescriptors, matches, 2);

                Log.v(TAG, filePath + " features matched!");

                LinkedList<DMatch> good_matches = new LinkedList<>();
                for (Iterator<MatOfDMatch> iterator = matches.iterator(); iterator.hasNext();) {
                    MatOfDMatch matOfDMatch = iterator.next();
                    if (matOfDMatch.toArray()[0].distance / matOfDMatch.toArray()[1].distance < 1.0) {
                        good_matches.add(matOfDMatch.toArray()[0]);
                    }
                }

                List<KeyPoint> keyPointListTarget = keyPoints.toList();

                List<Point> sourcePoints = new LinkedList<>();
                List<Point> targetPoints = new LinkedList<>();

                for (DMatch match: good_matches){
                    sourcePoints.add(mKeyPointListSource.get(match.queryIdx).pt);
                    targetPoints.add(keyPointListTarget.get(match.trainIdx).pt);
                }

                if (sourcePoints.size() > 0) {
                    MatOfPoint2f source = new MatOfPoint2f();
                    source.fromList(sourcePoints);

                    MatOfPoint2f target = new MatOfPoint2f();
                    target.fromList(targetPoints);

                    Mat homographMask = new Mat();
                    Calib3d.findHomography(source, target, Calib3d.RANSAC, 5, homographMask);

                    List<MatOfDMatch> inliers = new LinkedList<>();
                    for (int i = 0; i < homographMask.rows(); i++)
                        if (homographMask.get(i, 0)[0] > 0)
                            inliers.add(matches.get(i));

                    if (inliers.size() > mBestFitMatches) {
                        mBestFitMatches = inliers.size();
                        mBestFitFileName = file.split("\\.")[0];
                    }

                    if (mBestFitMatches > 250){
                        mCurrentFileIndex = mDBFileList.length;
                    }

                    Log.v(TAG, "RESULTS: " + file + ", " + inliers.size());
                }
                mCurrentFileIndex++;
            }
        }
    }

    /**
     * Returns the integer amount the loading icon should be updated for each file in the
     * image database that has been processed.
     *
     * @param width     the total amount of progress space (out of 100) devoted to classification
     * @return          an integer step
     */
    int calculateStep(int width){
        mDBFileList = loadImageDatabase();
        try {
            return width / mDBFileList.length;
        }catch (NullPointerException e){
            Log.v(TAG, "Database failed to load correctly!");
            return 100;
        }
    }

    /**
     * Detects features of unknown banknote. Split from rest of processing in order to provide
     * a more informative loading screen.
     *
     * @param image     Mat of unknown banknote
     */
    void extractImageFeatures(Mat image){
        mKeyPoints = detectFeatures(image, null);
        mDescriptors = getDescriptors(image, mKeyPoints);

        mKeyPointListSource = mKeyPoints.toList();
        Log.v(TAG, "Features Extracted");
    }

    /**
     * Determines if there are more files to be processed.
     *
     * @return      True if more files to process, false otherwise
     */
    boolean comparisonComplete(){
        return mCurrentFileIndex >= mDBFileList.length;
    }

    /**
     * Compares the unknown banknote to one set of database images and masks.
     */
    void processFile(){
        if (mDBFileList == null){
            Log.v(TAG, "Error! File database could not be loaded.");
        }else {
            List<MatOfDMatch> matches = new LinkedList<>();
            String fileName = mDBFileList[mCurrentFileIndex];

            Mat image = loadAsset(fileName, "images");
            Mat mask = loadAsset(fileName, "masks");

            Log.v(TAG, "Matching" + fileName + "features...");
            MatOfKeyPoint keyPoints = detectFeatures(image, mask);
            MatOfKeyPoint databaseDescriptors = getDescriptors(image, keyPoints);
            mMatcher.knnMatch(mDescriptors, databaseDescriptors, matches, 2);
            Log.v(TAG, fileName + " features matched!");

            LinkedList<DMatch> good_matches = new LinkedList<>();
            for (Iterator<MatOfDMatch> iterator = matches.iterator(); iterator.hasNext();) {
                MatOfDMatch matOfDMatch = iterator.next();
                if (matOfDMatch.toArray()[0].distance / matOfDMatch.toArray()[1].distance < 1.0) {
                    good_matches.add(matOfDMatch.toArray()[0]);
                }
            }

            List<KeyPoint> keyPointListTarget = keyPoints.toList();

            List<Point> sourcePoints = new LinkedList<>();
            List<Point> targetPoints = new LinkedList<>();

            for (DMatch match: good_matches){
                sourcePoints.add(mKeyPointListSource.get(match.queryIdx).pt);
                targetPoints.add(keyPointListTarget.get(match.trainIdx).pt);
            }

            if (sourcePoints.size() > 0) {
                MatOfPoint2f source = new MatOfPoint2f();
                source.fromList(sourcePoints);

                MatOfPoint2f target = new MatOfPoint2f();
                target.fromList(targetPoints);

                Mat homographMask = new Mat();
                Calib3d.findHomography(source, target, Calib3d.RANSAC, 5, homographMask);

                List<MatOfDMatch> inliers = new LinkedList<>();
                for (int i = 0; i < homographMask.rows(); i++)
                    if (homographMask.get(i, 0)[0] > 0)
                        inliers.add(matches.get(i));

                if (inliers.size() > mBestFitMatches) {
                    mBestFitMatches = inliers.size();
                    mBestFitFileName = fileName;
                }

                if (mBestFitMatches > 250){
                    mCurrentFileIndex = mDBFileList.length;
                }

                Log.v(TAG, "RESULTS: " + fileName + ", " + inliers.size());
            }
            mCurrentFileIndex++;
        }
    }

    String [] getResults(){
        return generateResultsString(mBestFitFileName);
    }

    /**
     * Applies a Scale-Invariant Feature Transform (SIFT) to the input image.
     *
     * @param image     the image to apply SIFT to
     * @return          a MatOfKeyPoint representing the features detected
     */
    private MatOfKeyPoint detectFeatures(Mat image, Mat mask){
        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.SIFT);

        if (mask == null)
            detector.detect(image, keyPoints);
        else
            detector.detect(image, keyPoints, mask);

        return keyPoints;
    }

    /**
     * Generates a list of descriptors from a list of key points.
     *
     * @param image         the image to classify
     * @param keyPoints     a list of MatOfKeyPoint generated from the image
     * @return              a list of MatOfKeyPoint generated from keyPoints
     */
    private MatOfKeyPoint getDescriptors(Mat image, MatOfKeyPoint keyPoints){
        MatOfKeyPoint descriptors = new MatOfKeyPoint();
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
        extractor.compute(image, keyPoints, descriptors);

        return descriptors;
    }

    /**
     * Extracts the 3-letter currency code and n-digit value of the matched banknote from its
     * filename.
     *
     * @param fileName      the file name the unknown banknote most closely matches from DB
     * @return              a string array containing the matched currency code and value
     */
    private String[] generateResultsString(String fileName){
        String [] components = fileName.split("_");
        return new String[] {components[mCurrencyCodeIndex], components[mCurrencyValueIndex]};
    }

    /**
     * Debugging function, used to display key points matched in an image.
     *
     * @param image         the image of the unknown banknote
     * @param keyPoints     key points generated by SIFT
     * @return              an output image with the key points annotated
     */
    private Mat drawKeyPoints(Mat image, MatOfKeyPoint keyPoints){
        Mat outputImage = image.clone();
        Features2d.drawKeypoints(image, keyPoints, outputImage, new Scalar(255, 0, 0),
                Features2d.DRAW_OVER_OUTIMG);

        return outputImage;
    }

    /**
     * Loads an image asset from Android device memory.
     *
     * @param assetName         the file name of the image asset
     * @param assetDirectory    the directory under the assets folder where the image asset is
     * @return                  a Mat file of the image asset
     */
    private Mat loadAsset(String assetName, String assetDirectory){
        Log.v(TAG, "Loading asset " + assetName + "...");
        AssetManager manager = mContext.getAssets();
        try {
            InputStream imageStream = manager.open("currency_images/" + assetDirectory + "/" +
                    assetName);
            Log.v(TAG, "Asset " + assetName + " loaded successfully!");
            return readInputStreamIntoMat(imageStream);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Creates a list of all images in the image database.
     *
     * @return      a list of filenames
     */
    private String [] loadImageDatabase(){
        AssetManager manager = mContext.getAssets();
        try{
            return manager.list("currency_images/images");
        }catch (IOException e){
            return null;
        }
    }

    /**
     * If a user-defined database exists, loads a list of all files within it.
     *
     * @return  an array of strings if a DB exists, null otherwise
     */
    private String [] loadUserImageDatabase(){
        File storageDirectory = new File(mContext.getFilesDir() + "/DB");

        if(storageDirectory.exists()) {
            mBasePath = storageDirectory.getPath();
            return storageDirectory.list();
        }
        else
            return null;
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
