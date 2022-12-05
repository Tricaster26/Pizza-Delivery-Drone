package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  This responsibility of the class is focused on the flight path of the drone. It finds the flight path of the drone
 *  and the angles used to make the path. It also tracks the computation ticks per move. An instance of this class
 *  represents a flight path with the ticks need to calculate it and the angles that make the path.
 */
public class FlightPath {
   public List<LngLat> path;
   public List<Long> ticks;
   public List<Double> angles;

   public FlightPath(List<LngLat> path, List<Long> ticks, List<Double> angles){
       this.ticks = ticks;
       this.path = path;
       this.angles = angles;
   }
    /** This method is used to obtain the flight path to be used by the drone. It examines the path given by halfPath,
     * to and from a destination and uses the shorter path as the flight path.
     *
     * @param source LngLat object representing the start position of the drone.
     * @param destination LngLat object representing the end position of the drone.
     * @param noFlyZones List of NoFlyZone objects as listed in the REST-Service
     * @return FlightPath object holding the information for the moves made by the drone.
     */
    public static FlightPath completeFlightPath(LngLat source , LngLat destination, NoFlyZone[] noFlyZones){
        List<LngLat> compPathList;
        List<Long> ticks = new ArrayList<>();
        List<Double> angles = new ArrayList<>();

        FlightPath fpA = halfPath(source,destination,noFlyZones);
        //use arrival path's(fpA) final position, as the source position of the return path(fpB)
        LngLat approxDest = fpA.path.get(fpA.path.size()-1);
        FlightPath fpB = halfPath(approxDest,source,noFlyZones);
        //we choose the shorter path
        if(fpA.path.size() > fpB.path.size()){
            //used to calculate ticks for reverse path to satisfy ticksSinceStartOfCalculation constraint.
            FlightPath fpB2 = halfPath(approxDest,source,noFlyZones);

            List<LngLat> fpBReverse = new ArrayList<>(fpB.path);
            Collections.reverse(fpBReverse);
            //last move of fpB.path is a hover move. So remove first move in reverse path.
            fpBReverse.remove(0);
            //remove the first LngLat of the returnPath as its position is already repeated twice (via hover)
            fpB.path.remove(0);
            //Add hover move at the end.
            fpBReverse.add(fpB2.path.get(0));

            compPathList =fpBReverse;
            compPathList.addAll(fpB.path);
            //Find opposite angles and add them to the list first as that is the starting path.
            fpB.oppositeAngles(angles);
            angles.addAll(fpB.angles);

            ticks.addAll(fpB.ticks);
            ticks.addAll(fpB2.ticks);
        }
        else{
            FlightPath fpA2 = halfPath(source,destination,noFlyZones);

            List<LngLat> fpAReverse = new ArrayList<>(fpA.path);
            Collections.reverse(fpAReverse);

            fpAReverse.remove(0); //last move of fpA.path is a hover move. So remove it
            fpAReverse.remove(0); //remove the first LngLat of the returnPath
            fpAReverse.add(fpA.path.get(0)); //Add hover move at the end.

            compPathList =fpA.path;
            compPathList.addAll(fpAReverse);

            angles.addAll(fpA.angles);
            fpA.oppositeAngles(angles);

            ticks.addAll(fpA.ticks);
            ticks.addAll(fpA2.ticks);
        }
        return new FlightPath(compPathList,ticks,angles);
    }

