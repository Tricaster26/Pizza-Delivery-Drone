package uk.ac.ed.inf;

import org.junit.jupiter.api.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrdersMethodCostTest {
    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println(testInfo.getDisplayName());
    }
    @AfterEach
    void testEnd() {
        System.out.println("---Test End---");
    }
    @Test
    @DisplayName("Valid delivery inputs")
    void validDeliveryInputTests() throws Exception {
        List<Restaurant> restaurantList = Restaurant.getRestaurantsFromRestServer(null);
        int price = Order.getDeliveryCost(restaurantList, "Vegan Delight" , "Meat Lover");
        assertEquals(price , 1100+1400+100, "Price must match values on website plus delivery charge");
        price = Order.getDeliveryCost(restaurantList, "Vegan Delight" , "Vegan Delight");
        assertEquals(1100+1100+100, price, "One type of pizza should be able to be ordered multiple times");
    }
}
