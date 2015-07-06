package adt;

public class Category extends Entry implements Comparable<Category> {
    protected String type;
    protected String name;

    public Category(Long id, String name, String type) {

	super(id);

	this.type = type;
	this.name = name;

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
