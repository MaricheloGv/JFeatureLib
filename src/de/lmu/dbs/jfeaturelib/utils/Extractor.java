package de.lmu.dbs.jfeaturelib.utils;

import de.lmu.dbs.jfeaturelib.LibProperties;
import de.lmu.dbs.jfeaturelib.features.FeatureDescriptor;
import de.lmu.ifi.dbs.utilities.Arrays2;
import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ImageProcessor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Class used as a commandline tool to extract features from directories of
 * images. The features are then written to a outfile.
 *
 * @author Franz
 */
public class Extractor {

    private static final Logger log = Logger.getLogger(Extractor.class.getName());
    @Option(name = "--threads", usage = "amount of threads")
    private int threads = Runtime.getRuntime().availableProcessors();
    // 
    @Option(name = "-d", usage = "directory containing images (default: execution directory)")
    private File directory;
    //
    @Option(name = "-r", usage = "recursively descend into directories (default: no)")
    private boolean recursive = false;
    //
    @Option(name = "-o", usage = "output to this file (default: features.csv)")
    private File outFile;
    //
    @Option(name = "--append", usage = "append to output file (default: false = overwrite)")
    private boolean append;
    //
    @Option(name = "-nh", usage = "omit headerline")
    private boolean omitHeader = false;
    //
    @Option(name = "-D", usage = "use this feature descriptor (e.G: Sift)", required = true)
    private String descriptor = null;
    //
    @Option(name = "-c", usage = "image class that should be written to the output file")
    private String imageClass = null;
    //
    @Option(name = "--help", usage = "show this screen")
    private boolean showHelp = false;
    // other command line parameters than options
    // @Argument
    // private List<String> arguments = new ArrayList<>();
    // 
    // FIXME move to properties file
    private static final int WRITE_BUFFER = 1024 * 1024; // bytes
    private static final String NL = "\n";
    //
    private final LibProperties properties;
    private final String[] imageFormats;
    private String separator = ", ";
    private int lineCounter = 0;
    // file exists and has a length > 0
    private boolean fileExists;
    // the descriptor to use
    private Class descriptorClazz;
    private Writer writer;
    private ExecutorService pool;

    public static void main(String[] args) throws IOException {
        InputStream is = Extractor.class.getResourceAsStream("/META-INF/logging.properties");
        PropertyConfigurator.configure(is);

        Extractor extractor = new Extractor();
        CmdLineParser parser = new CmdLineParser(extractor);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java -jar JFeatureLib.jar de.lmu.dbs.jfeaturelib.utils.Extractor [arguments]");
            parser.printUsage(System.err);
            return;
        }

        if (extractor.showHelp) {
            System.err.println("java -jar JFeatureLib.jar de.lmu.dbs.jfeaturelib.utils.Extractor [arguments]");
            parser.printUsage(System.out);
            System.exit(0);
        }

        extractor.run();
    }

    private Extractor() throws IOException {
        properties = LibProperties.get();
        imageFormats = properties.getString(LibProperties.IMAGE_FORMATS).split(" *, *");
        for (int i = 0; i < imageFormats.length; i++) {
            imageFormats[i] = imageFormats[i].trim();
        }
    }

    private void run() {
        validateInput();
        openWriter();

        openPool();
        processImages(createFileList());
        closePool();

        closeWriter();
    }

    private void validateInput() throws IllegalArgumentException {
        try {
            String base = FeatureDescriptor.class.getPackage().getName();
            descriptorClazz = Class.forName(base + "." + descriptor);
            if (!FeatureDescriptor.class.isAssignableFrom(descriptorClazz)) {
                throw new IllegalArgumentException("The class must derive from FeatureDescriptor");
            }
        } catch (ClassNotFoundException ex) {
            log.warn(ex.getMessage(), ex);
            throw new IllegalArgumentException("the descriptor class does not exist or cannot be created");
        }
        if (directory == null || !directory.isDirectory() || !directory.canRead()) {
            throw new IllegalArgumentException("the directory cannot be read or does not exist");
        }
        if (outFile == null || (outFile.exists() && !outFile.canWrite())) {
            throw new IllegalArgumentException("the output file is not valid or not writable");
        }
        try {
            outFile.createNewFile();
        } catch (IOException ex) {
            log.warn(ex.getMessage(), ex);
            throw new IllegalArgumentException("the output file could not be created");
        }
        if (imageClass != null && !imageClass.matches("^\\w$")) {
            throw new IllegalArgumentException("the image class must only contain word characters");
        }

        fileExists = (outFile.exists() && outFile.length() > 0);
    }

    private Collection<File> createFileList() {
        return FileUtils.listFiles(directory, imageFormats, recursive);
    }

    private void openWriter() {
        try {
            writer = new BufferedWriter(new FileWriter(outFile, append), WRITE_BUFFER);
        } catch (IOException ex) {
            log.warn(ex.getMessage(), ex);
            throw new IllegalStateException("could not open output file for writing");
        }
    }

    private void closeWriter() {
        try {
            writer.close();
        } catch (IOException ex) {
            log.warn(ex.getMessage(), ex);
            throw new IllegalStateException("could not close output file");
        }
    }

    private void processImages(Collection<File> images) {
        for (File file : images) {
            pool.submit(new Task(file, properties));
        }
    }

    private void openPool() {
        pool = Executors.newFixedThreadPool(threads);
    }

    private void closePool() {
        try {
            pool.shutdown();
            pool.awaitTermination(100, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            log.warn(ex.getMessage(), ex);
            throw new IllegalStateException("error while shutting down pool");
        }
    }

    private synchronized void writeOutput(File file, List<double[]> features) throws IOException {
        // we are appending to an existing file. so start with a new line
        if (lineCounter == 0 && fileExists) {
            writer.append(NL);
        }

        // write head?
        if (!omitHeader) {
            omitHeader = true;
            if (imageClass != null) {
                writer.append("class" + separator);
            }
            writer.append("filename");
            for (int i = 0; i < features.get(0).length; i++) {
                writer.append(separator + i);
            }
            writer.append(NL);
        }

        // write one line for each feature
        for (double[] feature : features) {
            // a second line is being written. Thus prepend a new line
            if (lineCounter++ > 0) {
                writer.append(NL);
            }
            // prepend image class (if given)
            if (imageClass != null) {
                writer.append(imageClass).append(separator);
            }

            // write file name
            writer.append(file.getName()).append(separator);

            // serialize the feature values
            writer.append(Arrays2.join(feature, separator));
        }
    }

    private class Task implements Runnable {

        private final File file;
        private final LibProperties properties;

        public Task(File file, LibProperties properties) {
            this.file = file;
            this.properties = properties;
        }

        @Override
        public void run() {
            try {
                log.debug("processing file " + file.getName());
                ImagePlus iplus = new Opener().openImage(file.getAbsolutePath());
                ImageProcessor processor = iplus.getProcessor();

                FeatureDescriptor fd = (FeatureDescriptor) descriptorClazz.newInstance();
                fd.setProperties(properties);
                fd.run(processor);
                List<double[]> features = fd.getFeatures();

                writeOutput(file, features);
            } catch (IOException | InstantiationException | IllegalAccessException ex) {
                log.warn(ex.getMessage(), ex);
            }
        }
    }
}
