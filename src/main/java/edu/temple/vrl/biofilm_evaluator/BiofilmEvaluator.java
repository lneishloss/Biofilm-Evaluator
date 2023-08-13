package edu.temple.vrl.biofilm_evaluator;

import eu.mihosoft.vrl.annotation.ComponentInfo;

import java.io.*;
import java.util.*;

import edu.gcsc.vrl.densityvis.ImageVoxels;

/**
 * @author Logan Neishloss
 */
@ComponentInfo(name="BiofilmEvaluator", category = "Custom")
public class BiofilmEvaluator implements Serializable{
    private final ArrayList<Node> vertices = new ArrayList<>();
    private final ArrayList<Triangle> faces = new ArrayList<>();
    ArrayList<Surface> surfaces = new ArrayList<>();
    ArrayList<Surface> holes = new ArrayList<>();

    private int[][][] interiorPoints;
    private boolean[] visited;

    private final int x;
    private final int y;
    private final int z;

    private final double width;
    private final double height;
    private final double depth;

    private boolean[][] adjacencyMatrix;
    private ArrayList<HashSet<Integer>> adjacencyList;

    private double[] volumes;
    private double[] surfaceAreas;
    //private double[] holesSurfaceAreas;
    double totalVolume = 0.0;
    double totalSurfaceArea = 0.0;

    private int[][][] cellDensity;
    private int[][][] h2o2Density;

    private int numInteriorVoxels;
    private int numExteriorVoxels;
    double interiorDensity = 0.0;
    double exteriorDensity = 0.0;
    double interiorCellDensity = 0.0;
    double exteriorCellDensity = 0.0;
    double wightedInteriorH202 = 0.0;
    double weightedExteriorH202 = 0.0;

    double sampleSpaceSize;
    double percentOfSampleSpace;

    int count = 0;

    Quadtree quadtree;

    /**
     * Constructor.
     * @param width width (in image coordinates)
     * @param height height (in image coordinates)
     * @param depth depth (in image coordinates)
     * @param x x
     * @param y y
     * @param z z
     */
    public BiofilmEvaluator(double width, double height, double depth, int x, int y, int z){
        this.width = width;
        this.height = height;
        this.depth =  depth;

        this.x = x;
        this.y = y;
        this.z = z;
    }


    /**
     * From an .obj and .txt file exported from MorphoGraphX, store each vertex and face into respective ArrayLists.
     * Marginally faster, but tends to not be worth it as datasets large enough for the speedup to be appreciable
     * are too large to store the adjacency matrix in memory.
     * @param obj obj file path
     * @param txt txt file path
     * @throws IOException Throws IOException
     */
    public void importMeshDataAdjacencyMatrix(File obj, File txt) throws IOException {
        Scanner scnr = new Scanner(new FileReader(txt));
        int numVertices = scnr.nextInt();

        System.out.println(numVertices);
        visited = new boolean[numVertices];
        adjacencyMatrix = new boolean[numVertices][numVertices];

        for(int i = 0; i < numVertices; i++){
            vertices.add(new Node(scnr.nextInt(), scnr.nextDouble(), scnr.nextDouble(), scnr.nextDouble()));
            scnr.nextInt();
        }

        for (int i = 0; i < numVertices; i++) {
            scnr.nextInt();
            int numConnections = scnr.nextInt();
            vertices.get(i).setNumConnections(numConnections);
            for (int j = 0; j < numConnections; j++) {
                int connection = scnr.nextInt();
                vertices.get(i).initializeConnection(vertices.get(connection));
                adjacencyMatrix[i][connection] = true;
                adjacencyMatrix[connection][i] = true;
            }
        }

        scnr = new Scanner(new FileReader(obj));
        scnr.nextLine();scnr.nextLine();scnr.nextLine();scnr.nextLine();
        for(int i = 0; i < numVertices * 2; i++){
            scnr.next();
            vertices.get(i / 2).setNormal(scnr.nextDouble(), scnr.nextDouble(), scnr.nextDouble());
        }
        scnr.nextLine();scnr.nextLine();scnr.nextLine();
        while(scnr.hasNext()){
            scnr.next();
            Triangle t = new Triangle(vertices.get(scnr.nextInt() - 1), vertices.get(scnr.nextInt() - 1), vertices.get(scnr.nextInt() - 1));
            faces.add(t);
        }

    }

