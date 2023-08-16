package edu.temple.vrl.biofilm_evaluator;

import javax.vecmath.Vector3d;
import java.util.ArrayList;

/**
 * Defines a single, contiguous triangular mesh.
 * @author Logan Neishloss
 */
public class Surface {
    private int i;
    private double minX, minY, minZ, maxX, maxY, maxZ;
    private final ArrayList<Triangle> mesh;
    private int numVoxels = 0;

    private double volume;
    private double surfaceArea;

    private double grayscaleMass;
    private double density;

    /**
     *
     * @param mesh ArrayList of triangles
     */
    public Surface(ArrayList<Triangle> mesh, int i){
        this.mesh = mesh;
        changeIndex(i);
        this.i = i;
        setExtrema();
    }

    /**
     *
     * @return ArrayList of triangles
     */
    public ArrayList<Triangle> getSurface(){
        return mesh;
    }

    /**
     *
     * @return Number of triangles
     */
    public Integer size(){
        return mesh.size();
    }

    /**
     *
     * @return min x value
     */
    public double getMinX(){
        return minX;
    }

    /**
     *
     * @return min y value
     */
    public double getMinY() { return minY; }

    /**
     *
     * @return min z value
     */
    public double getMinZ() { return minZ; }

    /**
     *
     * @return max x value
     */
    public double getMaxX() {
        return maxX;
    }

    /**
     *
     * @return max y value
     */
    public double getMaxY() {
        return maxY;
    }

    /**
     *
     * @return max z value
     */
    public double getMaxZ() { return maxZ; }


    /**
     *
     * @return number of voxels in surface
     */
    public int getNumVoxels() { return numVoxels; }

    /**
     *
     * @return index of surface
     */
    public int getIndex() { return i; }


    /**
     * Increments numVoxels
     */
    public void addVoxel(){
        numVoxels++;
    }

    /**
     * Changes the index of every triangle in the Surface
     * @param i index
     */
    public void changeIndex(int i){
        this.i = i;
        for(Triangle t : mesh){
            t.surfaceIndex = i;
        }
    }

    public void calculateSurfaceArea(){
        surfaceArea = 0;
        for(Triangle t : mesh){
            surfaceArea += t.getArea();
        }
    }

    public void calculateVolume(){
        double signedVolume = 0.0;
        for(Triangle t : mesh){
            double tetrahedronVolume;

            ArrayList<Node> vertices = t.getVertices();
            Vector3d a = new Vector3d(vertices.get(0).x, vertices.get(0).y, vertices.get(0).z);
            Vector3d b = new Vector3d(vertices.get(1).x, vertices.get(1).y, vertices.get(1).z);
            Vector3d c = new Vector3d(vertices.get(2).x, vertices.get(2).y, vertices.get(2).z);

            Vector3d cross = new Vector3d();
            cross.cross(b, c);
            tetrahedronVolume = Math.abs((1.0/6.0) * cross.dot(a));

            if(t.getNormalVector().dot(a) < 0){
                tetrahedronVolume *= -1;
            }
            signedVolume += tetrahedronVolume;
        }
        volume = signedVolume;
    }

    public double getSurfaceArea(){
        return surfaceArea;
    }

    public double getVolume(){
        return volume;
    }

    public void addHole(ArrayList<Triangle> triangles){
        for(Triangle t : triangles){
            t.surfaceIndex = i;
            mesh.add(t);
        }
    }

    public void addMass(double mass){
        grayscaleMass += mass;
        density = grayscaleMass / numVoxels;
    }

    public double getDensity(){
        return density;
    }

    /**
     *
     * @return String
     */
    @Override
    public String toString(){
        return size().toString();
    }

    private void setExtrema(){
        maxX = -300;
        maxY = -300;
        maxZ = -300;
        minX = 300;
        minY = 300;
        minZ = 300;
        for(Triangle t : mesh){
            for(Node n : t.getVertices()){
                if(n.x > maxX){
                    maxX = n.x;
                }
                else if(n.x < minX){
                    minX = n.x;
                }
                if(n.y > maxY){
                    maxY = n.y;
                }
                else if(n.y < minY){
                    minY = n.y;
                }
                if(n.z > maxZ){
                    maxZ = n.z;
                }
                else if(n.z < minZ){
                    minZ = n.z;
                }

            }
        }
    }


}
