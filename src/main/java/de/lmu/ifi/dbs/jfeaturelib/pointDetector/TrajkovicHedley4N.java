/*
 * This file is part of the JFeatureLib project: https://github.com/locked-fg/JFeatureLib
 * JFeatureLib is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * JFeatureLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JFeatureLib; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * You are kindly asked to refer to the papers of the according authors which 
 * should be mentioned in the Javadocs of the respective classes as well as the 
 * JFeatureLib project itself.
 * 
 * Hints how to cite the projects can be found at 
 * https://github.com/locked-fg/JFeatureLib/wiki/Citation
 */
package de.lmu.ifi.dbs.jfeaturelib.pointDetector;

import de.lmu.ifi.dbs.jfeaturelib.ImagePoint;
import de.lmu.ifi.dbs.jfeaturelib.Progress;
import ij.process.ImageProcessor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Trajkovic and Hedley Corner Detector.
 *
 * This Implementation does not yet use a Besenham Circle for the Points. This is a simpler Algorithm which only uses 4
 * neighbours, like the Algorithm from Donovan Parks and Jean-Philippe Gravel.
 *
 * See http://kiwi.cs.dal.ca/~dparks/CornerDetection/trajkovic.htm and
 * http://en.wikipedia.org/wiki/Corner_detection#The_Trajkovic_and_Hedley_corner_detector for further information. *
 *
 * @author Robert Zelhofer
 */
public class TrajkovicHedley4N implements PointDetector {

    //Returning List of Corners
    private List<ImagePoint> corners;
    //Threshold values
    private int threshold1;
    private int threshold2;
    //lower resolution Scale;
    private int lowResScale;
    //radius for the circle
    private int distance;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @Override
    public List<ImagePoint> getPoints() {
        return corners;
    }

    @Override
    public EnumSet<Supports> supports() {
        return EnumSet.of(
                Supports.NoChanges,
                Supports.DOES_8G);
    }

    @Override
    public void run(ImageProcessor ip) {
        pcs.firePropertyChange(Progress.getName(), null, Progress.START);

        int lowResWidth = ip.getWidth() / lowResScale;
        int lowResHeight = ip.getHeight() / lowResScale;
        //Create lower Resolution image with custom Scale
        ImageProcessor lowResIp = ip.resize(lowResWidth, lowResWidth);
        ImageProcessor lowResIpResult = lowResIp.duplicate();

        //First cSimple For low Res Image
        for (int i = distance; i < lowResWidth - distance; i++) {
            for (int j = distance; j < lowResHeight - distance; j++) {
                int cSimple = cSimple(lowResIp, i, j);

                if (cSimple >= threshold1) {
                    lowResIpResult.putPixel(i, j, 255);
                } else {
                    lowResIpResult.putPixel(i, j, 0);
                }
            }
        }
        // Then cInterpixel for the actual Cornerness Measure
        for (int i = distance; i < lowResWidth - distance; i++) {
            for (int j = distance; j < lowResHeight - distance; j++) {
                if (lowResIpResult.getPixel(i, j) == 255) {
                    int origX = i * lowResScale;
                    int origY = j * lowResScale;
                    int cSimpleOrig = cSimple(ip, origX, origY);

                    if (cSimpleOrig >= threshold2) {
                        if (cInterpixel(ip, origX, origY) >= threshold2) {
                            corners.add(new ImagePoint(origX, origY));
                        }
                    }
                }
            }

        }

        pcs.firePropertyChange(Progress.getName(), null, Progress.END);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Creates Trajkovic and Hedley Corner Detector with defauult values: distance = 1 threshold1 = 500 threshold2 = 500
     * scale = 2
     */
    public TrajkovicHedley4N() {
        this(1, 500, 500, 2);
    }

    /**
     * Creates Trajkovic and Hedley Corner Detector
     *
     * @param distance This value sets the distance from the nearby pixels.
     * @param threshold1 This is the value for the first calcualtion of possible corners in the scaled lower resolution
     * image.
     * @param threshold2 This is the value for the second calculation of possible corners in the original image.
     * @param scale Scale for the lower resolution image. The orignial images size is divided by this number;
     */
    public TrajkovicHedley4N(int distance, int threshold1, int threshold2, int scale) {
        corners = new ArrayList<>();
        this.distance = distance;
        this.threshold1 = threshold1;
        this.threshold2 = threshold2;
        this.lowResScale = scale;
    }

    /**
     * Calculate the Simple Trajkovic and Hedley Cornerness Measure
     *
     * @param ip ImageProcessor
     * @param x X value of the Pixel
     * @param y Y value of the Pixel
     * @return Simple Cornerness Measure
     */
    private int cSimple(ImageProcessor ip, int x, int y) {
        int iC = ip.getPixel(x, y);
        int iA1 = ip.getPixel(x, y + distance);
        int iA2 = ip.getPixel(x, y - distance);
        int iB1 = ip.getPixel(x + distance, y);
        int iB2 = ip.getPixel(x - distance, y);
        int rA = (int) Math.pow((iA1 - iC), 2) + (int) Math.pow((iA2 - iC), 2);
        int rB = (int) Math.pow((iB1 - iC), 2) + (int) Math.pow((iB2 - iC), 2);
        return Math.min(rA, rB);
    }

    /**
     * Calculate the Trajkovic and Hedley Cornerss Measure on the Original Image after the Simple Cornerness Measure
     *
     * @param ip Imageprocessor
     * @param x X value of the Pixel
     * @param y Y value of the Pixel
     * @return Cornerness Measure
     */
    private int cInterpixel(ImageProcessor ip, int x, int y) {
        int iC = ip.getPixel(x, y);
        int iA1 = ip.getPixel(x, y + distance);
        int iA2 = ip.getPixel(x, y - distance);
        int iB1 = ip.getPixel(x + distance, y);
        int iB2 = ip.getPixel(x - distance, y);
        int rA = (int) Math.pow((iA1 - iC), 2) + (int) Math.pow((iA2 - iC), 2);
        int rB = (int) Math.pow((iB1 - iC), 2) + (int) Math.pow((iB2 - iC), 2);
        int b1 = (iB1 - iA1) * (iA1 - iC) + (iB2 - iA2) * (iA2 - iC);
        int b2 = (iB1 - iA2) * (iA2 - iC) + (iB2 - iA1) * (iA1 - iC);
        int c = rA;
        int b = Math.min(b1, b2);
        int a = rB - rA - 2 * b;

        int cInterpixel;
        if (b < 0 && (a + b) > 0) {
            cInterpixel = c - ((b * b) / a);
        } else {
            //else cSimple
            cInterpixel = Math.min(rA, rB);
        }
        return cInterpixel;
    }
}