    /**
     * From an .obj and .txt file exported from MorphoGraphX, store each vertex and face into respective ArrayLists.
     * @param obj obj file path
     * @param txt txt file path
     * @throws IOException Throws IOException
     */
    public void importMeshData(File obj, File txt) throws IOException {
        Scanner scnr = new Scanner(new FileReader(txt));
        int numVertices = scnr.nextInt();

        System.out.println(numVertices);
        visited = new boolean[numVertices];
        adjacencyList = new ArrayList<>();

        for(int i = 0; i < numVertices; i++){
            vertices.add(new Node(scnr.nextInt(), scnr.nextDouble(), scnr.nextDouble(), scnr.nextDouble()));
            scnr.nextInt();
        }

        for (int i = 0; i < numVertices; i++) {
            scnr.nextInt();
            int numConnections = scnr.nextInt();
            vertices.get(i).setNumConnections(numConnections);
            HashSet<Integer> edges = new HashSet<>();
            for (int j = 0; j < numConnections; j++) {
                int connection = scnr.nextInt();
                vertices.get(i).initializeConnection(vertices.get(connection));
                edges.add(connection);
            }
            adjacencyList.add(edges);
        }

        scnr = new Scanner(new FileReader(obj));
        scnr.nextLine();scnr.nextLine();scnr.nextLine();scnr.nextLine();
        for(int i = 0; i < numVertices * 2; i++){
            scnr.next();
            vertices.get(i / 2).setNormal(scnr.nextDouble(), scnr.nextDouble(), scnr.nextDouble());
        }
        scnr.nextLine();scnr.nextLine();scnr.nextLine();
        while(scnr.hasNext()){
            scnr.next();
            Triangle t = new Triangle(vertices.get(scnr.nextInt() - 1), vertices.get(scnr.nextInt() - 1), vertices.get(scnr.nextInt() - 1));
            faces.add(t);
        }

    }

    /**
     * Generates a list of discrete connected surfaces. Marginally faster, but tends to not be worth it as datasets large enough for the speedup to be appreciable
     * are too large to store the adjacency matrix in memory.
     * @param size vertex threshold value below which a surface will not be added to the list
     */
    public void generateSurfacesAdjacencyMatrix(int size){
        int count = 1;
        for(Node n : vertices){
            if(!visited[n.getIndex()]){
                ArrayList<Node> connected = getConnectedComponents(n.getIndex());
                Surface s = new Surface(getFacesFromNodes(connected, faces), count);
                if(s.size() > size){
                    surfaces.add(s);
                    count++;
                }
            }
        }

        for(Surface s : surfaces){
            s.getSurface().sort(Triangle::compareTo);
        }
    }

    /**
     * Generates a list of discrete connected surfaces
     * @param size vertex threshold value below which a surface will not be added to the list
     */
    public void generateSurfaces(int size){
        int count = 1;
        ArrayList<Node> removedNodes = new ArrayList<>();
        ArrayList<Triangle> removedTriangles = new ArrayList<>();
        for(Node n : vertices){
            //System.out.println(n.n);
            if(!visited[n.getIndex()]){
                ArrayList<Node> connected = getConnectedComponentsAdjacencyList(n.getIndex());
                Surface s = new Surface(getFacesFromNodes(connected, faces), count);
                if(s.size() > size){
                    surfaces.add(s);
                    count++;
                }
                else{
                    removedNodes.addAll(connected);
                    for(Triangle t : s.getSurface()){
                        faces.remove(t);
                    }
                }
            }
        }
        for(Node n : removedNodes){
            vertices.remove(n);
        }

        for(Surface s : surfaces){
            s.getSurface().sort(Triangle::compareTo);
        }
    }

    /**
     * Export processed surface data to a new .obj file.
     * @throws IOException Throws IOException
     */
    public void exportOBJ(String filename) throws IOException {
        DataExport d = new DataExport();
        d.exportOBJ(filename, vertices, surfaces);
    }

