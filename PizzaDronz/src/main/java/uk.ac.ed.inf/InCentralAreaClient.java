package uk.ac.ed.inf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException ;
import java.net.URL;
import java.util.List;


public class InCentralAreaClient {
    public List<AreaCoordinates> centralAreaCoordinates;
    private static InCentralAreaClient instance = null;
    /** This method is used to retrieve the central area locations from the rest service provided by the user.
     *
     * @param givenBase the base address of the REST-server website
     * @param  givenEcho givenEcho for website
     */
     public void collectCentralArea(URL givenBase, String givenEcho){
        try {
            URL baseUrl = new URL ("https://ilp-rest.azurewebsites.net/");
            String echoBasis = "";
            if (givenBase != null && givenEcho != null){
                 baseUrl = givenBase ;
                 echoBasis = givenEcho ;
            }
            if ( !baseUrl.toString().endsWith ( "/" ) ) {
                baseUrl = new URL(baseUrl + "/") ;
            }
            // we call the centralArea endpoint
            URL url = new URL( baseUrl + "centralArea/ " + echoBasis ) ;
/**
        the Jackson JSON library provides helper methods which can directly
        take a URL, perform the GET request convert the result to the specified class
 */
            centralAreaCoordinates = new ObjectMapper().readValue( url , new TypeReference<>() {
            });
/**
 *some error checking − only needed for the sample ( if the JSON data is not correct usually an exception is thrown \)
 */
            if ( !centralAreaCoordinates.get(0).name.endsWith ( echoBasis ) ) {
                throw new RuntimeException ( "wrong echo returned " ) ;
            }
        } catch (IOException e ) {
            System.err.println("Invalid URL");
            System.exit(1);
        }

    }
     //private constructor which only stays in this class
     private InCentralAreaClient(){
         this.centralAreaCoordinates = null;
    }
    /**
     method getInstance() is used to create instance of the singleton class InCentralAreaClient and then return that
     instance when called in other classes.
     */
    public static InCentralAreaClient getInstance() {
        if(instance == null){
            instance = new InCentralAreaClient();
            instance.collectCentralArea(null,null);
        }
        return instance;
    }

}
