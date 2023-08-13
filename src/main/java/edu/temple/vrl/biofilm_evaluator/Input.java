package edu.temple.vrl.biofilm_evaluator;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

//512 x 512 x 65 , .48147 x .48247 x .503541
public class Input {
    static ArrayList<Node> nodes = new ArrayList<>();
    static boolean[] visited;

    public static void main(String[] args) throws IOException {
        Scanner scnr = new Scanner(new FileReader("newmesh.txt"));
        int numNodes = scnr.nextInt();

        visited = new boolean[numNodes];
        for(int i = 0; i < numNodes; i++){
            visited[i] = false;
        }

        ArrayList<Triangle> faces = new ArrayList<>();
        ArrayList<Surface> surfaces = new ArrayList<>();

        for (int i = 0; i < numNodes; i++) {
            nodes.add(new Node(scnr.nextInt(), scnr.nextDouble(), scnr.nextDouble(), scnr.nextDouble()));
            scnr.nextInt();
        }

        for (int i = 0; i < numNodes; i++) {
            scnr.nextInt();
            int numConnections = scnr.nextInt();
            nodes.get(i).setNumConnections(numConnections);
            for (int j = 0; j < numConnections; j++) {
                nodes.get(i).initializeConnection(nodes.get(scnr.nextInt()));
            }
        }

        scnr = new Scanner(new FileReader("newmesh.obj"));
        scnr.nextLine();scnr.nextLine();scnr.nextLine();scnr.nextLine();
        for(int i = 0; i < numNodes * 2; i++){
            scnr.next();
            nodes.get(i / 2).setNormal(scnr.nextDouble(), scnr.nextDouble(), scnr.nextDouble());
        }
        scnr.nextLine();scnr.nextLine();scnr.nextLine();
        while(scnr.hasNext()){
            scnr.next();
            Triangle t = new Triangle(nodes.get(scnr.nextInt() - 1), nodes.get(scnr.nextInt() - 1), nodes.get(scnr.nextInt() - 1    ));
            faces.add(t);
        }

        for(Node n : nodes){
            int count = 1;
            if(!visited[n.getIndex()]){
                ArrayList<Node> connected = new ArrayList<>();
                connected = getConnectedComponents(n, connected);
                Surface s = new Surface(getFacesFromNodes(connected, faces), count);
                if(s.size() > 50){
                    surfaces.add(s);
                    count++;
                }
            }
        }

        for(Surface s : surfaces){
            s.getSurface().sort(Triangle::compareTo);
        }

        //nodes = getNodesFromSurfaces(surfaces);

        exportOBJ(nodes, surfaces);

        long start = System.currentTimeMillis();

        boolean[][][] interiorPoints = determineInteriorPoints(surfaces);

        exportInteriorExteriorPoints(interiorPoints);

        long end = System.currentTimeMillis();

        System.out.println(end - start);

    }

    public static ArrayList<Node> getConnectedComponents(Node n, ArrayList<Node> connected){
        visited[n.getIndex()] = true;
        connected.add(n);
        for(Node m : n.getConnections()){
            if(!visited[m.getIndex()]){
                getConnectedComponents(m, connected);
            }
        }

        return connected;
    }

    public static ArrayList<Triangle> getFacesFromNodes(ArrayList<Node> connected, ArrayList<Triangle> faces){
        ArrayList<Triangle> connectedFaces = new ArrayList<>();

        //Collections.reverse(faces);

        for(Triangle f : faces){
            Node n = f.getVertices().get(0);
            for(Node m : connected){
                if(n.equals(m)){
                    connectedFaces.add(f);
                }
            }
        }

        return connectedFaces;
    }

    public static void exportOBJ(ArrayList<Node> nodes, ArrayList<Surface> surfaces) throws IOException {
        FileOutputStream outputStream = new FileOutputStream("output.obj");
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

        String output = "#Vertices\n";
        byte[] bytes = output.getBytes();
        bufferedOutputStream.write(bytes);

        String str;
        for(Node n : nodes){
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

    public static void exportInteriorExteriorPoints(boolean[][][] interiorPoints) throws IOException{
        FileOutputStream outputStream = new FileOutputStream("interior_exterior.txt");
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

        String s;
        byte[] bytes;
        for(int k = 0; k < 65; k++){
            s = "z = " + k + "\n";
            bytes = s.getBytes();
            bufferedOutputStream.write(bytes);
            for(int i = 0; i < 512; i++){
                for(int j = 0; j < 512; j++){
                    if(interiorPoints[i][j][k]){
                        s = "1 ";
                    }
                    else{
                        s = "0 ";
                    }
                    bytes = s.getBytes();
                    bufferedOutputStream.write(bytes);
                }
                s = "\n";
                bytes = s.getBytes();
                bufferedOutputStream.write(bytes);
            }
        }


    }

    /*
    public static ArrayList<Node> getNodesFromSurfaces(ArrayList<Surface> surfaces){
        HashSet<Node> nodes = new HashSet<>();
        for(Surface s : surfaces){
            for(Triangle f : s.getSurface()){
                nodes.addAll(f.getVertices());
            }
        }
        ArrayList<Node> vertices = new ArrayList<>(nodes);
        vertices.sort(Node::compareTo);

        return new ArrayList<>(nodes);
    }
     */

    public static ArrayList<Triangle> getTrianglesFromSurfaces(ArrayList<Surface> surfaces){
        HashSet<Triangle> faces = new HashSet<>();
        for(Surface s : surfaces){
            faces.addAll(s.getSurface());
        }
        ArrayList<Triangle> triangles = new ArrayList<>(faces);
        triangles.sort(Triangle::compareTo);

        return triangles;
    }

    //4324351 for 1 3d mesh stack
    public static boolean[][][] determineInteriorPoints(ArrayList<Surface> surfaces){
        boolean[][][] interiorPoints = new boolean[512][512][65];
        final double width = .48147;
        final double height = .48147;
        final double depth = .503541;

        for(int i = 1; i <= 5; i++){
            for(int j = 1; j <= 512; j++){
                for(int k = 1; k <= 512; k++){
                    int intersections = 0;
                    Line l = new Line((j - 512.0 / 2.0) * width, (k - 512.0 / 2.0) * height, (i - 65.0 / 2.0) * depth, (65.0 / 2.0) * depth);
                    for(Surface s : surfaces){
                        if((i - 65.0 / 2.0) * depth > s.getMinZ() && (i - 65.0 / 2.0) * depth < s.getMaxZ()){
                            if((j - 512.0 / 2.0) * width > s.getMinX() && (j - 512.0 / 2.0) * width < s.getMaxX()){
                                if((k - 512.0 / 2.0) * height > s.getMinY() && (k - 512.0 / 2.0) * height < s.getMaxY()){
                                    for(Triangle t : s.getSurface()){
                                        if(l.intersection(t)) {
                                            intersections++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    interiorPoints[j - 1][k - 1][i - 1] = intersections % 2 != 0;
                }
            }
        }

        return interiorPoints;
    }

    public static void computeTowerDensity(){

    }

}
