package adt.models;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Category extends Entry implements Comparable<Category> {
    protected String type;
    protected String name;

    public Category(ResultSet rs) throws SQLException {

	this(rs.getLong("id"), rs.getString("name"), rs.getString("type"));
    }

    public Category(Long id, String name, String type) {

	super(id);

	this.name = name;
	this.type = type;

    }

    public String getName() {

	return name;
    }

    public void setName(String name) {

	this.name = name;
    }

    public String getType() {

	return type;
    }

    public void setType(String type) {

	this.type = type;
    }

    @Override
    public int compareTo(Category other) {

	return name.compareTo(other.getName());
    }
}
