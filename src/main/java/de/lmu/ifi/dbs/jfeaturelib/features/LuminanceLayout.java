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

import java.util.EnumSet;

/**
 * The LuminanceLayout Descriptor is intended for grayscale or B/W images. It
 * scales an image down to a very small size and uses this smaller version as a
 * descriptor. Interesting aspect is that white stripes are added to make the
 * small image quadratic.
 *
 * This class is a wrapper for the according LIRE class.
 *
 * @author Franz
 * @since 1.5.0
 */
public class LuminanceLayout extends LireWrapper {

    public LuminanceLayout() {
        super(new net.semanticmetadata.lire.imageanalysis.LuminanceLayout());
    }

    @Override
    public EnumSet<Supports> supports() {
        return EnumSet.of(Supports.DOES_8G);
    }
}
