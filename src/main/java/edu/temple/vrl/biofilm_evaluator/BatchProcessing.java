package edu.temple.vrl.biofilm_evaluator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Class containing useful helper methods for batch processing of biofilm data.
 * Requires a specific naming convention for mesh and image stack files.
 * @author Logan Neishloss
 */
public class BatchProcessing {

    //Example batch processing of time-point data
    public static void main(String[] args) throws IOException {
        String import_filename;
        int t;
        double width;
        double height;
        double depth;
        int x;
        int y;
        int z;
        String export_filename;

        //Make sure .tif stack is named as "biofilmDate".tif
        //.obj and .txt are named biofilmDate_threshold_30000 / threshold_60000
        //Time points data must be labeled with their time point, i.e. biofilmDate_t0, or biofilmDate_t1.1

        //Increment this for each biofilm
        import_filename = "biofilmDate_t0";
        //For time-point data, change this depending on which time-point the data is from
        t = 0;
        //These parameters may or may not vary from biofilm to biofilm, somewhat tedious but necessary to check.
        width = 0.2405002;
        height = 0.2405002;
        depth = 0.2517703;
        x = 1024;
        y = 1024;
        z = 134;
        //To compile all the data together, leave this unchanged
        export_filename = "timePointDataExample";

        //Call processing method
        timePoints( import_filename, t, width, height, depth, x, y, z, export_filename);

        //For next biofilm, change everything as needed
        import_filename = "biofilmDate_t1";
        t = 1;
        z = 92;
        timePoints( import_filename, t, width, height, depth, x, y, z, export_filename);
    }

    /**
     *
     * @param import_filename name of file to evaluate
     * @param width width (in image coordinates)
     * @param height height (in image coordinates)
     * @param depth depth (in image coordinates)
     * @param x size of image in x direction
     * @param y size of image in y direction
     * @param z number of image stacks in z direction
     * @param export_filename name of file to print data to
     * @throws IOException file not found
     */
    public static void computeThresholds(String import_filename, double width, double height, double depth, int x, int y, int z, String export_filename) throws IOException {
        String name = import_filename;
        int numSurfaces30000 = 0;
        int numSurfaces60000 = 0;
        double vol30000 = 0.0;
        double vol60000 = 0.0;
        for(int i = 3; i <= 6; i += 3){
            //long start = System.nanoTime();
            BiofilmEvaluator be = new BiofilmEvaluator(width, height, depth, x, y, z);
            int threshold = i * 10000;
            import_filename = name;
            String obj = import_filename + "_threshold_" + threshold + ".obj";
            String txt = import_filename + "_threshold_" + threshold + ".txt";
            //String tif = s + ".tif";
            be.importMeshData(new File(obj), new File(txt));
            be.generateSurfaces(0);
            //be.exportOBJ(s + "_threshold_" + threshold.toString() + "_cleaned.obj");
            be.generateQuadtree(100);
            be.computeSurfaceMeasurements();
            //be.determineInteriorPoints();
            //be.importImage(new File(tif));
            //be.computeSurfaceDensities();
            //be.exportBiofilmData("biofilms.csv");
            //be.exportSurfacesData("surfaces.csv");
            if(i == 3){
                be.exportVolumeData(export_filename + "_30000.csv", import_filename);
                numSurfaces30000 = be.surfaces.size();
                vol30000 = be.totalVolume;
            }
            else{
                be.exportVolumeData(export_filename + "_60000.csv", import_filename);
                numSurfaces60000 = be.surfaces.size();
                vol60000 = be.totalVolume;
            }
            //long end = System.nanoTime();
            //System.out.println((end - start) / 1000000000.0 / 60.0);
        }
        printRatio(numSurfaces30000, numSurfaces60000, vol30000, vol60000, import_filename, export_filename);
    }

    /**
     *
     * @param import_filename name of file to evaluate
     * @param t time-point being evaluated
     * @param width width (in image coordinates)
     * @param height height (in image coordinates)
     * @param depth depth (in image coordinates)
     * @param x size of image in x direction
     * @param y size of image in y direction
     * @param z number of image stacks in z direction
     * @param export_filename name of file to print data to
     * @throws IOException file not found
     */
    public static void timePoints(String import_filename, int t, double width, double height, double depth, int x, int y, int z, String export_filename) throws IOException {
        String name = import_filename;
        int numSurfaces30000 = 0;
        int numSurfaces60000 = 0;
        double vol30000 = 0.0;
        double vol60000 = 0.0;
        for(int i = 3; i <= 6; i += 3){
            //long start = System.nanoTime();
            BiofilmEvaluator be = new BiofilmEvaluator(width, height, depth, x, y, z);
            int threshold = i * 10000;
            import_filename = name;
            String obj = import_filename + "_threshold_" + threshold + ".obj";
            String txt = import_filename + "_threshold_" + threshold + ".txt";
            String tif = import_filename + ".tif";
            be.importMeshData(new File(obj), new File(txt));
            be.generateSurfaces(0);
            //be.exportOBJ(s + "_threshold_" + threshold.toString() + "_cleaned.obj");
            be.generateQuadtree(100);
            be.computeSurfaceMeasurements();
            be.determineInteriorPoints();
            be.importImage(new File(tif));
            be.computeSurfaceDensities();
            //be.exportBiofilmData("biofilms.csv");
            //be.exportSurfacesData("surfaces.csv");
            if(i == 3){
                be.exportTimePointsData(export_filename + "_30000.csv", import_filename, t);
                numSurfaces30000 = be.surfaces.size();
                vol30000 = be.totalVolume;
            }
            else{
                be.exportTimePointsData(export_filename + "_60000.csv", import_filename, t);
                numSurfaces60000 = be.surfaces.size();
                vol60000 = be.totalVolume;
            }
            //long end = System.nanoTime();
            //System.out.println((end - start) / 1000000000.0 / 60.0);
        }
        printRatio(numSurfaces30000, numSurfaces60000, vol30000, vol60000, import_filename, export_filename);
    }

    /**
     * Creates a new .csv file and prints to it certain useful quantities
     * such as the ratio of the number of structures of the same biofilm at two different thresholds
     * or the ratio between the volume of the same biofilm at two different thresholds.
     * @param num30000 number of distinct structures at threshold 30000.
     * @param num60000 number of distinct structures at threshold 60000.
     * @param vol30000 total volume at threshold 30000
     * @param vol60000 total volume at threshold 60000
     * @param import_filename name of file evaluated
     * @param export_filename name of file exported
     * @throws IOException file not found
     */
    public static void printRatio(int num30000, int num60000, double vol30000, double vol60000, String import_filename, String export_filename) throws IOException{
        FileOutputStream outputStream;
        String output = "";
        byte[] bytes;

        if(new File(export_filename + "_ratios.csv").isFile()){
            outputStream = new FileOutputStream(export_filename + "_ratios.csv", true);
        }
        else{
            outputStream = new FileOutputStream(export_filename + "_ratios.csv");
            output = "Biofilm, # of 30000 Structures, # of 60000 Structures, Ratio # 30000 / # 60000, Total Vol 30000, Total Vol 60000, Ratio Vol 30000 / Vol 60000\n";
        }
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

        output = output.concat(import_filename + ", "  + num30000 + ", " + num60000 + ", " + (double)num30000 / (double)num60000 + ", " + vol30000 +  ", " + vol60000 + ", " + vol30000 / vol60000 + "\n");

        bytes = output.getBytes();
        bufferedOutputStream.write(bytes);

        bufferedOutputStream.close();
        outputStream.close();
    }
}
