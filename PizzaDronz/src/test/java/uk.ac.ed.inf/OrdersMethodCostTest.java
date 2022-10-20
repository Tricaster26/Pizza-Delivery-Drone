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
        assertEquals(price , 1400+1100+100, "Price must match values on website plus delivery charge");
    }
}
