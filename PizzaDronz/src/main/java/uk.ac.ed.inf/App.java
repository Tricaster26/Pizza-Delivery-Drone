package uk.ac.ed.inf;


import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;


public class App 
{
    public static void main( String[] args )  {
        String orderDate = args[0];
        Date orderDateFormatted = new Date();
        try {
            //entered String must be a date of format yyyy-MM-dd
            SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd");
            formatter.setLenient(false);
            orderDateFormatted = formatter.parse(orderDate);
        }
        catch (ParseException e){
            System.err.println("Invalid Date format. Correct Format (yyyy-MM-dd)");
            System.exit(1);
        }

        URL baseAddress = null;
        try {
            baseAddress = new URL(args[1]);
        }
        catch (IOException e){
            System.err.println("Malformed URL");
            System.exit(1);
        }
        String randomSeed = args[2];

        //getting required data from rest service using the inputs
        List<Order> orders = Order.ordersForDay(orderDateFormatted, baseAddress);
        Restaurant[] restaurantList = Restaurant.getRestaurantsFromRestServer(baseAddress);
        NoFlyZone[] noFlyZones = NoFlyZone.getNoFlyZones(baseAddress);
        InCentralAreaClient.getInstance().collectCentralArea(baseAddress, "");

        //develop Json information according to spec
        ConstructJson jsonInfo = new ConstructJson();
        jsonInfo.developJson(orders,restaurantList,noFlyZones);
        //produce the required Json files.
        jsonInfo.outputFiles(orderDate);
    }

}
