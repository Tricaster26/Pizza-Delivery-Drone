package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Order {
    @JsonProperty( "orderNo" )
    public int orderNo;
    @JsonProperty( "orderDate" )
    public Date orderDate;
    @JsonProperty( "customer" )
    public String customer;
    @JsonProperty( "name" )
    public String name;
    @JsonProperty( "creditCardNumber" )
    public int creditCardNumber;
    @JsonProperty( "creditCardExpiry" )
    public Date creditCardExpiry;
    @JsonProperty("cvv")
    public int cvv;
    @JsonProperty("priceTotalInPence")
    public int priceTotalInPence;
    @JsonProperty("orderItems")
    public List<Menu> orderItems;



    /** This method and, its helper, is used to calculate the delivery cost, in pence, of a given list of pizzas which
     * can all be found in one restaurant from a given list of restaurants.
     * */
    public static int getDeliveryCost(Restaurant[] restaurantList, String... args) throws Exception {
        int currentCost = 100;
        //names of pizzas are all lower case as to accept arguments that are not case-sensitive
        String firstPizza = args[0].toLowerCase();
        Restaurant withPizza = null;

        // Cannot order more than 4 pizzas
        if(args.length > 4){
            throw new Exception("Cannot order more than 4 pizzas");
        }
        try {
            for (Restaurant currRestaurant : restaurantList) {
                for (int j = 0; j < currRestaurant.menu.size(); j++) {
                    Menu currItem = currRestaurant.menu.get(j);
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

        // if first pizza in argument is not found in any menu, then throw exception.
        if(withPizza == null){
            throw new Exception(args[0] + " is not valid or listed in available restaurants");
        }
        currentCost = currentCost + pizzaFinder(withPizza, args);

        return currentCost;
    }
    /** Helper method for getDeliveryCost. If the pizzas in the list are in the same restaurant then
     * the total price for those pizzas (excluding the first) is returned. */
    private static int pizzaFinder(Restaurant givenRestaurant, String...args) throws InvalidPizzaCombinationException {
        int costChecker = 0;
        int currentCost = 0;
        if(args.length > 1 ){
            for(int i = 1; i <args.length ;i++){
                for(int j = 0; j < givenRestaurant.menu.size();j++){
                    if (Objects.equals(args[i].toLowerCase(), givenRestaurant.menu.get(j).name.toLowerCase())){
                        currentCost =currentCost + givenRestaurant.menu.get(j).priceInPence;
                        costChecker++;
                    }
                }
                //if for loop is exited without finding a matching name in the same menu. Throw exception.
                if(costChecker != i){
                    throw new InvalidPizzaCombinationException("Invalid Pizza Combination");
                }
            }
        }
        return currentCost;
    }
}
