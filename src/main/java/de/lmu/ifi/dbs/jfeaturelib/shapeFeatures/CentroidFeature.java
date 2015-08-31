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
package de.lmu.ifi.dbs.jfeaturelib.shapeFeatures;

import de.lmu.ifi.dbs.jfeaturelib.Progress;
import de.lmu.ifi.dbs.jfeaturelib.features.AbstractFeatureDescriptor;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * Computes the Centroid according to "Bryan S. Morse (2000): Lecture 9: Shape
 * Description (Regions), Brigham Young University, Available from:
 * http://homepages.inf.ed.ac.uk/rbf/CVonline/LOCAL_COPIES/MORSE/region-props-and-moments.pdf".
 *
 * @author Johannes Stadler
 * @since 09/29/2012
 */
public class CentroidFeature extends AbstractFeatureDescriptor {

    private double x = 0, y = 0;
    private int k;
    private int backgroundColor = 0;

    @Override
    public void run(ImageProcessor ip) {
        pcs.firePropertyChange(Progress.getName(), null, Progress.START);

        ByteProcessor image;
        if (!ByteProcessor.class.isAssignableFrom(ip.getClass())) {
            image = (ByteProcessor) ip.convertToByte(true);
        } else {
            image = (ByteProcessor) ip;
        }
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                if (image.getPixel(j, i) != backgroundColor) {
                    k++;
                    x += j;
                    y += i;
                }

            }
        }
        x = x / k;
        y = y / k;

        addData(new double[]{x, y});
        pcs.firePropertyChange(Progress.getName(), null, Progress.END);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String getDescription() {
        return "returns the shape's centroid pixel (x,y)";
    }
}