    public void generateQuadtree(int size){
        quadtree = new Quadtree((1 - x / 2.0) * width, (x - x / 2.0) * width, (1 - y / 2.0) * height, (y - y / 2.0) * height, surfaces, size);
    }

    /**
     * Generates a 3D integer array of interior points by counting the number of intersections a line cast from each point in the sample space in the Z-direction
     * has with the faces of the surfaces. An odd number of intersection means a point is in the interior of a surface. Even means exterior.
     * @return The integer of a point refers to the index of the surface it belongs to. 0 means a point is in the exterior.
     */
    public int[][][] determineInteriorPoints(){
        interiorPoints = new int[x][y][z];
        for(int i = 1; i <= z; i++){
            for(int j = 1; j <= x; j++){
                for(int k = 1; k <= y; k++){
                    Line l = new Line((j - x / 2.0) * width, (k - y / 2.0) * height, (i - z / 2.0) * depth, (5 + z / 2.0) * depth);
                    ArrayList<Triangle> triangleSublist = quadtree.getTrianglesSublist((j - x / 2.0) * width, (k - y / 2.0) * height);
                    int intersections = 0;
                    int[] indices = new int[triangleSublist.size()];
                    int intersectedIndex;
                    for(Triangle t : triangleSublist){
                        if ((j - x / 2.0) * width > t.getMinX() && (j - x / 2.0) * width < t.getMaxX()) {
                            if ((k - y / 2.0) * height > t.getMinY() && (k - y / 2.0) * height < t.getMaxY()) {
                                if (l.intersection(t)) {
                                    indices[intersections] = t.surfaceIndex;
                                    intersections++;
                                }
                            }
                        }
                    }
                    if(intersections % 2 != 0){
                        intersectedIndex = getOddOccurringIndex(indices, intersections);
                        interiorPoints[j - 1][k - 1][i - 1] = intersectedIndex;
                        surfaces.get(intersectedIndex - 1).addVoxel();
                    }
                }
            }
            //System.out.println(i);
        }
        return interiorPoints;
    }

    public void computeSurfaceMeasurements(){
        ArrayList<Surface> holes = new ArrayList<>();
        for(Surface s : surfaces){
            s.calculateSurfaceArea(width, height, depth);
            s.calculateVolume();
            if(s.getVolume() < 0.0){
                holes.add(s);
                int index = checkHoles(s);
                ArrayList<Triangle> hole;
                hole = s.getSurface();
                surfaces.get(index).addHole(hole);
            }
        }
        for(Surface s : holes){
            surfaces.remove(s);
        }
        int i = 1;
        for(Surface s : surfaces){
            s.calculateSurfaceArea(width, height, depth);
            s.calculateVolume();
            s.changeIndex(i);
            i++;
            totalVolume += s.getVolume();
            totalSurfaceArea += s.getSurfaceArea();

        }
        sampleSpaceSize = x * width * y * height * z * depth;
        percentOfSampleSpace = totalVolume / sampleSpaceSize;
    }

    /**
     * Imports the grayscale value of the original image into a 3D array
     * FOr use of quantification with hydrogen peroxide data.
     * @param biofilmImage File path to bacteria image
     * @param h2o2Image File path to H202 image
     */
    public void importImages(File biofilmImage, File h2o2Image){
        ImageVoxels iv = ImageVoxels.load(biofilmImage);
        ImageVoxels h2o2IV = ImageVoxels.load(h2o2Image);
        cellDensity = iv.getData();
        h2o2Density = h2o2IV.getData();
    }

    /**
     * Imports the grayscale value of the original image into a 3D array
     * @param biofilmImage File path to bacteria image
     */
    public void importImage(File biofilmImage){
        ImageVoxels iv = ImageVoxels.load(biofilmImage);
        cellDensity = iv.getData();
        System.out.println(iv.getDepth());
    }

