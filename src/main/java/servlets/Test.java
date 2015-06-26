package servlets;

import java.io.IOException;

import misc.RequestBody;

public class Test {
    
    public static void main(String args[]) throws IOException {
    
        RequestBody rb =
                new RequestBody("{'k1':'apple','k2':'orange', 'k3': ['test1', 'test2', 'test3'],"
                        + "'k4': {'a1': 1, 'a2': 'a2str', 'a3': ['a', 'b', 'c'], 'a4': {'testing1': 'testing1', 'testing2': 'testing2'}}}");
        
        System.out.println(rb.getMap("k4").result.getMap("a4").result.getString("testing1", 0, 3));
    }
}
