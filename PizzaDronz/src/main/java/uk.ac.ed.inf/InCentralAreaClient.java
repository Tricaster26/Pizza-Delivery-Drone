package uk.ac.ed.inf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException ;
import java.net.URL;
import java.util.List;


public class InCentralAreaClient {
    public List<AreaResponse> responses;
    private static InCentralAreaClient instance = null;
    /**
     This method is used to retrieve the data from the rest service provided by the user. Currently replaced by test
     data. The two input parameters are parts of the URL for the companies' website.
     */
     public void getResponse(String givenBase, String givenEcho){
        String baseUrl = "https://ilp-rest.azurewebsites.net/";
        String echoBasis = "";
        responses = null;
        try {
            if (givenBase != null && givenEcho != null){
                 baseUrl = givenBase ;
                 echoBasis = givenEcho ;
            }
            if ( !baseUrl.endsWith ( "/" ) ) {
                baseUrl += "/" ;
            }
            // we call the centralArea endpoint
            URL url = new URL( baseUrl + "centralArea/ " + echoBasis ) ;
/**
        the Jackson JSON library provides helper methods which can directly
        take a URL, perform the GET request convert the result to the specified class
 */
            responses = new ObjectMapper().readValue( url , new TypeReference<>() {
            });
/**
 *some error checking − only needed for the sample ( if the JSON data is not correct usually an exception is thrown \)
 */
            if ( !responses.get(0).name.endsWith ( echoBasis ) ) {
                throw new RuntimeException ( "wrong echo returned " ) ;
            }
        } catch (IOException e ) {
            e.printStackTrace ( ) ;
        }

    }
     //private constructor which only stays in this class
    private InCentralAreaClient(){

    }
    /**
     method getInstance() is used to create instance of the singleton class InCentralAreaClient and then return that
     instance when called in other classes.
     */
    public static InCentralAreaClient getInstance() {
        if(instance == null){
            instance = new InCentralAreaClient();
            instance.getResponse(null,null);
        }
        return instance;
    }

}
