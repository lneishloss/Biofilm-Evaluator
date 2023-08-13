package edu.temple.vrl.biofilm_evaluator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

//This class has some useful helper functions for batch processing
public class Testing {
    public static void main(String[] args) throws IOException {
        double width;
        double height;
        double depth;
        int x;
        int y;
        int z;

        width = 0.2405002;
        height = 0.2405002;
        depth = 0.2517703;
        x = 1024;
        y = 1024;
        z = 134;
        timePoints( "102822_t0", 0, width, height, depth, x, y, z, "treated_data");

        width = 0.2405002;
        height = 0.2405002;
        depth = 0.2937320;
        x = 1024;
        y = 1024;
        z = 134;
        timePoints( "102822_t0_1", 0, width, height, depth, x, y, z, "treated_data");

        width = 0.2405002;
        height = 0.2405002;
        depth = 0.2937320;
        x = 1024;
        y = 1024;
        z = 134;
        timePoints( "102822_t0_2", 0, width, height, depth, x, y, z, "treated_data");

        width = 0.2405002;
        height = 0.2405002;
        depth = 0.2517703;
        x = 1024;
        y = 1024;
        z = 92;
        timePoints( "102822_t1", 1, width, height, depth, x, y, z, "treated_data");

        width = 0.2405002;
        height = 0.2405002;
        depth = 0.2517703;
        x = 1024;
        y = 1024;
        z = 114;
        timePoints( "102822_t1_1", 1, width, height, depth, x, y, z, "treated_data");

        width = 0.2405002;
        height = 0.2405002;
        depth = 0.2517703;
        x = 1024;
        y = 1024;
        z = 116;
        timePoints( "102822_t1_2", 1, width, height, depth, x, y, z, "treated_data");

        width = 0.2405002;
        height = 0.2405002;
        depth = 0.2517703;
        x = 1024;
        y = 1024;
        z = 111;
        timePoints( "102822_t1_3", 1, width, height, depth, x, y, z, "treated_data");

        width = 0.2405002;
        height = 0.2405002;
        depth = 0.2517703;
        x = 1024;
        y = 1024;
        z = 108;
        timePoints( "102822_t2", 2, width, height, depth, x, y, z, "treated_data");

        width = 0.2405002;
        height = 0.2405002;
        depth = 0.2517703;
        x = 1024;
        y = 1024;
        z = 107;
        timePoints( "102822_t2_1", 2, width, height, depth, x, y, z, "treated_data");

        width = 0.2405002;
        height = 0.2405002;
        depth = 0.2517703;
        x = 1024;
        y = 1024;
        z = 106;
        timePoints( "102822_t2_2", 2, width, height, depth, x, y, z, "treated_data");

        width = 0.2405002;
        height = 0.2405002;
        depth = 0.2517703;
        x = 1024;
        y = 1024;
        z = 137;
        timePoints( "102822_t3", 3, width, height, depth, x, y, z, "treated_data");

        width = 0.2405002;
        height = 0.2405002;
        depth = 0.2517703;
        x = 1024;
        y = 1024;
        z = 71;
        timePoints( "102822_t3_1", 3, width, height, depth, x, y, z, "treated_data");

        width = 0.2405002;
        height = 0.2405002;
        depth = 0.5035405;
        x = 1024;
        y = 1024;
        z = 106;
        timePoints( "102822_t3_2", 3, width, height, depth, x, y, z, "treated_data");

        width = 0.2405002;
        height = 0.2405002;
        depth = 0.2937320;
        x = 1024;
        y = 1024;
        z = 106;
        timePoints( "102822_t3_3", 3, width, height, depth, x, y, z, "treated_data");




    }

