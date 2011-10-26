package de.lmu.dbs.jfeaturelib.features;

import de.lmu.ifi.dbs.utilities.Arrays2;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.util.EnumSet;

/**
 * Reads the histogram from the Image Processor and returns it as double[]
 * @author Benedikt
 */
public class RGBHistogram implements FeatureDescriptor{

    private DescriptorChangeListener changeListener;
    private long time;
    private boolean calculated;
    private int progress;    
    private int tonalValues;
    private final int CHANNELS;
    private int[] features;
    private ColorProcessor image;
    
    /**
     * Constructs a RGB histogram with default parameters (8bit per channel).
     */    
    public RGBHistogram(){
        //assuming 8bit RGB image
        tonalValues = 256;
        CHANNELS = 3;
        features = new int[CHANNELS*tonalValues];
        calculated = false;
        progress = 0;
    }
    
    /**
     * Constructs a RGB histogram.
     * @param values Number of tonal values, i.e. 256 for 8bit jpeg
     */ 
    public RGBHistogram(int values){
        //assuming 8bit RGB image
        tonalValues = values;
        CHANNELS = 3;
        features = new int[CHANNELS*tonalValues];
        calculated = false;
    }
    
    /**
     * Returns the RGB histogram as int array.
     */      
    @Override
    public double[] getFeatures() {
        if(calculated){
            return Arrays2.convertToDouble(features);
        }
        else{
            //TODO throw exception
            return null;
        }
    }
      
    /**
     * Defines the capability of the algorithm.
     * 
     * @see PlugInFilter
     * @see #supports() 
     */ 
    @Override
    public EnumSet<Supports> supports() {        
        EnumSet set = EnumSet.of(
            Supports.NoChanges,
            Supports.DOES_8C,
            Supports.DOES_8G,
            Supports.DOES_RGB
        );
        //set.addAll(DOES_ALL);
        return set;
    }
    
    /**
     * Returns information about the getFeauture returns in a String array.
     */ 
    @Override
    public String[] getDescription() {
        String[] info =  new String[CHANNELS*tonalValues];
        for (int i = 0; i < info.length; i++){
            if(i<tonalValues) info[i] = "Red Pixels with tonal value " + i;
            else if(i<tonalValues*2) info[i] = "Green Pixels with tonal value " + i%tonalValues;
            else info[i] = "Blue Pixels with tonal value " + i%tonalValues;
        }
        return(info);
    }
    
    /**
     * Starts the RGB histogram detection.
     * @param ip ImageProcessor of the source image
     */    
    @Override
    public void run(ImageProcessor ip) {
        long start = System.currentTimeMillis();
        this.image = (ColorProcessor)ip;
        fireStateChanged();
        process();
        calculated = true;
        time = (System.currentTimeMillis() - start);
    }
    
    private void process() {
        
        int[] r,g,b;
        ColorProcessor.setWeightingFactors(1,0,0);
        r = image.getHistogram();
        ColorProcessor.setWeightingFactors(0,1,0);
        g = image.getHistogram();
        ColorProcessor.setWeightingFactors(0,0,1);
        b = image.getHistogram();
        
        for(int i = 0; i < features.length; i++){
            if(i<tonalValues)features[i] = r[i];
            else if(i<tonalValues*2)features[i] = g[i%tonalValues];
            else features[i] = b[i%tonalValues];
            progress = (int)Math.round(i*(100.0/features.length));
            fireStateChanged();
        }
    }
    
    @Override
     public long getTime(){
         return time;
     }
     
    @Override
    public boolean isCalculated(){
        return calculated;
    }

    @Override
    public int getProgress() {
        return progress;
    }

    @Override
    public void setArgs(double[] args) {
        if(args == null){
            this.tonalValues = 256;
        }
        else if(args.length == 1){
            this.tonalValues = Integer.valueOf((int)args[0]);
        }
        else{
            throw new ArrayIndexOutOfBoundsException("Arguments array is not formatted correctly");
        }
        
    }
    
    @Override
    public void addChangeListener(DescriptorChangeListener l) {
        changeListener = l;
        l.valueChanged(new DescriptorChangeEvent(this));
    }

    @Override
    public void fireStateChanged() {
        changeListener.valueChanged(new DescriptorChangeEvent(this));
    }
}
