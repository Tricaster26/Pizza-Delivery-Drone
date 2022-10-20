package uk.ac.ed.inf;

import java.util.List;
import java.util.Objects;

public class Order {

    /** This method is used to calculate the delivery cost, in pence, of a given list of pizzas which can all be found
     * in one restaurant from a given list of restaurants.
     * */
    public static int getDeliveryCost(List<Restaurant> restaurantList, String... args) throws Exception {
        int currentCost = 100;
        int costChecker = 0;
        //names of pizzas are all lower case as to accept arguments that are not case-sensitive
        String firstPizza = args[0].toLowerCase();
        Restaurant withPizza = null;

        // Cannot order more than 4 pizzas
        if(args.length > 4){
            throw new Exception("Cannot order more than 4 pizzas");
        }
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


        // if first pizza in argument is not found in any menu, then throw exception.
        if(withPizza == null){
            throw new Exception("Ordered Pizza is not listed in available restaurants");
        }

        if(args.length > 1 ){
            for(int i = 1; i <args.length ;i++){
                for(int j = 0; j < withPizza.menu.size();j++){
                    if (Objects.equals(args[i].toLowerCase(), withPizza.menu.get(j).name.toLowerCase())){
                        currentCost =currentCost + withPizza.menu.get(j).priceInPence;
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