    /** This is a helper method for our main flightpath method. It is used to calculate the flightpath from a given source
     * to a  given destination.
     *
     * @param source LngLat object representing the start position of the drone.
     * @param destination LngLat object representing the end position of the drone.
     * @param noFlyZones List of NoFlyZone objects as listed in the REST-Service
     * @return FlightPath object holding the information for the moves made by the drone.
     */
    private static FlightPath halfPath(LngLat source , LngLat destination, NoFlyZone[] noFlyZones )  {
        List<LngLat> queue = new ArrayList<>(); //used to check the current position of the drone.
        queue.add(source);
        List<LngLat> flightPath = new ArrayList<>();
        flightPath.add(source);
        List<Long> ticks = new ArrayList<>(); // holds computation ticks per move
        List<Double> angles = new ArrayList<>(); // holds angles per move;

        //will change to make sure its contains valid directions for a given position.
        List<CompassDirection> validCompassValues = CompassDirection.allDirections();
        // Used to check if route is allowed to leave central area.
        boolean inAreaSource = source.inCentralArea();
        boolean inAreaDest = destination.inCentralArea();
        while(!queue.isEmpty()){
            LngLat currPosition = queue.get(0);
            if (currPosition.closeTo(destination)) {
                break;
            }
            for (NoFlyZone noFlyZone : noFlyZones) {
                List<CompassDirection> validDir = validDirections(currPosition, noFlyZone.coordinates);
                validCompassValues.retainAll(validDir);
            }
            CompassDirection bestDirection = currPosition.optimalDirection(validCompassValues,destination);
            LngLat newPosition =  currPosition.nextPosition(bestDirection);

            queue.add(newPosition);
            flightPath.add(newPosition);
            ticks.add(System.nanoTime());
            angles.add(bestDirection.angle());

            queue.remove(0);
            //we reset the compass values and filter them .
            validCompassValues = CompassDirection.allDirections();
            // If both source and destination are in central area, then we cannot not leave central area.
            if (inAreaSource && inAreaDest){
                validCompassValues.removeIf(validCompassValue -> !newPosition.nextPosition(validCompassValue).inCentralArea());
            }
            //if the source is not in central area and the new position is,
            // then the remaining moves cannot lead outside the central area
            if (!inAreaSource){
                if (newPosition.inCentralArea()){
                    //Removes compass values from validCompassValues, that lead to a position that is outside the central area
                    validCompassValues.removeIf(validCompassValue -> !newPosition.nextPosition(validCompassValue).inCentralArea());
                }
            }
            //if position has been visited, then any future move that leads to it , is invalid
            validCompassValues.removeIf(validCompassValue -> flightPath.contains(newPosition.nextPosition(validCompassValue)));
        }
        //hover at destination
        flightPath.add(flightPath.get(flightPath.size()-1).nextPosition(null));
        ticks.add(System.nanoTime());
        angles.add(null);

        return (new FlightPath(flightPath,ticks,angles));
    }

    /** Helper function used to find if 2 line segments meet. Takes LngLat A, B and C then returns a boolean value
     * to be used in the function bestDirections.
     */
    private static boolean threeCoordinates(LngLat A, LngLat B, LngLat C){
        return (C.lat() - A.lat()) * (B.lng() -A.lng()) > (B.lat()-A.lat()) * (C.lng()-A.lng());
    }
    /** Helper function used to find the moves that do not cross the boundaries of the  no-fly zones.
     *
     * @param source LngLat object represent current position of the drone.
     * @param coordinates  list of lists of double representing coordinates(no-fly zone boundaries)
     * @return list of CompassDirection enums which don't cause the current position to move into (using nextPosition)
     *  the boundaries formed by a pair of coordinates.
     */
    private static List<CompassDirection> validDirections(LngLat source, double[][] coordinates){
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

    /** Helper method used to calculate the opposite angles in the instance angles of the object, in degrees.
     * It Also removes the null values in the beginning and adds one at the end to correctly place the hover move.
     *
     * @param angleList list of type Double whose copy is manipulated.
     */
    private void oppositeAngles(List<Double> angleList){
        List<Double> copy =  new ArrayList<>(angles);
        Collections.reverse(copy);
        copy.remove(0); //remove first angle as we don't start with a hover move
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

    /** This method is used to add this FlightPath object to a sorted list of FlightPath objects increasing
     *  based on their path instance size. The object is placed in a position in the list such that it is
     *  still sorted the same.
     *
     * @param optimisedPath List of FlightPath objects, intended to be sorted based on path size
     * @param optimised List of Order objects, intended for List to be sorted based on optimisedPath.
     * @param tempOrder An order that is paired with the FlightPath object to be added into optimised.
     */
    public void optimisedPaths(List<FlightPath> optimisedPath, List<Order> optimised, Order tempOrder){
        if(optimisedPath.size() == 0){
            optimisedPath.add(this);
            optimised.add(tempOrder);
        }
        else{
            for (int i = 0 ;i<optimisedPath.size();i++){
                if(path.size() < optimisedPath.get(i).path.size()){
                    optimisedPath.add(i,this);
                    optimised.add(i,tempOrder);
                    break;
                }
                else if(i == optimisedPath.size()-1){
                    optimisedPath.add(this);
                    optimised.add(tempOrder);
                    break;
                }
            }
        }
    }

}
