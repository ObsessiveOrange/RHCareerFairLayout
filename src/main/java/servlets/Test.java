package servlets;

import java.util.ArrayList;
import java.util.Collections;

import adt.Term;

import com.google.gson.Gson;

public class Test {
    
    public static void main(String args[]) {
    
        ArrayList<Term> terms = new ArrayList<Term>();
        terms.add(new Term("2013", "Spring"));
        terms.add(new Term("2013", "Fall"));
        terms.add(new Term("2013", "Winter"));
        terms.add(new Term("2014", "Winter"));
        terms.add(new Term("2014", "Fall"));
        terms.add(new Term("2014", "Spring"));
        terms.add(new Term("2015", "Fall"));
        terms.add(new Term("2015", "Spring"));
        terms.add(new Term("2015", "Winter"));
        terms.add(new Term("2016", "Spring"));
        terms.add(new Term("2016", "Winter"));
        terms.add(new Term("2016", "Fall"));
        
        Collections.sort(terms);
        
        System.out.println(new Gson().toJson(terms));
    }
}
