package adt.models;

import misc.DataTable;

public class Sheet extends DataTable {

    /**
     * 
     */
    private static final long serialVersionUID = 2894564054267085814L;
    private String name;

    public Sheet() {

	// stub; nothing to do here...
    }

    public Sheet(String name) {

	this.setName(name);
    }

    /**
     * @return the name
     */
    public String getName() {

	return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {

	this.name = name;
    }
}
