package com.example.shakecaine.OCR.services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;
import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shakecaine on 2016-05-22.
 */
public class TextDetectService {

    private Scalar CONTOUR_COLOR;

    private Mat doCanny(Mat frame)
    {
        // init
        Mat grayImage = frame;
        Mat detectedEdges = new Mat();

        // convert to grayscale
        //Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);

        // reduce noise with a 3x3 kernel
        Imgproc.blur(grayImage, detectedEdges, new Size(3, 3));

        // canny detector, with ratio of lower:upper threshold of 3:1
        Imgproc.Canny(detectedEdges, detectedEdges, 50, 50 * 3);

        // using Canny's output as a mask, display the result
        Mat dest = new Mat();
        frame.copyTo(dest, detectedEdges);

        return dest;
    }

    private Mat doBackgroundRemoval(Mat frame)
    {
        // init
        Mat hsvImg = new Mat();
        List<Mat> hsvPlanes = new ArrayList<>();
        Mat thresholdImg = new Mat();

        int thresh_type = Imgproc.THRESH_BINARY_INV;

        // threshold the image with the average hue value
        hsvImg.create(frame.size(), CvType.CV_8U);
        //Imgproc.cvtColor(frame, hsvImg, Imgproc.COLOR_BGR2HSV);
        hsvImg = frame;
        Core.split(hsvImg, hsvPlanes);

        // get the average hue value of the image
        double threshValue = this.getHistAverage(hsvImg, hsvPlanes.get(0));

        Imgproc.threshold(hsvPlanes.get(0), thresholdImg, threshValue, 179.0, thresh_type);

        Imgproc.blur(thresholdImg, thresholdImg, new Size(5, 5));

        // dilate to fill gaps, erode to smooth edges
        Imgproc.dilate(thresholdImg, thresholdImg, new Mat(), new Point(-1, -1), 1);
        Imgproc.erode(thresholdImg, thresholdImg, new Mat(), new Point(-1, -1), 3);

        Imgproc.threshold(thresholdImg, thresholdImg, threshValue, 179.0, Imgproc.THRESH_BINARY);

        // create the new image
        Mat foreground = new Mat(frame.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        frame.copyTo(foreground, thresholdImg);

        return foreground;
    }

    private double getHistAverage(Mat hsvImg, Mat hueValues)
    {
        // init
        double average = 0.0;
        Mat hist_hue = new Mat();
        // 0-180: range of Hue values
        MatOfInt histSize = new MatOfInt(180);
        List<Mat> hue = new ArrayList<>();
        hue.add(hueValues);

        // compute the histogram
        Imgproc.calcHist(hue, new MatOfInt(0), new Mat(), hist_hue, histSize, new MatOfFloat(0, 179));

        // get the average Hue value of the image
        // (sum(bin(h)*h))/(image-height*image-width)
        // -----------------
        // equivalent to get the hue of each pixel in the image, add them, and
        // divide for the image size (height and width)
        for (int h = 0; h < 180; h++)
        {
            // for each bin, get its value and multiply it for the corresponding
            // hue
            average += (hist_hue.get(h, 0)[0] * h);
        }

        // return the average hue of the image
        return average = average / hsvImg.size().height / hsvImg.size().width;
    }

    public List<Mat> rectangleText(Mat mGrayy, Mat orginalMat) {
        List<Mat> matList = new ArrayList<Mat>();
        Mat mGray = mGrayy;


        CONTOUR_COLOR = new Scalar(255);
        MatOfKeyPoint keypoint = new MatOfKeyPoint();
        List<KeyPoint> listpoint = new ArrayList<KeyPoint>();
        KeyPoint kpoint = new KeyPoint();
        Mat mask = Mat.zeros(mGray.size(), CvType.CV_8UC1);
        int rectanx1;
        int rectany1;
        int rectanx2;
        int rectany2;

        //
        Scalar zeos = new Scalar(0, 0, 0);
        List<MatOfPoint> contour1 = new ArrayList<MatOfPoint>();
        List<MatOfPoint> contour2 = new ArrayList<MatOfPoint>();
        Mat kernel = new Mat(1, 50, CvType.CV_8UC1, Scalar.all(255));
        Mat morbyte = new Mat();
        Mat hierarchy = new Mat();

        Rect rectan2 = new Rect();//
        Rect rectan3 = new Rect();//
        int imgsize = mGray.height() * mGray.width();

        //
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.MSER);
        detector.detect(mGray, keypoint);
        listpoint = keypoint.toList();
        //
        for (int ind = 0; ind < listpoint.size(); ind++) {
            kpoint = listpoint.get(ind);
            rectanx1 = (int) (kpoint.pt.x - 0.5 * kpoint.size);
            rectany1 = (int) (kpoint.pt.y - 0.5 * kpoint.size);
            //rectanx2 = (int) (kpoint.pt.x + 0.5 * kpoint.size);
            //rectany2 = (int) (kpoint.pt.y + 0.5 * kpoint.size);
            rectanx2 = (int) (kpoint.size);
            rectany2 = (int) (kpoint.size);
            if (rectanx1 <= 0)
                rectanx1 = 1;
            if (rectany1 <= 0)
                rectany1 = 1;
            if ((rectanx1 + rectanx2) > mGray.width())
                rectanx2 = mGray.width() - rectanx1;
            if ((rectany1 + rectany2) > mGray.height())
                rectany2 = mGray.height() - rectany1;
            Rect rectant = new Rect(rectanx1, rectany1, rectanx2, rectany2);
            Mat roi = new Mat(mask, rectant);
            roi.setTo(CONTOUR_COLOR);
        }

        Imgproc.morphologyEx(mask, morbyte, Imgproc.MORPH_DILATE, kernel);
        Imgproc.findContours(morbyte, contour2, hierarchy,
                Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        for (int ind = 0; ind < contour2.size(); ind++) {
            rectan3 = Imgproc.boundingRect(contour2.get(ind));

            if (rectan3.area() > 0.5 * imgsize || rectan3.area() < 100
                    || rectan3.width / rectan3.height < 2) {
                Mat roi = new Mat(morbyte, rectan3);
                roi.setTo(zeos);
            } else {
                Imgproc.rectangle(mGray, rectan3.br(), rectan3.tl(),
                        CONTOUR_COLOR);
                matList.add(mGray.submat(rectan3.y, rectan3.y + rectan3.height, rectan3.x, rectan3.x + rectan3.width));
            }
        }

        matList.add(mGray);
        return matList;
    }

    public void detectTextRectangle(String path, String DATA_PATH, String lang, TextExtractService textExtractService){
        File file = new File(path);
        if (!file.exists())
            Log.d("RECTANGLES", "FUCK YOU");

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inScreenDensity = 300;
        options.inTargetDensity = 300;
        options.inPreferQualityOverSpeed = true;


        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        Log.d("RECTANGLES", "Scaled down bitmap size HEIGHT = " + bitmap.getHeight() + ", WIDTH = " + bitmap.getWidth());

        Log.v("RECTANGLES", "Before baseApi");
        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(DATA_PATH, lang);
        baseApi.setImage(bitmap);
        baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_ONLY);

        String recognizedText = baseApi.getUTF8Text();

        baseApi.end();

        // You now have the text in recognizedText var, you can do anything with it.
        // We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
        // so that garbage doesn't make it to the display.

        if (lang.equalsIgnoreCase("eng")) {
            recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
        }

        recognizedText = recognizedText.trim();

        if(recognizedText.length() > 1) {
            Log.v("RECTANGLES", "File :" + file.getName() + ", OCRED TEXT: " + recognizedText);
            textExtractService.extractSubWords(recognizedText);
        } else {
            Log.v("RECTANGLES", "File :" + file.getName() + ", NO TEXT");
        }

    }
}
