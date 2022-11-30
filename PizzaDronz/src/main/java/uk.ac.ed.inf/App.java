package uk.ac.ed.inf;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception {
        LngLat obj1 = new LngLat(-3.1900599977594,55.9429495391453);
        LngLat obj2 = new LngLat(-3.1899887323379517,55.94284650540911);;
        System.out.println( Node.bfs(obj1,obj2));
  }
}
