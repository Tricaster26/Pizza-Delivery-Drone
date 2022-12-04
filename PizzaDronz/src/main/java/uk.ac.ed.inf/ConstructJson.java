package uk.ac.ed.inf;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *  The responsibility of this class is constructing the Json and GeoJson files asked for by the spec. An instance of
 *  this class represents the information used to form the Json files for deliveries and flightpath. Also, the information
 *  for the GeoJson file, drone path.
 */
public class ConstructJson {
    public JSONArray allDeliveries;
    public JSONArray allMoves;
    public FeatureCollection featureCollection;
    public ConstructJson(){
        this.allDeliveries = new JSONArray();
        this.allMoves  = new JSONArray();
        this.featureCollection = null;
    }

    /** This method is used to update the instance variables with the desired information asked by the specification.
     *
     * @param orders List of Order objects. A list of orders for a given day obtained from the REST-Service.
     * @param restaurantList List of Restaurant objects. A list of restaurants obtained from the REST-Service.
     * @param noFlyZones List of NoFlyZone objects as listed in the REST-Service.
     */
   public void developJson(List<Order> orders, Restaurant[] restaurantList, NoFlyZone[] noFlyZones) {
       LngLat appletonTower = new LngLat(-3.186874,55.944494);
       int moves = 2000;
       List<LngLat> completePath = new ArrayList<>();
       for (Order tempOrder : orders) {
           OrderOutcome orderOutcome = tempOrder.orderOutcome(restaurantList);

           if (orderOutcome == OrderOutcome.ValidButNotDelivered){
               Restaurant chosenRes = tempOrder.restaurantFinder(restaurantList);
               LngLat destination = new  LngLat (chosenRes.longitude,chosenRes.latitude);

               //path calculation to destination
               FlightPath flight = FlightPath.completeFlightPath(appletonTower,destination,noFlyZones);
               List<Double> angles = flight.angles;
               //number of angles represents number of moves we can make, in completePath algorithm
               if (angles.size() <= moves){
                   orderOutcome = OrderOutcome.Delivered;
                   moves = moves - angles.size();
                   completePath.addAll(flight.path);
                   //If the outcome is delivered then the move is recorded in the flightpath JSON file
                   developAllMoves(flight,tempOrder);
               }
           }
           developDeliveries(orderOutcome,tempOrder);
       }
       developCollection(completePath);
   }

    /** This is a helper method used to update the allMoves instance variable
     *  according to the information provided in the spec to create the flightpath json file.
     *
     * @param flight  FlightPath object holding the information for the path, ticks and angles.
     * @param tempOrder An Order object. Usually from a list of Orders.
     */
   @SuppressWarnings("unchecked")
   private void developAllMoves(FlightPath flight,Order tempOrder){
       List<LngLat> flightpath = flight.path;
       List<Long> ticks = flight.ticks;
       List<Double> angles = flight.angles;
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

    /** This is a helper method used to update the allDeliveries instance variable according to
     * the information provided in the spec to create the deliveries json file.
     *
     * @param orderOutcome  OrderOutcome enum. Tells us if an order is valid.
     * @param tempOrder  An Order object. Usually from a list of Orders.
     */
    @SuppressWarnings("unchecked")
   private void developDeliveries(OrderOutcome orderOutcome, Order tempOrder){
        //used to construct deliveries Json file
       JSONObject deliveryRecord = new JSONObject();
       if(orderOutcome == OrderOutcome.Delivered){
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

    /** This is a helper method used to update the featureCollection instance variable according to the
     * information provided in the spec to create the GeoJson file.
     *
     * @param completePath List of LngLat representing the complete path for a given day of orders.
     */
   private void developCollection(List<LngLat> completePath){
       List<Point> points = new ArrayList<>();
       for (LngLat lngLat : completePath) {
           points.add(Point.fromLngLat(lngLat.lng(), lngLat.lat()));
       }
       LineString lineString = LineString.fromLngLats(points);
       Feature feature = Feature.fromGeometry(lineString);
       featureCollection = FeatureCollection.fromFeature(feature);
   }

    /** produces the deliveries, flightpath and drone outputfiles as mentioned in the spec.
     *
     * @param orderDate String which is used for the naming of the file
     */

    public void outputFiles(String orderDate){
       try {
           FileWriter file = new FileWriter("deliveries-" + orderDate + ".json");
           file.write(allDeliveries.toJSONString());
           file.close();

           FileWriter file2 = new FileWriter("flightpath-" + orderDate + ".json");
           file2.write(allMoves.toJSONString());
           file2.close();

           FileWriter file3 = new FileWriter("drone-" + orderDate + ".geojson");
           file3.write(featureCollection.toJson());
           file3.close();
       }
       catch (IOException e){
           System.err.println("Cannot create Json Files. ConstructJson object is invalid");
           System.exit(1);
       }
   }
}
