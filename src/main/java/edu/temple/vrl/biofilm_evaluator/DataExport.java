package edu.temple.vrl.biofilm_evaluator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Class to clean up BiofilmEvaluator class by separating all methods related to exporting data.
 * @author Logan Neishloss
 */
public class DataExport {

    public void printBiofilmData(double interiorDensity, double exteriorDensity, double totalSurfaceArea, double totalVolume, double sampleSpaceSize, double percentOfSampleSpace){
        System.out.println("Interior Density, Exterior Density, Total Surface Area, Total Volume, Sample Space, Percent Interior");

        String s = "";
        s = s.concat(interiorDensity + ", " +  exteriorDensity + ", " + totalSurfaceArea + ", " + totalVolume + ", " + sampleSpaceSize + ", " + percentOfSampleSpace);

        System.out.println(s);
    }

    public void exportBiofilmData(String filename, double interiorDensity, double exteriorDensity, double totalSurfaceArea, double totalVolume, double sampleSpaceSize, double percentOfSampleSpace) throws IOException {
        FileOutputStream outputStream;
        if(new File(filename).isFile()){
            outputStream = new FileOutputStream(filename, true);
        }
        else{
            outputStream = new FileOutputStream(filename);
        }
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

        String output = "Interior Density, Exterior Density, Total Surface Area, Total Volume, Sample Space, Percent Interior\n";
        output = output.concat(interiorDensity + ", " +  exteriorDensity + ", " + totalSurfaceArea + ", " + totalVolume + ", " + sampleSpaceSize + ", " + percentOfSampleSpace + "\n");
        byte[] bytes = output.getBytes();
        bufferedOutputStream.write(bytes);

        bufferedOutputStream.close();
        outputStream.close();
    }

    public void printSurfacesData(ArrayList<Surface> surfaces){
        for(int i = 1; i < surfaces.size(); i++){
            System.out.print("Surface Area " + i + ", " + "Volume " + i + ", " + "Density " + i + ", ");
        }
        System.out.println("Surface Area " + surfaces.size() + ", " + "Volume " + surfaces.size() + ", " + "Density " + surfaces.size());

        for(Surface s : surfaces){
            if(s.getIndex() != surfaces.size()){
                System.out.print(s.getSurfaceArea() + ", " + s.getVolume() + ", " + s.getDensity() + ", ");
            }
            else{
                System.out.println(s.getSurfaceArea() + ", " + s.getVolume() + ", " + s.getDensity());
            }
        }

    }

    public void exportSurfacesData(String filename, ArrayList<Surface> surfaces) throws IOException{
        FileOutputStream outputStream;
        if(new File(filename).isFile()){
             outputStream = new FileOutputStream(filename, true);
        }
        else{
            outputStream = new FileOutputStream(filename);
        }
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

        String output = "";
        for(int i = 1; i < surfaces.size(); i++){
            output = output.concat("Surface Area " + i + ", " + "Volume " + i + ", " + "Density " + i + ", ");
        }
        output = output.concat("Surface Area " + surfaces.size() + ", " + "Volume " + surfaces.size() + ", " + "Density " + surfaces.size() + "\n");

        byte[] bytes = output.getBytes();
        bufferedOutputStream.write(bytes);

        output = "";
        for(Surface s : surfaces){
            if(s.getIndex() != surfaces.size()){
                output = output.concat(s.getSurfaceArea() + ", " + s.getVolume() + ", " + s.getDensity() + ", ");
            }
            else{
                output = output.concat(s.getSurfaceArea() + ", " + s.getVolume() + ", " + s.getDensity() + "\n");
            }
        }

        bytes = output.getBytes();
        bufferedOutputStream.write(bytes);

        bufferedOutputStream.close();
        outputStream.close();
    }

    public void exportSurfaceDataVertically(String series, String filename, ArrayList<Surface> surfaces) throws IOException{
        FileOutputStream outputStream;
        String output = "";
        byte[] bytes;

        if(new File(filename).isFile()){
            outputStream = new FileOutputStream(filename, true);
        }
        else{
            outputStream = new FileOutputStream(filename);
            output = "Structure, Surface Area, Volume, Density\n";
        }
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

        for(Surface s : surfaces){
            output = output.concat(series + ", " + s.getSurfaceArea() + ", " + s.getVolume() + ", " + s.getDensity() + "\n");
        }

        bytes = output.getBytes();
        bufferedOutputStream.write(bytes);

        bufferedOutputStream.close();
        outputStream.close();
    }