    /**
     * Computes density of interior and exterior of biofilm
     */
    public void computeDensity(){
        numInteriorVoxels = 0;
        numExteriorVoxels = 0;
        for(int i = 0; i < x; i++){
            for(int j = 0; j < y; j++){
                for(int k = 0; k < z; k++){
                    if(interiorPoints[i][j][k] > 0){
                        interiorDensity += cellDensity[k][i][j];
                        numInteriorVoxels++;
                    }
                    else{
                        exteriorDensity += cellDensity[k][i][j];
                        numExteriorVoxels++;
                    }
                }
            }
        }

        interiorDensity /= numInteriorVoxels;
        exteriorDensity /= numExteriorVoxels;
    }

    public void computeSurfaceDensities(){
        numInteriorVoxels = 0;
        numExteriorVoxels = 0;
        for(int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                for (int k = 0; k < z; k++) {
                    if(interiorPoints[i][j][k] > 0){
                        int index = interiorPoints[i][j][k];
                        surfaces.get(index - 1).addMass(cellDensity[k][i][j]);
                        interiorDensity += cellDensity[k][i][j];
                        numInteriorVoxels++;
                    }
                    else{
                        exteriorDensity += cellDensity[k][i][j];
                        numExteriorVoxels++;
                    }
                }
            }
        }

