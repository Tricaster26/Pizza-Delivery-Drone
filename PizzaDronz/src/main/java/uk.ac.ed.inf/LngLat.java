package uk.ac.ed.inf;

import java.util.ArrayList;

public record LngLat(double lng, double lat){
// Constructor added for possible future additions
    public LngLat(double lng, double lat){
        this.lng = lng;
        this.lat = lat;
        }

    /**
     * This method can be called to check if a geographic coordinate is within the central Area defined by the REST
     * service. Returns true if in or on the boundaries received.
     * It does this by checking if the horizontal line to the right of the coordinate touches the boundary an odd number
     * of times.
     */
    public boolean inCentralArea(){
        boolean isCentral = false;
        int contactCounter = 0;
        InCentralAreaClient abc = InCentralAreaClient.getInstance();

        //The array list of locations with their coordinates obtained from the REST service
        ArrayList<AreaResponse> centralArea = abc.responses;

        for(int i = 0; i < centralArea.size(); i++){
            LngLat coordinate1;
            LngLat coordinate2;

            coordinate1 = new LngLat(centralArea.get(i).longitude,centralArea.get(i).latitude);

        // The last coordinate in the array creates a boundary line with the first coordinate in the array.
            if (i == centralArea.size() - 1){
                 coordinate2 = new LngLat(centralArea.get(0).longitude,centralArea.get(0).latitude);
            }

        //Every other coordinate connects to the subsequent coordinate in the array.
            else {
                 coordinate2 = new LngLat(centralArea.get(i+1).longitude,centralArea.get(i+1).latitude);
            }

        //check if record's latitude is in between,or one of, the latitudes of the vertices that connect to each other.
            if (lat >= coordinate1.lat && lat <= coordinate2.lat || lat <= coordinate1.lat && lat >= coordinate2.lat){
                double gradient = (coordinate2.lat - coordinate1.lat)/(coordinate2.lng - coordinate1.lng);
                double intercept =  coordinate2.lat - (coordinate2.lng * gradient);

        //case 1, where gradient of boundary line is infinite.
                if(coordinate1.lng == coordinate2.lng){
                    if(coordinate1.lng == lng){
                        isCentral = true;
                        break;
                    }
                    else if(lng < coordinate1.lng){
                        contactCounter++;
                    }
                }
        //case 2, where gradient of boundary line is 0.
                else if(coordinate1.lat == coordinate2.lat){
                    if(lng >= coordinate1.lng && lng <=coordinate2.lng || lng<= coordinate1.lng && lng >= coordinate2.lng){
                        isCentral = true;
                        break;
                    }
                }
        //case 3, where coordinate is on a boundary line with a non-zero non-infinite gradient.
                else if (gradient*lng + intercept == lat ){
                    isCentral = true;
                    break;
                }
        //case 4, where latitude of the coordinate meets with a boundary line(non-zero, non-infinite gradient) to its right.
                else if (((lat - intercept)/gradient) > lng){
                    contactCounter++;
                }
            }
        }
        if( contactCounter % 2 != 0){
         isCentral = true;
         };
        return isCentral;
    }

    /**
     * This method is used check the distance between two coordinates. It takes a LngLat object and returns the distance
     * between the coordinates parameter's and the LngLat object that called the method.
     */
    public double distanceTo(LngLat position2){
        double trueDistance = Math.sqrt(Math.pow(lng - position2.lng, 2) + Math.pow(lat - position2.lat,2));
        return  Math.round(trueDistance * Math.pow(10, 13)) / (Math.pow(10, 13));
    }
    /**
     * This method checks if the distance between two coordinates is within 0.00015 degrees. Takes in a LngLat object
     * as a parameter and returns a boolean value that returns true if the distance is within 0.00015 degrees.
     */
    public boolean closeTo(LngLat position2){
        boolean isClose = false;
        double distance  = distanceTo(position2);
        if (distance <= 0.00015){
           isClose = true;
        }
        return isClose;
    }
    /** This method is used to find out the next position of the drone given a direction.The input parameter is an angle
     *  to determine the direction. Returns a new LngLat object showing the next position of the drone
     */
    public LngLat nextPosition(CompassDirection compassDirection){
        double newLng = lng;
        double newLat = lat;

        try {
            //If compassDirect is null then the coordinates do not change
            if (!(compassDirection == null)) {
                newLng = Math.sin(compassDirection.angle()) * 0.00015 + lng;
                newLat = Math.cos(compassDirection.angle()) * 0.00015 + lat;
                //round to the 13th decimal place
                newLng = Math.round(newLng * Math.pow(10, 13)) / (Math.pow(10, 13));
                newLat = Math.round(newLat * Math.pow(10, 13)) / (Math.pow(10, 13));
            }
        }
        catch(Exception e){
            System.out.println("Invalid compass direction given");
        }

        return new LngLat(newLng,newLat);

    }

}

