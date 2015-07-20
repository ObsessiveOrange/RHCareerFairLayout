package adt.models;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Company extends Entry implements Comparable<Company> {

    protected String name;
    protected String description;
    protected String websiteLink;
    protected String address;

    /**
     * Constructor for reconstructing from SQL queries
     * 
     * @param id
     *            The ID of the company
     * @param name
     *            The name of the company
     * @param description
     *            A description of the company (Can be null)
     * @param tableNumber
     *            The table the company will be at.
     */

    public Company(ResultSet rs) throws SQLException {

	this(rs.getLong("id"), rs.getString("name"), rs.getString("description"), rs.getString("websiteLink"),
		rs.getString("address"));
    }

    public Company(Long id, String name, String description, String websiteLink, String address) {

	super(id);
	this.name = name;
	this.description = description;
	this.websiteLink = websiteLink;
	this.address = address;
    }

    public String getName() {

	return name;
    }

    public void setName(String name) {

	this.name = name;
    }

    public String getDescription() {

	return description;
    }

    public void setDescription(String description) {

	this.description = description;
    }

    public String getWebsiteLink() {

	return websiteLink;
    }

    public void setWebsiteLink(String websiteLink) {

	this.websiteLink = websiteLink;
    }

    public String getAddress() {

	return address;
    }

    public void setAddress(String address) {

	this.address = address;
    }

    @Override
    public int compareTo(Company other) {

	return name.compareTo(other.getName());
    }

}
