package uk.ac.ed.inf;
import org.junit.jupiter.api.*;


import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

public class LngLatTest {
  List<AreaCoordinates> testArea = InCentralAreaClient.getInstance().responses ;

    void testCoordinates1(){
        testArea.get(0).longitude = 1 ;
        testArea.get(0).latitude = 3;
        testArea.get(1).longitude = 2;
        testArea.get(1).latitude = 2;
        testArea.get(2).longitude = 1.5;
        testArea.get(2).latitude = 0;
        testArea.get(3).longitude = 1;
        testArea.get(3).latitude = 1;
        InCentralAreaClient.getInstance().responses = testArea;
    }
    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println(testInfo.getDisplayName());
    }
    @AfterEach
    void testEnd() {
        System.out.println("---Test End---");
    }
    @Test
    @DisplayName("In CentralArea test")
    void isWithinBoundary(){
        testCoordinates1();
        LngLat testCoordinate = new LngLat(1.25,2);
        assertTrue(testCoordinate.inCentralArea(),"Coordinate is within boundary");
        testCoordinate = new LngLat(2,2);
        assertTrue(testCoordinate.inCentralArea(),"coordinate is on vertex");
        testCoordinate = new LngLat(1.75,1);
        assertTrue(testCoordinate.inCentralArea(),"coordinate is on slanted line boundary");
        testCoordinate = new LngLat(1,2);
        assertTrue(testCoordinate.inCentralArea(), "Coordinate is on vertical line boundary");
    }
    @Test
    @DisplayName("Out CentralArea test")
    void isNotWithinBoundary(){
        testCoordinates1();
        LngLat testCoordinate = new LngLat(0,2);
        assertFalse(testCoordinate.inCentralArea(),"Coordinate is on the left, outside the boundary");
        testCoordinate = new LngLat(2,1);
        assertFalse(testCoordinate.inCentralArea(), "Coordinate is on the right, outside the boundary ");
        testCoordinate = new LngLat(1.3,0);
        assertFalse(testCoordinate.inCentralArea(), "Coordinate is outside boundary, next to the lowest vertex");
        testCoordinate = new LngLat(0,3);
        assertFalse(testCoordinate.inCentralArea(), "Coordinate is outside boundary, next to the top vertex");
    }
    @Test
    @DisplayName("closeTo method test")
    void isCloseTo(){
        LngLat testCoordinate = new LngLat(0,1);
        LngLat testCoordinate2 = new LngLat(0,0);
        assertFalse(testCoordinate.closeTo(testCoordinate2), "coordinates are not 0.00015 degrees off each other");
        testCoordinate2 = new LngLat(0,1.00015);
        assertTrue(testCoordinate.closeTo(testCoordinate2), "coordinates are exactly 0.00015 degrees off each other");
        testCoordinate2 = new LngLat(0,1.00014);
        assertTrue(testCoordinate.closeTo(testCoordinate2), "coordinates are less than 0.00015 degrees off each other");
    }
    @Test
    @DisplayName("nextPosition method test")
    void nextPosition(){
        LngLat testCoordinate = new LngLat(0,0);
        LngLat testCoordinate2 = testCoordinate.nextPosition(CompassDirection.SSW);
        assertEquals(0.00015, testCoordinate2.distanceTo(testCoordinate), "Distance between one move is 0.00015 degrees");
    }


}
