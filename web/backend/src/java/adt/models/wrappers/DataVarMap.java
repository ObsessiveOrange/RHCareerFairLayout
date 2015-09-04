package adt.models.wrappers;

import java.util.HashMap;

import adt.models.DataVar;

public class DataVarMap extends HashMap<String, String> {

    /**
     * 
     */
    private static final long serialVersionUID = -6604330445386875778L;

    public void put(DataVar dataVar) {
	put(dataVar.getItem(), dataVar.getValue());
    }
}
