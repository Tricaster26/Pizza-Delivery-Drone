package uk.ac.ed.inf;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Node {
    private final LngLat node;
    public Node parent;
    public Node(LngLat node){
        this.node = node;
        this.parent = null;
    }
    public Node getParent(){
        return parent;
    }
    public LngLat getNode(){
        return node;
    }

    /** Helper function used to find if 2 line segments meet. Takes coordinate A, B and C then returns a boolean value
     * to be used in the function bestDirections.
     */
    private static boolean threeCoordinates(LngLat A, LngLat B, LngLat C){
        return (C.lat() - A.lat()) * (B.lng() -A.lng()) > (B.lat()-A.lat()) * (C.lng()-A.lng());
    }

    private static List<CompassDirection> bestDirections(LngLat source,double[][] coordinates){
        List<CompassDirection> validCompassDirections = new ArrayList<>();
        for (int i=0; i<coordinates.length - 1; i++){
            LngLat boundary1 = new LngLat(coordinates[i][0] , coordinates[i][1]);
            LngLat boundary2 = new LngLat(coordinates[i+1][0], coordinates[i+1][1]);
            for (int j = 0; j <CompassDirection.compassValues().size();j++){
                //dest is the position after a given move.
                LngLat dest = source.nextPosition(CompassDirection.compassValues().get(j));
                //intersect1 and intersect2 are used to calculate if lines boundary1boundary2 and dest2source intersect
                boolean intersect1 = threeCoordinates(source,boundary1,boundary2) != threeCoordinates(dest,boundary1,boundary2);
                boolean intersect2 = threeCoordinates(source,dest,boundary1) != threeCoordinates(source,dest,boundary2);
                // if the lines segments do not intersect.
                if(!(intersect2 && intersect1)){
                    validCompassDirections.add(CompassDirection.compassValues().get(j));
                }
            }
        }
        return validCompassDirections;
    }
    /** This method is used to calculate the closest coordinate returned by nextPosition for a  given coordinate. It takes
     * a LngLat value and returns a LngLat value using the nextPosition method.
     */
    public static List<LngLat> bfs(LngLat source , LngLat destination) throws Exception {
        Node firstNode = new Node(source);
        List<Node> queue = new ArrayList<>();
        queue.add(firstNode);
        List<Node> visitedNode = new ArrayList<>();
        visitedNode.add(firstNode);
        List<LngLat> visited = new ArrayList<>();
        visited.add(source);
        List<CompassDirection> validCompassValues = CompassDirection.compassValues();
        NoFlyZone[] noFlyZones = NoFlyZone.getNoFlyZones(new URL("https://ilp-rest.azurewebsites.net/"));
        for (NoFlyZone noFlyZone : noFlyZones) {
            validCompassValues.retainAll(bestDirections(source, noFlyZone.coordinates));
        }
        while(!queue.isEmpty()){
            for(int i = 0 ;i < CompassDirection.compassValues().size();i++){
                LngLat newPosition = queue.get(0).getNode().nextPosition(CompassDirection.compassValues().get(i));
                Node newNode = new Node(newPosition);
                if (!visited.contains(newPosition) && newPosition.distanceTo(destination) < queue.get(0).getNode().distanceTo(destination)){
                    queue.add(newNode);
                    visited.add(newPosition);
                    newNode.parent = queue.get(0);
                    visitedNode.add(newNode);
                }
                if(newPosition.closeTo(destination)){
                    break;
                }
            }
            queue.remove(0);
            if(visitedNode.get(visitedNode.size()-1).getNode().closeTo(destination)){
                break;
            }
        }
        List<LngLat> finalList = new ArrayList<>();
        Node currentNode = visitedNode.get(visitedNode.size()-1);
        finalList.add(currentNode.getNode());
        while(currentNode.parent != null){
            finalList.add(currentNode.parent.getNode());
            currentNode = currentNode.parent;
        }
        return finalList;
    }
}