    public static void computeThresholds(String s, double width, double height, double depth, int x, int y, int z, String filename) throws IOException {
        String name = s;
        int numSurfaces30000 = 0;
        int numSurfaces60000 = 0;
        double vol30000 = 0.0;
        double vol60000 = 0.0;
        for(int i = 3; i <= 6; i += 3){
            //long start = System.nanoTime();
            BiofilmEvaluator be = new BiofilmEvaluator(width, height, depth, x, y, z);
            Integer threshold = i * 10000;
            s = name;
            String obj = s + "_threshold_" + threshold.toString() + ".obj";
            String txt = s + "_threshold_" + threshold.toString() + ".txt";
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
                be.exportVolumeData(filename + "_30000.csv", s);
                numSurfaces30000 = be.surfaces.size();
                vol30000 = be.totalVolume;
            }
            else{
                be.exportVolumeData(filename + "_60000.csv", s);
                numSurfaces60000 = be.surfaces.size();
                vol60000 = be.totalVolume;
            }
            //long end = System.nanoTime();
            //System.out.println((end - start) / 1000000000.0 / 60.0);
        }
        printRatio(numSurfaces30000, numSurfaces60000, vol30000, vol60000, s, filename);
    }

    public static void timePoints(String s, int j, double width, double height, double depth, int x, int y, int z, String filename) throws IOException{
        String name = s;
        int numSurfaces30000 = 0;
        int numSurfaces60000 = 0;
        double vol30000 = 0.0;
        double vol60000 = 0.0;
        for(int i = 3; i <= 6; i += 3){
            //long start = System.nanoTime();
            BiofilmEvaluator be = new BiofilmEvaluator(width, height, depth, x, y, z);
            Integer threshold = i * 10000;
            s = name;
            String obj = s + "_threshold_" + threshold.toString() + ".obj";
            String txt = s + "_threshold_" + threshold.toString() + ".txt";
            String tif = s + ".tif";
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
                be.exportTimePointsData(filename + "_30000.csv", s, j);
                numSurfaces30000 = be.surfaces.size();
                vol30000 = be.totalVolume;
            }
            else{
                be.exportTimePointsData(filename + "_60000.csv", s, j);
                numSurfaces60000 = be.surfaces.size();
                vol60000 = be.totalVolume;
            }
            //long end = System.nanoTime();
            //System.out.println((end - start) / 1000000000.0 / 60.0);
        }
        printRatio(numSurfaces30000, numSurfaces60000, vol30000, vol60000, s, filename);

    }

    public static void signedVolumeTesting(String s, int j, double width, double height, double depth, int x, int y, int z, String filename) throws IOException {
        String name = s;
        for(int i = 3; i <= 6; i += 3){
            //long start = System.nanoTime();
            BiofilmEvaluator be = new BiofilmEvaluator(width, height, depth, x, y, z);
            Integer threshold = i * 10000;
            s = name;
            String obj = s + "_threshold_" + threshold.toString() + ".obj";
            String txt = s + "_threshold_" + threshold.toString() + ".txt";
            String tif = s + ".tif";
            be.importMeshData(new File(obj), new File(txt));
            be.generateSurfaces(0);
            be.generateQuadtree(100);
            be.computeSurfaceMeasurements();
            //be.exportOBJ(s + "_threshold_" + threshold.toString() + "_cleaned.obj");
            //be.determineInteriorPoints();
            //be.importImage(new File(tif));
            //be.computeSurfaceDensities();
            //be.exportBiofilmData("biofilms.csv");
            //be.exportSurfacesData("surfaces.csv");
            if(i == 3){
                be.exportTimePointsData(filename + "_30000.csv", s, j);
            }
            else{
                be.exportTimePointsData(filename + "_60000.csv", s, j);
            }
            //long end = System.nanoTime();
            //System.out.println((end - start) / 1000000000.0 / 60.0);
        }
    }

    public static void printRatio(int num30000, int num60000, double vol30000, double vol60000, String s, String filename) throws IOException{
        FileOutputStream outputStream;
        String output = "";
        byte[] bytes;

        if(new File(filename + "_ratios.csv").isFile()){
            outputStream = new FileOutputStream(filename + "_ratios.csv", true);
        }
        else{
            outputStream = new FileOutputStream(filename + "_ratios.csv");
            output = "Biofilm, # of 30000 Structures, # of 60000 Structures, Ratio # 30000 / # 60000, Total Vol 30000, Total Vol 60000, Ratio Vol 30000 / Vol 60000\n";
        }
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

        output = output.concat(s + ", "  + num30000 + ", " + num60000 + ", " + (double)num30000 / (double)num60000 + ", " + vol30000 +  ", " + vol60000 + ", " + vol30000 / vol60000 + "\n");

        bytes = output.getBytes();
        bufferedOutputStream.write(bytes);

        bufferedOutputStream.close();
        outputStream.close();
    }

}
