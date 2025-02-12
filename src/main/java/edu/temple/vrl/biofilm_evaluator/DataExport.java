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
    private ArrayList<Surface> surfaces;

    public DataExport(){

    }
    public DataExport(ArrayList<Surface> surfaces){
        this.surfaces = surfaces;
    }

    /**
     *
     * @param interiorDensity
     * @param exteriorDensity
     * @param totalSurfaceArea
     * @param totalVolume
     * @param sampleSpaceSize
     * @param percentOfSampleSpace
     * @return Biofilm data formatted for use in VRL Studio Project.
     */
    public String biofilmDataVRL(double interiorDensity, double exteriorDensity, double totalSurfaceArea, double totalVolume, double sampleSpaceSize, double percentOfSampleSpace){
        String s = interiorDensity + ", " +  exteriorDensity + ", " + totalSurfaceArea + ", " + totalVolume + ", " + sampleSpaceSize + ", " + percentOfSampleSpace + "\n";
        System.out.println(s);

        return s;
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
        output = output.concat(interiorDensity + ", " +  exteriorDensity + ", " + totalSurfaceArea + ", " + totalVolume + ", " + sampleSpaceSize + ", " + percentOfSampleSpace);
        byte[] bytes = output.getBytes();
        bufferedOutputStream.write(bytes);

        bufferedOutputStream.close();
        outputStream.close();
    }

    /**
     *
     * @return Print data for each surface in the surfaces list, formatted for VRL Studio Project.
     */
    public String surfacesDataVRL(){
        String output = "Structure, Surface Area, Volume, Density\n";
        for(int i = 1; i <= surfaces.size(); i++){
            output = output.concat(i + ", " + surfaces.get(i - 1).getSurfaceArea() + ", " + surfaces.get(i - 1).getVolume() + ", " + surfaces.get(i - 1).getDensity() + "\n");
        }
        //System.out.println(output);
        return output;
    }

    public void exportSurfacesData(String filename) throws IOException{
        FileOutputStream outputStream;
        if(new File(filename).isFile()){
             outputStream = new FileOutputStream(filename, true);
        }
        else{
            outputStream = new FileOutputStream(filename);
        }
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

        String output = "Structure, Surface Area, Volume, Density\n";
        for(int i = 1; i <= surfaces.size(); i++){
            output = output.concat(i + ", " + surfaces.get(i - 1).getSurfaceArea() + ", " + surfaces.get(i - 1).getVolume() + ", " + surfaces.get(i - 1).getDensity() + "\n");
        }

        byte[] bytes = output.getBytes();
        bufferedOutputStream.write(bytes);

        bufferedOutputStream.close();
        outputStream.close();
    }

    public void exportTimePointsData(String filename, String structure, int timepoint) throws IOException{
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

    public void exportOBJ(String filename, ArrayList<Node> vertices) throws IOException{
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

    public void exportCellDensities(int[][][] cellDensities, int x, int y, int z) throws IOException{
        BufferedImage[] images = new BufferedImage[z];
        for(int k = 0; k < z; k++){
            int count = 0;
            byte[] bytes = new byte[x * y];
            for(int j = 0; j < y; j++){
                for(int i = 0; i < x; i++){
                    if(cellDensities[k][i][j] > 0){
                        bytes[count] = (byte)cellDensities[k][i][j];
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

        FileOutputStream fos = new FileOutputStream("cellDensities.tif");
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
