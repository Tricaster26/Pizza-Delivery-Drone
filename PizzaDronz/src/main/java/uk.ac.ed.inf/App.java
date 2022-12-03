package uk.ac.ed.inf;

import com.mapbox.geojson.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;


public class App 
{
    @SuppressWarnings("unchecked")
    public static void main( String[] args ) throws Exception {
        Scanner sc= new Scanner(System.in);
        System.out.print("Enter REST-server base URL: ");
        URL baseAddress= new URL (sc.nextLine());
        System.out.print("Enter Order Date: ");
        String orderDate = sc.nextLine();
        Date orderDateFormatted;
        try {
            //entered String must be a date of format yyyy-MM-dd
            SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd");
            formatter.setLenient(false);
            orderDateFormatted = formatter.parse(orderDate);
        }
        catch (ParseException e){
            throw new Exception("Invalid Date format. Correct Format (yyyy-MM-dd)");

        }
        System.out.print("Enter number: ");
        sc.nextInt();

        //getting required data from rest service using the inputs
        List<Order> orders = Order.ordersForDay(orderDateFormatted, baseAddress);
        Restaurant[] restaurantList = Restaurant.getRestaurantsFromRestServer(baseAddress);
        NoFlyZone[] noFlyZones = NoFlyZone.getNoFlyZones(baseAddress);
        LngLat appletonTower = new LngLat(-3.186874,55.944494);

        //Json arrays for the first 2 Json files
        JSONArray allDeliveries = new JSONArray();
        JSONArray allMoves = new JSONArray();
        //List of Points and all positions to construct GeoJson file
        List<Point> points = new ArrayList<>();
        List<LngLat> completePath = new ArrayList<>(); //used to construct GeoJson file

        //holds maximum number of moves the drone can make a day
        int moves = 2000;

        for (Order tempOrder : orders) {
            OrderOutcome orderOutcome = tempOrder.orderOutcome(restaurantList);
            List<LngLat> flightpath;
            List<Long> ticks;
            List<Double> angles;
            //used to construct deliveries Json file
            JSONObject deliveryRecord = new JSONObject();

            if (orderOutcome == OrderOutcome.VALID_BUT_NOT_DELIVERED){
                Restaurant chosenRes = tempOrder.restaurantFinder(restaurantList);
                LngLat destination = new  LngLat (chosenRes.longitude,chosenRes.latitude);

                //path calculation to destination
                FlightPath fullPath = FlightPath.completeFlightPath(appletonTower,destination,noFlyZones);
                flightpath = fullPath.path;
                ticks = fullPath.nanoTime;
                angles = fullPath.angle;
                //number of angles represents number of moves we can make, in completePath algorithm
                if (angles.size() <= moves){
                    orderOutcome = OrderOutcome.DELIVERED;
                    moves = moves - angles.size();
                    completePath.addAll(flightpath);
                    //If the outcome is delivered then the move is recorded in the flightpath JSON file
                    for(int i = 0 ; i < flightpath.size() - 1;i++){
                        JSONObject pathRecord = new JSONObject();
                        pathRecord.put("orderNo", tempOrder.orderNo);
                        pathRecord.put("fromLongitude", flightpath.get(i).lng());
                        pathRecord.put("fromLatitude",flightpath.get(i).lat() );
                        pathRecord.put("angle", angles.get(i) );
                        pathRecord.put("toLongitude", flightpath.get(i+1).lng());
                        pathRecord.put("toLatitude",flightpath.get(i+1).lat());
                        pathRecord.put("ticksSinceStartOfCalculation", ticks.get(i) );
                        allMoves.add(pathRecord);
                    }
                }
            }
            if(orderOutcome == OrderOutcome.DELIVERED){
                deliveryRecord.put("orderNo", tempOrder.orderNo);
                deliveryRecord.put("outcome", orderOutcome.toString());
                deliveryRecord.put("costInPence", tempOrder.priceTotalInPence);
            }
            else {
                deliveryRecord.put("orderNo", tempOrder.orderNo);
                deliveryRecord.put("outcome", orderOutcome.toString());
                deliveryRecord.put("costInPence", 0);
            }
            allDeliveries.add(deliveryRecord);
        }
        FileWriter file = new FileWriter("deliveries-" + orderDate +".json");
        file.write(allDeliveries.toJSONString());
        file.close();

        FileWriter file2 = new FileWriter("flightpath-"+orderDate+".json");
        file2.write(allMoves.toJSONString());
        file2.close();


        for (LngLat lngLat : completePath) {
            points.add(Point.fromLngLat(lngLat.lng(), lngLat.lat()));
        }
        LineString lineString = LineString.fromLngLats(points);
        Feature feature = Feature.fromGeometry(lineString);
        FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);
        FileWriter file3 = new FileWriter("drone-"+orderDate+".geojson");
        file3.write(featureCollection.toJson());
        file3.close();


    }
}
