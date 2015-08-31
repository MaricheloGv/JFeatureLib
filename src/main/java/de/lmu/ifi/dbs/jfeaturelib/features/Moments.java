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
package de.lmu.ifi.dbs.jfeaturelib.features;

import de.lmu.ifi.dbs.jfeaturelib.Descriptor.Supports;
import de.lmu.ifi.dbs.jfeaturelib.Progress;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import java.util.EnumSet;

/**
 * Calculates the basic statistical moments of an image like mean, std_dev,
 * skewness and kurtosis.
 *
 * @author graf
 * @since 11/4/2011
 */
public class Moments extends AbstractFeatureDescriptor {

    @Override
    public void run(ImageProcessor ip) {
        int STATS = ImageStatistics.MEAN
                + ImageStatistics.STD_DEV
                + ImageStatistics.SKEWNESS
                + ImageStatistics.KURTOSIS;
        
        firePropertyChange(Progress.START);
        ImageStatistics stat = ImageStatistics.getStatistics(ip, STATS, null);
        addData(new double[]{stat.mean, stat.stdDev, stat.skewness, stat.kurtosis});
        firePropertyChange(Progress.END);
    }

    @Override
    public EnumSet<Supports> supports() {
        return DOES_ALL;
    }

    @Override
    public String getDescription() {
        return "First 4 Statistical Moments (Mean, StdDev, Skewness, kurtosis)";
    }
}
