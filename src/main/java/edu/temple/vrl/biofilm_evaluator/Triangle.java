package edu.temple.vrl.biofilm_evaluator;

import javax.vecmath.Vector3d;
import java.util.ArrayList;

/**
 * Defines a triangle in a triangular mesh.
 * @author Logan Neishloss
 */
public class Triangle implements Comparable<Triangle>{
    private final ArrayList<Node> vertices = new ArrayList<>();
    private final Vector3d normalVector = new Vector3d();
    private final double minX;
    private final double maxX;
    private final double minY;
    private final double maxY;
    //private final double minZ;
    private final double maxZ;
    int surfaceIndex;

    /**
     * Triangle constructor. A Triangle object is defined by three Node objects.
     * @param n1 Node n1
     * @param n2 Node n2
     * @param n3 Node n3
     */
    public Triangle(Node n1, Node n2, Node n3){
        vertices.add(n1);
        vertices.add(n2);
        vertices.add(n3);

        setNormalVector();

        minX = Math.min(n1.x, Math.min(n2.x, n3.x));
        maxX = Math.max(n1.x, Math.max(n2.x, n3.x));
        minY = Math.min(n1.y, Math.min(n2.y, n3.y));
        maxY = Math.max(n1.y, Math.max(n2.y, n3.y));
        //minZ = Math.min(n1.z, Math.min(n2.z, n3.z));
        maxZ = Math.max(n1.z, Math.max(n2.z, n3.z));
    }

    /**
     *
     * @return ArrayList of Nodes
     */
    public ArrayList<Node> getVertices(){
        return vertices;
    }

    /**
     *
     * @return min x value
     */
    public double getMinX() { return minX; }

    /**
     *
     * @return max x value
     */
    public double getMaxX(){
        return maxX;
    }

    /**
     *
     * @return min y value
     */
    public double getMinY() { return minY; }

    /**
     *
     * @return max y value
     */
    public double getMaxY(){
        return maxY;
    }


    /*
    /**
     *
     * @return min z value
     */
    /*
    public double getMinZ() { return minZ; }
     */

    /**
     *
     * @return max z value
     */
    public double getMaxZ(){
        return maxZ;
    }


    private void setNormalVector(){
        Vector3d a = vertices.get(0).getVertex();
        Vector3d b = vertices.get(1).getVertex();
        Vector3d c = vertices.get(2).getVertex();

        Vector3d v1 = new Vector3d(a.x - b.x, a.y - b.y, a.z - b.z);
        Vector3d v2 = new Vector3d(a.x - c.x, a.y - c.y, a.z - c.z);

        normalVector.cross(v1, v2);

        normalVector.normalize();
    }

    /**
     *
     * @param M A vector from an arbitrary point to a point on the plane of the triangle
     * @return 0 if point at M lies outside the triangle, 1 if inside, 2 if exactly on an edge
     */
    public int barycentricCoordinates(Vector3d M){
        Vector3d a = new Vector3d(vertices.get(0).x, vertices.get(0).y, vertices.get(0).z);
        Vector3d b = new Vector3d(vertices.get(1).x, vertices.get(1).y, vertices.get(1).z);
        Vector3d c = new Vector3d(vertices.get(2).x, vertices.get(2).y, vertices.get(2).z);

        Vector3d ba = new Vector3d(b.x - a.x, b.y - a.y, b.z - a.z);
        Vector3d ca = new Vector3d(c.x - a.x, c.y - a.y, c.z - a.z);
        Vector3d pa = new Vector3d(M.x - a.x, M.y - a.y, M.z - a.z);
        Vector3d n = new Vector3d();
        n.cross(ba, ca);

        Vector3d ucrossw = new Vector3d();
        ucrossw.cross(ba, pa);
        Vector3d wcrossv = new Vector3d();
        wcrossv.cross(pa, ca);

        double gamma = ucrossw.dot(n) / n.dot(n);
        double beta = wcrossv.dot(n) / n.dot(n);
        double alpha = 1 - gamma - beta;


        if(alpha >= 0 && alpha <= 1 && beta >= 0 && beta <= 1 && gamma >= 0 && gamma <= 1){
            if(alpha == 0 || beta == 0 || gamma == 0){
                return 2;
            }
            else{
                return 1;
            }
        }
        else{
            return 0;
        }
    }

    /*
    public int overlappingAreas(Vector3d M){
        Vector3d a = new Vector3d(vertices.get(0).x, vertices.get(0).y, vertices.get(0).z);
        Vector3d b = new Vector3d(vertices.get(1).x, vertices.get(1).y, vertices.get(1).z);
        Vector3d c = new Vector3d(vertices.get(2).x, vertices.get(2).y, vertices.get(2).z);

        int l1, l2, l3;
        l1 = sameSideOfLine(a, b, M, c);
        l2 = sameSideOfLine(b, c, M, a);
        l3 = sameSideOfLine(c, a, M, b);

        if(l1 > 0 && l2 > 0 && l3 > 0){
            if(l1 == 2 || l2 == 2 || l3 == 2){
                return 2;
            }
            else{
                return 1;
            }
        }
        else{
            return 0;
        }
    }
     */

    /*
    private int sameSideOfLine(Vector3d l1, Vector3d l2, Vector3d a, Vector3d b){
        double s = ((((l1.y - l2.y) * (a.x - l2.x)) / (l1.x - l2.x) + l2.y - a.y) * (((l1.y - l2.y) * (b.x - l2.x)) / (l1.x - l2.x) + l2.y - b.y));
        if(s > 0){
            return 1;
        }
        else if(s == 0){
            return 2;
        }
        else{
            return 0;
        }
    }

     */

    public Vector3d getNormalVector(){return normalVector;}

    public double getArea(){
        Vector3d a = new Vector3d(vertices.get(0).x, vertices.get(0).y, vertices.get(0).z);
        Vector3d b = new Vector3d(vertices.get(1).x, vertices.get(1).y, vertices.get(1).z);
        Vector3d c = new Vector3d(vertices.get(2).x, vertices.get(2).y, vertices.get(2).z);

        Vector3d ba = new Vector3d(b.x - a.x, b.y - a.y, b.z - a.z);
        Vector3d ca = new Vector3d(c.x - a.x, c.y - a.y, c.z - a.z);

        Vector3d cross = new Vector3d();
        cross.cross(ba, ca);

        return cross.length() / 2;
    }

    @Override
    public String toString(){
        String s = "f ";
        for(Node n : vertices){
            int i = n.getIndex() + 1;
            s = s.concat(i + " ");
        }

        s = s.concat("\n");

        return s;
    }

    @Override
    public int compareTo(Triangle t){
        if(vertices.get(0).getIndex() - t.getVertices().get(0).getIndex() == 0){
            if(vertices.get(1).getIndex() - t.getVertices().get(1).getIndex() == 0){
                if(vertices.get(2).getIndex() - t.getVertices().get(2).getIndex() == 0){
                    return 0;
                }
                else{
                    return vertices.get(2).getIndex() - t.getVertices().get(2).getIndex();
                }
            }
            else{
                return  t.getVertices().get(1).getIndex() - vertices.get(1).getIndex();
            }
        }
        else{
            return vertices.get(0).getIndex() - t.getVertices().get(0).getIndex();
        }
    }

    @Override
    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }

        if(!(obj instanceof Triangle)){
            return false;
        }

        Triangle t = (Triangle) obj;

        return t.getVertices().equals(getVertices());
    }
}
