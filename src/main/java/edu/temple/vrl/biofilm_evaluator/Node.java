package edu.temple.vrl.biofilm_evaluator;

import javax.vecmath.Vector3d;
import java.util.ArrayList;

/**
 * @author Logan Neishloss
 */
public class Node implements Comparable<Node>{
    int n;
    double x, y, z;
    double normalx, normaly, normalz;
    int numConnections;
    ArrayList<Node> connections = new ArrayList<>();

    /**
     * Constructor
     * @param n index
     * @param x x
     * @param y y
     * @param z z
     */
    Node(int n, double x, double y, double z){
        this.n = n;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Adds a node to the list of connected nodes
     * @param node node
     */
    public void initializeConnection(Node node){
        connections.add(node);
    }

    /**
     *
     * @param numConnections number of connected nodes
     */
    public void setNumConnections(int numConnections){
        this.numConnections = numConnections;
    }

    /**
     * Sets value of normal vector
     * @param x x
     * @param y y
     * @param z z
     */
    public void setNormal(double x, double y, double z){
        normalx = x;
        normaly = y;
        normalz = z;
    }


    /**
     *
     * @return Vector from origin to node
     */
    public Vector3d getVertex(){
        return new Vector3d(x, y, z);
    }

    /**
     *
     * @return ArrayList of connected nodes
     */
    public ArrayList<Node> getConnections(){
        return connections;
    }

    /**
     *
     * @return index n
     */
    public int getIndex(){
        return n;
    }

    /**
     *
     * @return String of normal vector
     */
    public String getNormal(){
        return "vn " + normalx + " " + normaly + " " + normalz + "\n";
    }

    /**
     *
     * @return String
     */
    @Override
    public String toString(){
        return x + " " + y + " " + z + "\n";
    }

    /**
     *
     * @param m Node
     * @return int
     */
    @Override
    public int compareTo(Node m){
        return getIndex() - m.getIndex();
    }
}
