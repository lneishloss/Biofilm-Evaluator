package edu.temple.vrl.biofilm_evaluator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class has some useful helper methods for batch processing of biofilm data.
 * @author Logan Neishloss
 */
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
        depth = 0.5035405;
        x = 1024;
        y = 1024;
        z = 98;

        String obj = "072222_series6_threshold_30000.obj";
        String txt = "072222_series6_threshold_30000.txt";
        String tif = "072222_series6.tif";

        printMethodTimes(width, height, depth, x, y, z, obj, txt, tif);
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
            DataExport d = new DataExport(be.getSurfaces());
            if(i == 3){
                d.exportSurfacesData(filename + "_30000.csv");
                numSurfaces30000 = be.surfaces.size();
                vol30000 = be.totalVolume;
            }
            else{
                d.exportSurfacesData(filename + "_60000.csv");
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
            DataExport d = new DataExport(be.getSurfaces());
            if(i == 3){
                d.exportTimePointsData(filename + "_30000.csv", s, j);
                numSurfaces30000 = be.surfaces.size();
                vol30000 = be.totalVolume;
            }
            else{
                d.exportTimePointsData(filename + "_60000.csv", s, j);
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
            DataExport d = new DataExport(be.getSurfaces());
            if(i == 3){
                d.exportTimePointsData(filename + "_30000.csv", s, j);
            }
            else{
                d.exportTimePointsData(filename + "_60000.csv", s, j);
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

    public static void printMethodTimes(double width, double height, double depth, int x, int y, int z, String obj, String txt, String tif) throws IOException {
        long init = 0;
        long importMesh = 0;
        long importImage = 0;
        long genSurfaces = 0;
        long genQuadtree = 0;
        long compSurface = 0;
        long detInt = 0;
        long compDens = 0;
        long print = 0;
        long startTime, endTime;

        startTime = System.currentTimeMillis();
        BiofilmEvaluator be = new BiofilmEvaluator(width, height, depth, x, y, z);
        endTime = System.currentTimeMillis();
        init = endTime - startTime;

        startTime = System.currentTimeMillis();
        be.importMeshData(new File(obj), new File(txt));
        endTime = System.currentTimeMillis();
        importMesh = endTime - startTime;

        startTime = System.currentTimeMillis();
        be.importImage(new File(tif));
        endTime = System.currentTimeMillis();
        importImage = endTime - startTime;

        startTime = System.currentTimeMillis();
        be.generateSurfaces(0);
        endTime = System.currentTimeMillis();
        genSurfaces = endTime - startTime;

        startTime = System.currentTimeMillis();
        be.generateQuadtree(100);
        endTime = System.currentTimeMillis();
        genQuadtree = endTime - startTime;

        startTime = System.currentTimeMillis();
        be.computeSurfaceMeasurements();
        endTime = System.currentTimeMillis();
        compSurface = endTime - startTime;

        startTime = System.currentTimeMillis();
        be.determineInteriorPoints();
        endTime = System.currentTimeMillis();
        detInt = endTime - startTime;

        startTime = System.currentTimeMillis();
        be.computeSurfaceDensities();
        endTime = System.currentTimeMillis();
        compDens = endTime - startTime;

        startTime = System.currentTimeMillis();
        System.out.println(be.printRigidStructuresData());
        endTime = System.currentTimeMillis();
        print = endTime - startTime;

        System.out.println("Init: " + init);
        System.out.println("Import Mesh: " + importMesh);
        System.out.println("Import Image: " + importImage);
        System.out.println("Generate Surfaces: " + genSurfaces);
        System.out.println("Generate Quadtree: " + genQuadtree);
        System.out.println("Compute Surface Measurements: " + compSurface);
        System.out.println("Determine Interior Points: " + detInt);
        System.out.println("Compute Surface Densities: " + compDens);
        System.out.println("Print: " + print);
    }

}