    public void exportTimePointsData(String filename, String structure, int timepoint, ArrayList<Surface> surfaces) throws IOException{
        FileOutputStream outputStream;
        String output = "";
        byte[] bytes;

        if(new File(filename).isFile()){
            outputStream = new FileOutputStream(filename, true);
        }
        else{
            outputStream = new FileOutputStream(filename);
            output = "Structure, TimePoint, Surface Area, Volume, Density\n";
        }
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

        for(Surface s : surfaces){
            output = output.concat(structure + ", " + timepoint + ", " + s.getSurfaceArea() + ", " + s.getVolume() + ", " + s.getDensity() + "\n");
        }

        bytes = output.getBytes();
        bufferedOutputStream.write(bytes);

        bufferedOutputStream.close();
        outputStream.close();
    }

    public void exportVolumeData(String filename, String structure, ArrayList<Surface> surfaces) throws IOException{
        FileOutputStream outputStream;
        String output = "";
        byte[] bytes;

        if(new File(filename).isFile()){
            outputStream = new FileOutputStream(filename, true);
        }
        else{
            outputStream = new FileOutputStream(filename);
            output = "Structure, Surface Area, Volume\n";
        }
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

        for(Surface s : surfaces){
            output = output.concat(structure + ", " + s.getSurfaceArea() + ", " + s.getVolume() + "\n");
        }

        bytes = output.getBytes();
        bufferedOutputStream.write(bytes);

        bufferedOutputStream.close();
        outputStream.close();
    }

    public void exportOBJ(String filename, ArrayList<Node> vertices, ArrayList<Surface> surfaces) throws IOException{
        FileOutputStream outputStream = new FileOutputStream(filename);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

        String output = "#Vertices\n";
        byte[] bytes = output.getBytes();
        bufferedOutputStream.write(bytes);

        String str;
        for(Node n : vertices){
            str = "v " + n.toString();
            bytes = str.getBytes();
            bufferedOutputStream.write(bytes);

            str = n.getNormal();
            bytes = str.getBytes();
            bufferedOutputStream.write(bytes);
        }

        output = "\n#Triangles\n";
        bytes = output.getBytes();
        bufferedOutputStream.write(bytes);

        ArrayList<Triangle> triangles = getTrianglesFromSurfaces(surfaces);

        for(Triangle t : triangles){
            str = t.toString();
            bytes = str.getBytes();
            bufferedOutputStream.write(bytes);
        }

        bufferedOutputStream.close();
        outputStream.close();
    }

    public void exportInteriorMap(int[][][] interiorPoints, int x, int y, int z) throws IOException{
        BufferedImage[] images = new BufferedImage[z];
        for(int k = 0; k < z; k++){
            int count = 0;
            byte[] bytes = new byte[x * y];
            for(int j = 0; j < y; j++){
                for(int i = 0; i < x; i++){
                    if(interiorPoints[i][j][k] > 0){
                        bytes[count] = (byte)0xFF;
                        //count++;
                    }
                    count++;
                }
            }
            BufferedImage image = new BufferedImage(x, y, BufferedImage.TYPE_BYTE_GRAY);
            byte[] array = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            System.arraycopy(bytes, 0, array, 0, array.length);
            image.setData(Raster.createRaster(image.getSampleModel(), new DataBufferByte(array, array.length), new Point()));
            images[k] = image;
        }

        FileOutputStream fos = new FileOutputStream("imageOutput.tif");
        ImageWriter writer = ImageIO.getImageWritersByFormatName("TIFF").next();

        try(ImageOutputStream output = ImageIO.createImageOutputStream(fos)){
            writer.setOutput(output);

            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

            writer.prepareWriteSequence(null);

            for(int k = 0; k < z; k++){
                writer.writeToSequence(new IIOImage(images[k], null, null), params);
            }

            writer.endWriteSequence();
        }

        writer.dispose();
    }

    private ArrayList<Triangle> getTrianglesFromSurfaces(ArrayList<Surface> surfaces){
        HashSet<Triangle> faces = new HashSet<>();
        for(Surface s : surfaces){
            faces.addAll(s.getSurface());
        }
        ArrayList<Triangle> triangles = new ArrayList<>(faces);
        triangles.sort(Triangle::compareTo);

        return triangles;
    }
}
