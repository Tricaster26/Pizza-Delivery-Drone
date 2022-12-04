package uk.ac.ed.inf;

import java.util.List;

/**
 *  The responsibility of this record is focused on geographic coordinates/positions. It is used to show how these
 *  coordinates exist in the world of the specification and how they can be manipulated.
 *  An instance of this class represents a geographic coordinate on Earth.
 * @param lng  longitude
 * @param lat  latitude
 */
public record LngLat(double lng, double lat){

    /** Method used to check if a geographic coordinate is within the central Area defined by the REST service.
     * It does this by checking if the point is on the boundary or if the  horizontal line to the right of the
     * point touches the boundary an odd number of times.
     *
     * @return boolean true if LngLat object is in or on the boundaries received
     */
    public boolean inCentralArea(){
        boolean isCentral = false;
        int contactCounter = 0; // checks how often the horizontal line intercepts the boundary
        InCentralAreaClient abc = InCentralAreaClient.getInstance();

        //The list of locations with their coordinates obtained from the REST service
        List<AreaCoordinates> centralArea = abc.centralAreaCoordinates;

        for(int i = 0; i < centralArea.size(); i++){
            LngLat coordinate1;
            LngLat coordinate2;

            coordinate1 = new LngLat(centralArea.get(i).longitude,centralArea.get(i).latitude);

        // The last coordinate in the list creates a boundary line with the first coordinate in the list.
            if (i == centralArea.size() - 1){
                 coordinate2 = new LngLat(centralArea.get(0).longitude,centralArea.get(0).latitude);
            }
        //Every other coordinate connects to the subsequent coordinate in the list.
            else {
                 coordinate2 = new LngLat(centralArea.get(i+1).longitude,centralArea.get(i+1).latitude);
            }

        //check if object's latitude is in between,or one of, the latitudes of the two vertices that connect to each other.
            if (lat >= coordinate1.lat && lat <= coordinate2.lat || lat <= coordinate1.lat && lat >= coordinate2.lat){
                double gradient = (coordinate2.lat - coordinate1.lat)/(coordinate2.lng - coordinate1.lng);
                double intercept =  coordinate2.lat - (coordinate2.lng * gradient);

        //case 1, point is on a line whose gradient is infinite.
                if(coordinate1.lng == coordinate2.lng){
                    if(coordinate1.lng == lng){
                        isCentral = true;
                        break;
                    }
                }

        //case 2, point is on a line whose gradient is 0.
                else if(coordinate1.lat == coordinate2.lat){
                    if(lng >= coordinate1.lng && lng <=coordinate2.lng || lng<= coordinate1.lng && lng >= coordinate2.lng){
                        isCentral = true;
                        break;
                    }
                }

                double substituteLat1 = coordinate1.lat;
                double substituteLat2 = coordinate2.lat;

        //if the point lies on the same latitude as one of the vertices on the line, but not the same longitude,
        //we push the line slightly up as to make the horizontal line pass the edges instead of the vertex.
                if (lat == coordinate1.lat && lng != coordinate1.lng|| lat == coordinate2.lat && lng != coordinate2.lng){
                    substituteLat1 = coordinate1.lat + Math.pow(10,-13);
                    substituteLat2 = coordinate2.lat + Math.pow(10,-13);
                    gradient = (substituteLat2 - substituteLat1)/(coordinate2.lng - coordinate1.lng);
                    intercept =  substituteLat2 - (coordinate2.lng * gradient);
                }
        //case 3, point is on boundary line with a non-zero,non-infinite gradient.
                if (gradient*lng + intercept == lat ){
                    isCentral = true;
                    break;
                }

                if(lat >= substituteLat1 && lat <= substituteLat2 || lat <= substituteLat1 && lat >= substituteLat2){

        //case 4, horizontal line to the right of the point intersects with a vertical boundary line.
                    if(coordinate1.lng==coordinate2.lng){
                        if(lng < coordinate1.lng){
                            contactCounter++;
                        }
                    }

        //case 5, horizontal line intersects boundary with a non-zero,non-infinite gradient.
                    else if (((lat - intercept)/gradient) > lng){
                        contactCounter++;
                    }
                }

            }
        }
        if( contactCounter % 2 != 0){
         isCentral = true;
         }
        return isCentral;
    }

    /** This method is used check the Pythagorean  distance between two coordinates.
     *
     * @param position2 a LngLat object
     * @return the distance between the LngLat objects in degrees.
     */
    public double distanceTo(LngLat position2){
        double trueDistance = Math.sqrt(Math.pow(lng - position2.lng, 2) + Math.pow(lat - position2.lat,2));
        return  Math.round(trueDistance * Math.pow(10, 13)) / (Math.pow(10, 13));
    }

    /** This method checks if the distance between two coordinates is within 0.00015 degrees.
     *
     * @param position2 a LngLat object
     * @return a boolean value that is true if distanceTo(position2) is within 0.00015 degrees.
     */
    public boolean closeTo(LngLat position2){
        boolean isClose = false;
        double distance  = distanceTo(position2);
        if (distance < 0.00015){
           isClose = true;
        }
        return isClose;
    }
    /** This method is used to find out the next position of the drone given a direction.
     *
     * @param compassDirection an enum to determine the direction
     * @return a new LngLat object showing the next position of the drone.
     */
    public LngLat nextPosition(CompassDirection compassDirection){
        double newLng = lng;
        double newLat = lat;

        //If compassDirection is null then the coordinates do not change as it will hover
        if (!(compassDirection == null)) {
            newLng = Math.sin(Math.toRadians(compassDirection.angle())) * 0.00015 + lng;
            newLat = Math.cos(Math.toRadians(compassDirection.angle())) * 0.00015 + lat;

            newLng = Math.round(newLng * Math.pow(10, 13)) / (Math.pow(10, 13));
            newLat = Math.round(newLat * Math.pow(10, 13)) / (Math.pow(10, 13));
            }

        return new LngLat(newLng,newLat);

    }

    /** This method finds the closest position, returned by nextPosition, to a given destination.
     *
     * @param directions List of CompassDirection enums to be checked.
     * @param destination LngLat object that is the target position.
     * @return CompassDirection that leads to the position closes to the target position.
     */

    public CompassDirection optimalDirection(List<CompassDirection> directions , LngLat destination){
        double distance = Integer.MAX_VALUE;
        CompassDirection bestPosition = null;
        for (CompassDirection direction : directions) {
            LngLat possiblePosition = nextPosition(direction);
            if (distance > possiblePosition.distanceTo(destination)) {
                distance = possiblePosition.distanceTo(destination);
                bestPosition = direction;
            }
            if ((bestPosition != direction) && edgeCase(destination,distance,possiblePosition)){
                bestPosition = direction;
            }
        }
        return bestPosition;
    }

    /** Helper method to handle specific edge case. Used to handle the decision-making when considering two moves that
     * are both as close to the destination as the other. This specifically useful when the true best position is
     * blocked by a no fly-zone boundary to and from the destination and the two moves are the next best.
     *
     * @param destination LngLat Object, the goal of the path.
     * @param distance double value, the current best distance
     * @param possiblePosition LngLat Object, the current position being considered
     * @return boolean value based on the constraints.
     */
    private boolean edgeCase(LngLat destination, double distance, LngLat possiblePosition){
        if(distance ==  possiblePosition.distanceTo(destination)){
            if(lat > destination.lat){
                return true;
            }
            if(lat == destination.lat){
                return lng > destination.lng;
            }
        }
        return false;
    }

}

