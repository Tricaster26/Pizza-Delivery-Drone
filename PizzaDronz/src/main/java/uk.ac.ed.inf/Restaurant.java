package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class Restaurant {
    @JsonProperty( "name" )
    public String name;
    @JsonProperty ( "longitude" )
    public double longitude;
    @JsonProperty ( "latitude" )
    public double latitude;
    @JsonProperty ( "menu" )
    public List<MenuItem> menu;

    /**This method is used to return the list of available restaurants from  the RestService.
     * @param serverBaseAddress the base address of the REST-server website
     * @return List of available restaurants as listed in the REST-server
     */
    public static Restaurant[] getRestaurantsFromRestServer(URL serverBaseAddress)  {
        Restaurant[] restaurantList = new Restaurant[0];
        try {
            if (serverBaseAddress == null){
                //Default address
                serverBaseAddress = new URL("https://ilp-rest.azurewebsites.net/");
            }
            if ( !serverBaseAddress.toString().endsWith( "/" ) ) {
                serverBaseAddress = new URL(serverBaseAddress + "/") ;
            }
            URL serverAddress = new URL(serverBaseAddress + "restaurants/");

    /**
     the Jackson JSON library provides helper methods which can directly
    take a URL, perform the GET request convert the result to the specified class
    */
            restaurantList = new ObjectMapper().readValue( serverAddress , new TypeReference<>() {
            });
    /**
    *some error checking − only needed for the sample ( if the JSON data is not correct usually an exception is thrown \)
    */

        } catch (IOException e) {
            System.err.println("Base URL entered is invalid");
            System.exit(1);
        }
        return restaurantList;
    }
}
