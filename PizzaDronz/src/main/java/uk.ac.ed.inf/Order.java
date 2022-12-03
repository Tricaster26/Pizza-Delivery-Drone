package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Order {
    @JsonProperty( "orderNo" )
    public String orderNo;
    @JsonProperty( "orderDate" )
    public String orderDate;
    @JsonProperty( "customer" )
    public String customer;
    @JsonProperty( "creditCardNumber" )
    public String creditCardNumber;
    @JsonProperty( "creditCardExpiry" )
    public String creditCardExpiry;
    @JsonProperty("cvv")
    public String cvv;
    @JsonProperty("priceTotalInPence")
    public int priceTotalInPence;
    @JsonProperty("orderItems")
    public List<String> orderItems;



    /** This method and, its helper, is used to calculate the delivery cost, in pence, of pizzas in the orderItems which
     * can all be found in one restaurant from a given list of restaurants.
     * */
    public int getDeliveryCost(Restaurant[] restaurantList) throws Exception {
        int currentCost = 100;
        //names of pizzas are all lower case as to accept arguments that are not case-sensitive
        String firstPizza = orderItems.get(0).toLowerCase();
        Restaurant withPizza = null;
        try {
            for (Restaurant currRestaurant : restaurantList) {
                for (int j = 0; j < currRestaurant.menu.size(); j++) {
                    MenuItem currItem = currRestaurant.menu.get(j);
                    //If the first string matches the name of a pizza from a restaurant we store the restaurant name and leave loop.
                    if (Objects.equals(currItem.name.toLowerCase(), firstPizza)) {
                        currentCost = currentCost + currItem.priceInPence;
                        withPizza = currRestaurant;
                        break;
                    }
                }
            }
        } catch(NullPointerException e){
            throw new NullPointerException("Restaurant list is null");
        }
        currentCost = currentCost + pizzaFinder(withPizza);

        return currentCost;
    }
    /** Helper method for getDeliveryCost. If the items in the orderItems list are in the same restaurant then
     * the total price for those pizzas (excluding the first) is returned. */
    private  int pizzaFinder(Restaurant givenRestaurant)  {
        int currentCost = 0;
        if(orderItems.size() > 1 ){
            for(int i = 1; i <orderItems.size() ;i++){
                for(int j = 0; j < givenRestaurant.menu.size();j++){
                    if (Objects.equals(orderItems.get(i).toLowerCase(), givenRestaurant.menu.get(j).name.toLowerCase())){
                        currentCost =currentCost + givenRestaurant.menu.get(j).priceInPence;
                    }
                }
            }
        }
        return currentCost;
    }
    /** This method is used to find the pizzeria that contains the pizzas in the order with a valid list of order
     * items. Takes in a list of Restaurant and returns the correct restaurant.  */
    public Restaurant restaurantFinder(Restaurant[] restaurantList) {
        Restaurant pizzeriaWithItem = null;
        for (Restaurant restaurant : restaurantList) {
            for (int i = 0; i < restaurant.menu.size(); i++)
                // Since pizzas are not shared between restaurants, then if first pizza matches,
                // rest of them should be here as well
                if (Objects.equals(restaurant.menu.get(i).name, orderItems.get(0))) {
                    pizzeriaWithItem = restaurant;
                    break;
                }
        }
        if(pizzeriaWithItem == null){
            throw new NullPointerException();
        }
        return  pizzeriaWithItem;
    }

    /**
     This method is used to retrieve the orders made in a given day. The information is obtained from the REST service
     whose base server address is given as an input.
     */

    public static List<Order> ordersForDay(Date date, URL serverBaseAddress) throws Exception {
        List<Order> listOfOrders;
        try {
            if (serverBaseAddress == null) {
                //Default address
                 serverBaseAddress = new URL("https://ilp-rest.azurewebsites.net/");
            }
            else if (!serverBaseAddress.toString().endsWith("/")) {
                 serverBaseAddress = new URL(serverBaseAddress + "/");
            }
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
            URL serverAddress = new URL(serverBaseAddress.toString() + "orders/" + formattedDate);

            listOfOrders = new ObjectMapper().readValue(serverAddress, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new Exception("INVALID BASE URL");
        }

        return listOfOrders;
    }

    /** Used to validate details of an order with a series of checks.Takes in the list of available restaurants and
     * returns an OrderOutcome enum stating if it valid or invalid, with more detailed enums for being invalid.
     */

    public OrderOutcome orderOutcome(Restaurant[] restaurantList) throws Exception {
        if(!validCardNumber()){
            return OrderOutcome.INVALID_CARD_NUMBER;
        }
        else if(!validExpiry()){
            return  OrderOutcome.INVALID_EXPIRY_DATE;
        }
        else if(cvv.length() != 3){
            return  OrderOutcome.INVALID_CVV;
        }
        else if(orderItems.size() > 4){
            return  OrderOutcome.INVALID_PIZZA_COUNT;
        }
        else if(!validPizzas(restaurantList)){
            return OrderOutcome.INVALID_PIZZA_NOT_DEFINED;
        }
        else if(!singleSupplier(restaurantList)){
            return OrderOutcome.INVALID_PIZZA_COMBINATION_MULTIPLE_SUPPLIERS;
        }
        else if(getDeliveryCost(restaurantList) != priceTotalInPence){
            return OrderOutcome.INVALID_TOTAL;
        }
        return OrderOutcome.VALID_BUT_NOT_DELIVERED;
    }
    /** This method checks if the pizzas are all from the same supplier. Takes in the list of Restaurant and returns a boolean
    * value stating if the pizzas are from a single restaurant.*/
    private boolean singleSupplier(Restaurant[] restaurantList){
        Restaurant firstPizza = restaurantFinder(restaurantList);
        boolean foundPizza = false;
        for(String orderItems:orderItems){
            for (int i = 0 ; i<firstPizza.menu.size() ; i++){
                //make both lower case to accept capsLock errors
                if (Objects.equals(orderItems.toLowerCase(), firstPizza.menu.get(i).name.toLowerCase())) {
                    foundPizza = true;
                    break;
                }
            }
            if (!foundPizza){
                return false;
            }
            foundPizza = false;
        }
        return  true;
    }
    /** This method is used to check if all the order items are listed in any of the available restaurants. Takes in
    * a parameter of type Restaurant[] and returns a boolean value which is false if an order item is not found in any restaurants
    * menu*/
    private boolean validPizzas(Restaurant[] restaurantList){
        boolean foundPizza = false;
        for (String orderItem : orderItems) {
            //checks if order item is listed in menus of any available restaurant.
            searchLoop:
            for (Restaurant restaurant : restaurantList) {
                for (int k = 0; k < restaurant.menu.size(); k++) {
                    //make both lower case to accept capsLock errors
                    if (Objects.equals(restaurant.menu.get(k).name.toLowerCase(), orderItem.toLowerCase())) {
                        foundPizza = true;
                        break searchLoop;
                    }
                }
            }
            if (!foundPizza) {
                return false;
            }
            foundPizza = false;
        }
        return true;
    }

    /** Applies validation checks to the card expiry date in the order object. Returns false if not valid and takes in no
     * input parameters */
    private boolean validExpiry(){
        Date dateOrder = null;
        try {
            //Check if order date is in the correct format
            SimpleDateFormat orderFormat = new SimpleDateFormat("yyyy-MM-dd");
            orderFormat.setLenient(false);
            dateOrder = orderFormat.parse(orderDate);
        }
        catch(ParseException e){
            System.err.println("OrderDates in JSON are not in the correct format");
            System.exit(1);
        }
        try {
            //card expiry date must be of this format
            SimpleDateFormat formatter = new SimpleDateFormat("MM/yy");
            formatter.setLenient(false);
            Date cardExpiry = formatter.parse(creditCardExpiry);
            //if card expiry date is before date order then order is invalid
            if (cardExpiry.before(dateOrder)){
                return false;
            }
        }
        // if error in parsing, then cvv is invalid format
        catch (ParseException e){
            return false;
        }
        return true;
    }

    /** Applies validation checks to the card number in the order object. Returns false if not valid and takes in no
     * input parameters */
    private boolean validCardNumber(){
        try{
            long cardNumber = Long.parseLong(creditCardNumber);
            if(String.valueOf(cardNumber).length() != 16){
                return false;
            }
            // mast 1 and mast 2 make the IIN ranges of a subset of master card
            boolean mast1 = Long.parseLong(creditCardNumber.substring(0,4)) >= 2221;
            boolean mast2 = Long.parseLong(creditCardNumber.substring(0,4)) <= 2720;
            // mast 3 and mast 4 make the rest of master card.
            boolean mast3 = Long.parseLong(creditCardNumber.substring(0,2)) >= 51;
            boolean mast4 = Long.parseLong(creditCardNumber.substring(0,2)) <= 55;
            //visa IIN range
            boolean visa = Long.parseLong(creditCardNumber.substring(0,1)) == 4;
            if(!(mast1 && mast2) && !(mast3 && mast4 )&& !visa){
                return false;
            }
            //Luhn algorithm to calculate check digit
            String noCheckDigit = creditCardNumber.substring(0,15);
            long checkDigit = cardNumber % 10;

            long  sum = 0;
            boolean isOdd= true; //used to check the odd loop
            for(int i=(noCheckDigit).length(); i>0 ; i--){
                int accountDigit = Integer.parseInt(noCheckDigit.substring(i - 1, i));
                if(isOdd){
                    accountDigit *=2;
                }
                // non-unit integers have their units summed
                sum += accountDigit % 10;
                sum += accountDigit/10;
                isOdd = !isOdd;
            }
            if( (10 - (sum % 10)) %10 != checkDigit){
                return false;
            }
        }
        catch (NumberFormatException e){
            return false;
        }
        return true;
    }

}
