package uk.ac.ed.inf;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws InvalidPizzaCombinationException, IOException {
       System.out.println(Order.getDeliveryCost(Restaurant.getRestaurantsFromRestServer(null),null));
    }
}
