package adt.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataVar extends Entry {

    private String item;
    private String value;

    public DataVar(String item, String value) {
	this.item = item;
	this.value = value;
    }

    /**
     * @return the item
     */
    public String getItem() {
	return item;
    }

    /**
     * @param item
     *            the item to set
     */
    public void setItem(String item) {
	this.item = item;
    }

    /**
     * @return the value
     */
    public String getValue() {
	return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(String value) {
	this.value = value;
    }
}
