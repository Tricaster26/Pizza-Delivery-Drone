package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;

public class NoFlyZone {
    @JsonProperty( "name" )
    public String name;
    @JsonProperty ( "coordinates" )
    public double[][] coordinates;

    /** This method is used to return the list of no-fly zones given by the RestService.
     *
     * @param serverBaseAddress the base address of the REST-server website
     * @return List of  NoFlyZone objects as listed in the REST-server
     */
    public static NoFlyZone[] getNoFlyZones(URL serverBaseAddress) {
        NoFlyZone[] noFlyZones = null;
        try {
            if (serverBaseAddress == null){
                //Default address
                serverBaseAddress = new URL("https://ilp-rest.azurewebsites.net/");
            }
            if ( !serverBaseAddress.toString().endsWith( "/" ) ) {
                serverBaseAddress = new URL(serverBaseAddress + "/") ;
            }
            URL serverAddress = new URL(serverBaseAddress + "noFlyZones/");
/**
 the Jackson JSON library provides helper methods which can directly
 take a URL, perform the GET request convert the result to the specified class
 */
            noFlyZones = new ObjectMapper().readValue( serverAddress , new TypeReference<>() {
            });
/**
 *some error checking − only needed for the sample ( if the JSON data is not correct usually an exception is thrown \)
 */

        } catch (IOException e) {
            System.err.println("Base URL entered is invalid");
            System.exit(1);
        }
        return noFlyZones;
    }

}
