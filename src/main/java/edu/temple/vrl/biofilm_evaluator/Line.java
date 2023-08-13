package edu.temple.vrl.biofilm_evaluator;

import javax.vecmath.Vector3d;

/**
 * @author Logan Neishloss
 */
public class Line {
    Vector3d R;
    double x, y, z1, z2;
    boolean edgeIntersection = false;
    double xAtEdge,yAtEdge;

    /**
     * Constructor
     * @param x x value
     * @param y y value
     * @param z1 z lower bound
     * @param z2 z upper bound
     */
    public Line(double x, double y, double z1, double z2){
        this.x = x;
        this.y = y;
        this.z1 = z1;
        this.z2 = z2;
        R = new Vector3d(x, y, z2 - z1);
    }

    /**
     * Calculates intersection of a line and a triangle.
     * @param t Triangle
     * @return True if Line and t intersect
     */
    public boolean intersection(Triangle t){
        if(z1 > t.getMaxZ()){
            return false;
        }
        Vector3d dS21 = new Vector3d(t.getVertices().get(1).x - t.getVertices().get(0).x, t.getVertices().get(1).y - t.getVertices().get(0).y, t.getVertices().get(1).z - t.getVertices().get(0).z);
        Vector3d dS31 = new Vector3d(t.getVertices().get(2).x - t.getVertices().get(0).x, t.getVertices().get(2).y - t.getVertices().get(0).y, t.getVertices().get(2).z - t.getVertices().get(0).z);
        Vector3d n = new Vector3d();
        n.cross(dS21, dS31);
        n.normalize();
        Vector3d r1 = new Vector3d(x, y, z1);
        Vector3d r2 = new Vector3d(x, y, z2);
        Vector3d R = new Vector3d(0, 0, r2.z - r1.z);

        Vector3d diff = new Vector3d(r1.x - t.getVertices().get(0).x, r1.y - t.getVertices().get(0).y, r1.z - t.getVertices().get(0).z);
        double prod1 = diff.dot(n);
        double prod2 = R.dot(n);
        double prod3 = prod1 / prod2;

        Vector3d M = new Vector3d(r1.x - R.x * prod3, r1.y - R.y * prod3, r1.z - R.z *prod3);

        if(z1 - M.z > 0){
            return false;
        }

        //long start = System.nanoTime();
        int intersection = t.barycentricCoordinates(M);
        //long end = System.nanoTime();
        //bTime += end - start;

        //long start2 = System.nanoTime();
        //int intersection = t.overlappingAreas(M);
        //long end2 = System.nanoTime();
        //oTime += end2 - start2;

        if(intersection == 0){
            return false;
        }
        else if(intersection == 1){
            return true;
        }
        else{
            if(edgeIntersection && xAtEdge == x && yAtEdge == y){
                return false;
            }
            edgeIntersection = true;
            xAtEdge = x;
            yAtEdge = y;
            return true;
        }
    }
}
