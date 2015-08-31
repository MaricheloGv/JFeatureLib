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

import net.semanticmetadata.lire.imageanalysis.LireFeature;

/**
 * Class for extrcating & comparing MPEG-7 based CBIR descriptor ColorLayout
 *
 * This is a wrapper class for the corresponding lire class (ColorLayout and
 * ColorLayoutImpl).
 *
 * @author Franz
 * @since 1.4.0
 */
public class MPEG7ColorLayout extends LireWrapper {

    public MPEG7ColorLayout() {
        super(new net.semanticmetadata.lire.imageanalysis.ColorLayout());
    }
}
