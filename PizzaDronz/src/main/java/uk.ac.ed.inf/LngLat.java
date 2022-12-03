package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.List;

public record LngLat(double lng, double lat){
    public LngLat(double lng, double lat){
        this.lng = lng;
        this.lat = lat;
        }

    /**
     * Method used to check if a geographic coordinate is within the central Area defined by the REST service. Returns
     * true if in or on the boundaries received.
     * It does this by checking if the point is on the boundary or if the  horizontal line to the right of the
     * point touches the boundary an odd number of times.
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

    /**
     * This method is used check the Pythagorean  distance between two coordinates. It takes a LngLat object and returns the distance
     * between the coordinates parameter's and the LngLat object that called the method.
     */
    public double distanceTo(LngLat position2){
        double trueDistance = Math.sqrt(Math.pow(lng - position2.lng, 2) + Math.pow(lat - position2.lat,2));
        return  Math.round(trueDistance * Math.pow(10, 13)) / (Math.pow(10, 13));
    }
    /**
     * This method checks if the distance between two coordinates is within 0.00015 degrees. Takes in a LngLat object
     * as a parameter and returns a boolean value that is true if the distance is within 0.00015 degrees.
     */
    public boolean closeTo(LngLat position2){
        boolean isClose = false;
        double distance  = distanceTo(position2);
        if (distance < 0.00015){
           isClose = true;
        }
        return isClose;
    }
    /** This method is used to find out the next position of the drone given a direction.The input parameter is an enum
     *  to determine the direction. Returns a new LngLat object showing the next position of the drone
     */
    public LngLat nextPosition(CompassDirection compassDirection){
        double newLng = lng;
        double newLat = lat;

        //If compassDirection is null then the coordinates do not change as it will hover
        if (!(compassDirection == null)) {
            newLng = Math.sin(Math.toRadians(compassDirection.angle())) * 0.00015 + lng;
            newLat = Math.cos(Math.toRadians(compassDirection.angle())) * 0.00015 + lat;
            //round to the 13th decimal place
            newLng = Math.round(newLng * Math.pow(10, 13)) / (Math.pow(10, 13));
            newLat = Math.round(newLat * Math.pow(10, 13)) / (Math.pow(10, 13));
            }

        return new LngLat(newLng,newLat);

    }

    /**This method finds the closest position, returned by nextPosition, to a given destination. It takes the parameter
     * compass directions to only take valid compass directions if need be, and takes LngLat object which is the target
     * position.
     */

    public CompassDirection optimalDirection(List<CompassDirection> directions , LngLat destination){
        double distance = Integer.MAX_VALUE;
        CompassDirection bestPosition = null;
        for (CompassDirection direction : directions) {
            LngLat currPosition = nextPosition(direction);
            if (distance > currPosition.distanceTo(destination)) {
                distance = currPosition.distanceTo(destination);
                bestPosition = direction;
            }
        }
        return bestPosition;
    }


    }

