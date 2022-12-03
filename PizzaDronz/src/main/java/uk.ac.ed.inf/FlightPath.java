package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FlightPath {
   public List<LngLat> path;
   public List<Long> nanoTime;
   public List<Double> angle;

   public FlightPath(List<LngLat> path, List<Long> ticks, List<Double> angle){
       this.nanoTime = ticks;
       this.path = path;
       this.angle = angle;
   }
    /** Helper function used to find if 2 line segments meet. Takes coordinate A, B and C then returns a boolean value
     * to be used in the function bestDirections.
     */
    private static boolean threeCoordinates(LngLat A, LngLat B, LngLat C){
        return (C.lat() - A.lat()) * (B.lng() -A.lng()) > (B.lat()-A.lat()) * (C.lng()-A.lng());
    }
    /** Helper function used to find the moves that do not cross the no-fly zones. It takes in the current position
     * and a  list of coordinates and returns a list of CompassDirection enums which don't cause the current position
     * to move into (using nextPosition) the boundaries formed by a pair of coordinates.
     */
    private static List<CompassDirection> bestDirections(LngLat source,double[][] coordinates){
        List<CompassDirection> validCompassDirections = CompassDirection.allDirections();
        for (int i=0; i<coordinates.length - 1; i++){
            List<CompassDirection> testCompassDirections =  new ArrayList<>();
            //Both points make a boundary line of a no-fly zone
            LngLat boundary1 = new LngLat(coordinates[i][0] , coordinates[i][1]);
            LngLat boundary2 = new LngLat(coordinates[i+1][0], coordinates[i+1][1]);
            for (int j = 0; j <CompassDirection.allDirections().size(); j++){
                //dest is the position after a given move.
                LngLat dest = source.nextPosition(CompassDirection.allDirections().get(j));
                //intersect1 and intersect2 are used to calculate if lines (boundary1,boundary2) and (dest,source) intersect
                boolean intersect1 = !(threeCoordinates(source,boundary1,boundary2) ==  threeCoordinates(dest,boundary1,boundary2));
                boolean intersect2 = !(threeCoordinates(source,dest,boundary1) == threeCoordinates(source,dest,boundary2));
                // if the lines segments do not intersect.
                if(!(intersect2 && intersect1)){
                    testCompassDirections.add(CompassDirection.allDirections().get(j));
                }
            }
            validCompassDirections.retainAll(testCompassDirections);
        }
        return validCompassDirections;
    }


    /** This is our flightpath method. It is used to calculate the flightpath from a given source to a  given
     * destination. The inputs are both LngLat objects. source is the starting position and destination is the
     * destination position.
     */
    public static FlightPath halfPath(LngLat source , LngLat destination, NoFlyZone[] noFlyZones )  {
        List<LngLat> queue = new ArrayList<>(); //used to check the current position of the drone.
        queue.add(source);
        List<LngLat> flightPathList = new ArrayList<>();
        flightPathList.add(source);
        List<Long> ticks = new ArrayList<>(); // holds computation ticks per move
        List<Double> angles = new ArrayList<>(); // holds angles per move;

        //will change to make sure its contains valid directions for a given position.
        List<CompassDirection> validCompassValues = CompassDirection.allDirections();
        // Used to check if route is allowed to leave central area.
        boolean inAreaSource = source.inCentralArea();
        boolean inAreaDest = destination.inCentralArea();
        while(!queue.isEmpty()){
            for (NoFlyZone noFlyZone : noFlyZones) {
                validCompassValues.retainAll(bestDirections(queue.get(0), noFlyZone.coordinates));
            }
                CompassDirection bestDirection = queue.get(0).optimalDirection(validCompassValues,destination);
                LngLat newPosition =  queue.get(0).nextPosition(bestDirection);
                // if the position has not been visited and the position is closer to destination.
                queue.add(newPosition);

                flightPathList.add(newPosition);
                ticks.add(System.nanoTime());
                angles.add(bestDirection.angle());
                if (newPosition.closeTo(destination)) {
                    break;
                }
            queue.remove(0);
            validCompassValues = CompassDirection.allDirections();
            // If both source and destination are in central area, then we cannot not leave central area.
            if (inAreaSource && inAreaDest){
                validCompassValues.removeIf(validCompassValue -> !newPosition.nextPosition(validCompassValue).inCentralArea());
            }
            //if the source is not in central area and the new position is,
            // then the remaining positions cannot be outside the central area
            if (!inAreaSource){
                if (newPosition.inCentralArea()){
                    //Removes compass values from validCompassValues, that lead to a position that is outside the central area
                    validCompassValues.removeIf(validCompassValue -> !newPosition.nextPosition(validCompassValue).inCentralArea());
                }
            }
            //if position has been visited, then any future move that leads to it , is invalid
            validCompassValues.removeIf(validCompassValue -> flightPathList.contains(newPosition.nextPosition(validCompassValue)));
            }
        //hover at destination
        flightPathList.add(flightPathList.get(flightPathList.size()-1).nextPosition(null));
        ticks.add(System.nanoTime());
        angles.add(null);

        return (new FlightPath(flightPathList,ticks,angles));
    }
/** helper method used to calculate the opposite angles in the angles of the object, in degrees. It Also removes
 * the null values in the beginning and adds one at the end .Takes in a list of type Double and adds the opposite
 * directions of the FlightPath object angles, to that list.*/
    private void oppositeAngles(List<Double> angleList){
        List<Double> copy =  new ArrayList<>(angle);
        Collections.reverse(copy);
        copy.remove(0); //reverse
        for (Double aDouble : copy) {
            if (aDouble < 180) {
                double oppositeAngle = aDouble + 180;
                angleList.add(oppositeAngle);
            } else if (aDouble >= 180) {
                double oppositeAngle = aDouble - 180;
                angleList.add(oppositeAngle);
            }
        }
        //add null to represent a hover move at the end of path.
        angleList.add(null);
    }

/** This method is used to obtain the flight path to be used by the drone. It examines the path given by halfPath,
 * to and from a destination and uses the shorter path as the flight path. It takes in 2 LngLat objects and a list
 * of type NoFlyZone. Returns an object of type FlightPath.*/
    public static FlightPath completeFlightPath(LngLat source , LngLat destination, NoFlyZone[] noFlyZones){
        List<LngLat> compPathList = new ArrayList<>();
        List<Long> ticks = new ArrayList<>();
        List<Double> angles = new ArrayList<>();

        FlightPath fpA = halfPath(source,destination,noFlyZones);
        //use return path's(fpB) start position, as the last position of the arrival path(fpA)
        LngLat approxDest = fpA.path.get(fpA.path.size()-1);
        FlightPath fpB = halfPath(approxDest,source,noFlyZones);
        //if the return path is shorter than the arrival path
        if(fpA.path.size() > fpB.path.size()){
            //used to calculate ticks for reverse path to satisfy ticksSinceStartOfCalculation constraint.
            FlightPath fpB2 = halfPath(approxDest,source,noFlyZones);

            List<LngLat> fpBReverse = new ArrayList<>(fpB.path);
            Collections.reverse(fpBReverse);
            //last move of fpB.path is a hover move. So remove first move in reverse path.
            fpBReverse.remove(0);
            //Add hover move at the end.
            fpBReverse.add(fpB.path.get(0));
            compPathList =fpBReverse;
            //remove the first LngLat of the returnPath as its position is already repeated twice (via hover)
            fpB.path.remove(0);
            compPathList.addAll(fpB.path);
            //Find opposite angles and add them initially to the list. use fpB2 to maintain fpB
            fpB2.oppositeAngles(angles);
            angles.addAll(fpB.angle);

            ticks.addAll(fpB.nanoTime);
            ticks.addAll(fpB2.nanoTime);
        }
        else{
            FlightPath fpA2 = halfPath(source,destination,noFlyZones);

            List<LngLat> fpAReverse = new ArrayList<>(fpA.path);
            Collections.reverse(fpAReverse);

            fpAReverse.remove(0);
            fpAReverse.remove(0);

            fpAReverse.add(fpA.path.get(0));
            compPathList =fpA.path;

            compPathList.addAll(fpAReverse);
            angles.addAll(fpA.angle);

            fpA2.oppositeAngles(angles);

            ticks.addAll(fpA.nanoTime);
            ticks.addAll(fpA2.nanoTime);
        }
        return new FlightPath(compPathList,ticks,angles);
    }
}