        interiorDensity /= numInteriorVoxels;
        exteriorDensity /= numExteriorVoxels;
    }

    /**
     * Computes H202 density of interior and exterior of biofilm. Finds a weighted density ratio with cell density.
     */
    public void computeH202Density() {
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                for (int k = 0; k < z; k++) {
                    if (interiorPoints[i][j][k] > 0) {
                        interiorCellDensity += h2o2Density[i][j][k];
                    } else {
                        exteriorCellDensity += h2o2Density[i][j][k];
                    }
                }
            }
        }
        interiorCellDensity /= numInteriorVoxels;
        exteriorCellDensity /= numExteriorVoxels;
        wightedInteriorH202 = interiorDensity / interiorCellDensity;
        weightedExteriorH202 = exteriorDensity / exteriorCellDensity;
    }

    /**
     * Prints data averaged over all rigid structures
     */
    public void printBiofilmData(){
        DataExport d = new DataExport();
        d.printBiofilmData(interiorDensity, exteriorDensity, totalSurfaceArea, totalVolume, sampleSpaceSize, percentOfSampleSpace);
    }

    /**
     * Exports to a file data averaged over all rigid structures
     */
    public void exportBiofilmData(String filename) throws IOException {
        DataExport d = new DataExport();
        d.exportBiofilmData(filename, interiorDensity, exteriorDensity, totalSurfaceArea, totalVolume, sampleSpaceSize, percentOfSampleSpace);
    }

    public void printSurfacesData(){
        DataExport d = new DataExport();
        d.printSurfacesData(surfaces);
    }

    public void exportSurfacesData(String filename) throws IOException{
        DataExport d = new DataExport();
        d.exportSurfacesData(filename, surfaces);
    }

    public void exportSurfacesDataVertically(String s, String filename) throws IOException{
        DataExport d = new DataExport();
        d.exportSurfaceDataVertically(s, filename, surfaces);
    }

    public void exportTimePointsData(String filename, String structure, int timepoint) throws IOException {
        DataExport d = new DataExport();
        d.exportTimePointsData(filename, structure, timepoint, surfaces);
    }

    public void exportVolumeData(String filename, String structure) throws IOException {
        DataExport d = new DataExport();
        d.exportVolumeData(filename, structure, surfaces);
    }

    /**
     * Exports interior map to a grayscale image file
     * @throws IOException throws IOException
     */
    public void exportInteriorMap() throws IOException {
        DataExport d = new DataExport();
        d.exportInteriorMap(interiorPoints, x, y, z);
    }

    /**
     * @param vertex index of vertex to start search from
     * @return an ArrayList of connected nodes
     */
    private ArrayList<Node> getConnectedComponents(int vertex){
        Queue<Integer> q = new LinkedList<>();
        q.add(vertex);
        visited[vertex] = true;
        boolean[] con = new boolean[visited.length];
        con[vertex] = true;

        while(!q.isEmpty()){
            Integer v = q.remove();

            for(int i = 0; i < visited.length; i++){
                if(adjacencyMatrix[v][i] && !visited[i]){
                    visited[i] = true;
                    con[i] = true;
                    q.add(i);
                }
            }
        }

        ArrayList<Node> connected = new ArrayList<>();
        for(int i = 0; i < con.length; i++){
            if(con[i]){
                connected.add(vertices.get(i));
            }
        }

        return connected;
    }

    private ArrayList<Node> getConnectedComponentsAdjacencyList(int vertex){
        Queue<Integer> q = new LinkedList<>();
        q.add(vertex);
        visited[vertex] = true;
        count++;
        //System.out.println(count);
        boolean[] con = new boolean[visited.length];
        con[vertex] = true;
        while(!q.isEmpty()){
            Integer v = q.remove();

            for(int i = 0; i < visited.length; i++){
                if(adjacencyList.get(v).contains(i) && !visited[i]){
                    visited[i] = true;
                    count++;
                    //System.out.println(count);
                    con[i] = true;
                    q.add(i);
                }
            }
        }

        ArrayList<Node> connected = new ArrayList<>();
        for(int i = 0; i < con.length; i++){
            if(con[i]){
                connected.add(vertices.get(i));
            }
        }

        return connected;
    }

    /**
     * @param connected ArrayList of connected nodes
     * @param faces ArrayList of all faces
     * @return ArrayList of connected faces in a surface
     */
    private ArrayList<Triangle> getFacesFromNodes(ArrayList<Node> connected, ArrayList<Triangle> faces){
        ArrayList<Triangle> connectedFaces = new ArrayList<>();

        for(Triangle t : faces){
            Node n = t.getVertices().get(0);
            for(Node m : connected){
                if(n.equals(m)){
                    connectedFaces.add(t);
                }
            }
        }

        return connectedFaces;
    }

    /**
     * @param surfaces ArrayList of surfaces
     * @return ArrayList of faces in a surface
     */
    private ArrayList<Triangle> getTrianglesFromSurfaces(ArrayList<Surface> surfaces){
        HashSet<Triangle> faces = new HashSet<>();
        for(Surface s : surfaces){
            faces.addAll(s.getSurface());
        }
        ArrayList<Triangle> triangles = new ArrayList<>(faces);
        triangles.sort(Triangle::compareTo);

        return triangles;
    }

    /**
     *
     * @param indices Array of triangle indices.
     * @param n Number of points to check
     * @return The index that occurs an odd number of times.
     */
    private int getOddOccurringIndex(int[] indices, int n){
        for (int i = 0; i < n; i++) {
            int count = 0;
            for (int j = 0; j < n; j++) {
                if (indices[i] == indices[j])
                    count++;
            }
            if (count % 2 != 0)
                return indices[i];
        }
        return -1;
    }

    /**
     *
     * @param s Hole s, a surface with positive surface area but non-positive calculated volume.
     * @return Index of the surface that the hole s belongs to
     */
    private int checkHoles(Surface s){
        Node v = s.getSurface().get(0).getVertices().get(0);
        Line l = new Line(v.x, v.y, s.getMaxZ() + 1, (5 + z / 2.0) * depth);
        ArrayList<Triangle> triangleSublist;
        triangleSublist = quadtree.getTrianglesSublist(v.x, v.y);
        int[] intersections = new int[triangleSublist.size()];
        int numIntersections = 0;
        for (Triangle t : triangleSublist) {
            if (l.intersection(t)) {
                intersections[numIntersections] = t.surfaceIndex - 1;
                numIntersections++;
            }
        }
        int lastOddOccurringIndex = 0;
        for(int i = 0; i < numIntersections; i++){
            int count = 0;
            for(int j = 0; j < numIntersections; j++){
                if(intersections[i] == intersections[j]){
                    count++;
                }
            }
            if(count % 2 != 0){
                lastOddOccurringIndex = intersections[i];
            }
        }
        return lastOddOccurringIndex;
    }

    /**
     * Divides sample space recursively into spaces with an equivalent number of Triangle objects based on a variable size-threshold
     * Note: Memory issues may occur with smaller size-thresholds
     */
    private class Quadtree {
        double minX, maxX, minY, maxY;
        Quadtree c1, c2, c3, c4;
        private ArrayList<Surface> surfacesSublist = new ArrayList<>();
        private ArrayList<Triangle> trianglesSublist = new ArrayList<>();
        int sizeThreshold;

        public Quadtree(double minX, double maxX, double minY, double maxY, ArrayList<Surface> surfaces, int size){
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;

            reduceSurfaces(surfaces);
            reduceTriangles();

            sizeThreshold = size;

            subdivide();
        }

        public ArrayList<Surface> getSurfacesSublist(double x, double y){
            if(x >= (minX + maxX) / 2){
                if(y >= (minY + maxY / 2)){
                    if(trianglesSublist.size() > sizeThreshold){
                        return c4.getSurfacesSublist(x, y);
                    }
                    else{
                        return surfacesSublist;
                    }
                }
                else{
                    if(trianglesSublist.size() > sizeThreshold){
                        return c2.getSurfacesSublist(x, y);
                    }
                    else{
                        return surfacesSublist;
                    }
                }
            }
            else{
                if(y >= (minY + maxY / 2)){
                    if(trianglesSublist.size() > sizeThreshold){
                        return c3.getSurfacesSublist(x, y);
                    }
                    else{
                        return surfacesSublist;
                    }
                }
                else{
                    if(trianglesSublist.size() > sizeThreshold){
                        return c1.getSurfacesSublist(x, y);
                    }
                    else{
                        return surfacesSublist;
                    }
                }
            }
        }

        public ArrayList<Triangle> getTrianglesSublist(double x, double y){
            if(x >= (minX + maxX) / 2.0){
                if(y >= (minY + maxY) / 2.0){
                    if(trianglesSublist.size() > sizeThreshold){
                        return c4.getTrianglesSublist(x, y);
                    }
                    else{
                        return trianglesSublist;
                    }
                }
                else{
                    if(trianglesSublist.size() > sizeThreshold){
                        return c2.getTrianglesSublist(x, y);
                    }
                    else{
                        return trianglesSublist;
                    }
                }
            }
            else{
                if(y >= (minY + maxY) / 2.0){
                    if(trianglesSublist.size() > sizeThreshold){
                        return c3.getTrianglesSublist(x, y);
                    }
                    else{
                        return trianglesSublist;
                    }
                }
                else{
                    if(trianglesSublist.size() > sizeThreshold){
                        return c1.getTrianglesSublist(x, y);
                    }
                    else{
                        return trianglesSublist;
                    }
                }
            }
        }


        private void subdivide(){
            if(trianglesSublist.size() > sizeThreshold){
                c1 = new Quadtree(minX, (minX + maxX) / 2.0, minY, (minY + maxY) / 2.0, surfacesSublist, sizeThreshold);
                c2 = new Quadtree((minX + maxX) / 2.0, maxX, minY, (minY + maxY) / 2.0, surfacesSublist, sizeThreshold);
                c3 = new Quadtree(minX, (minX + maxX) / 2.0, (minY + maxY) / 2.0, maxY, surfacesSublist, sizeThreshold);
                c4 = new Quadtree((minX + maxX) / 2.0, maxX,(minY + maxY) / 2.0, maxY, surfacesSublist, sizeThreshold);
            }
        }

        private void reduceSurfaces(ArrayList<Surface> surfaces){
            for(Surface s : surfaces){
                if(s.getMinX() <= maxX && s.getMaxX() >= minX){
                    if(s.getMinY() <= maxY && s.getMaxY() >= minY){
                        surfacesSublist.add(s);
                    }
                }
            }
        }

        private void reduceTriangles(){
            for(Surface s : surfacesSublist){
                for(Triangle t : s.getSurface()){
                    if(t.getMinX() <= maxX && t.getMaxX() >= minX) {
                        if (t.getMinY() <= maxY && t.getMaxY() >= minY) {
                            trianglesSublist.add(t);
                            t.surfaceIndex = s.getIndex();
                        }
                    }
                }
            }
        }


    }

}
