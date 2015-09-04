package cf.obsessiveorange.rhcareerfairlayout.data.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.fasterxml.jackson.annotation.JsonProperty;

import cf.obsessiveorange.rhcareerfairlayout.data.managers.DBManager;

public class Company extends Entry implements Comparable<Company> {

    protected String name;
    protected String description;
    protected String websiteLink;
    protected String address;

    /**
     * Constructor for reconstructing from SQL queries
     *
     * @param id          The ID of the company
     * @param name        The name of the company
     * @param description A description of the company (Can be null)
     * @param tableNumber The table the company will be at.
     */

    public Company(Cursor c) {

        super(c.getLong(c.getColumnIndexOrThrow(DBManager.KEY_ID)));

        this.name = c.getString(c.getColumnIndexOrThrow(DBManager.KEY_NAME));
        this.description = c.getString(c.getColumnIndexOrThrow(DBManager.KEY_DESCRIPTION));
        this.websiteLink = c.getString(c.getColumnIndexOrThrow(DBManager.KEY_WEBSITE_LINK));
        this.address = c.getString(c.getColumnIndexOrThrow(DBManager.KEY_ADDRESS));
    }

    public Company(@JsonProperty("id") Long id,
                   @JsonProperty("name") String name,
                   @JsonProperty("description") String description,
                   @JsonProperty("websiteLink") String websiteLink,
                   @JsonProperty("address") String address) {

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

    public ContentValues toContentValues() {
        ContentValues row = new ContentValues();

        row.put(DBManager.KEY_ID, this.getId());
        row.put(DBManager.KEY_NAME, this.getName());
        row.put(DBManager.KEY_DESCRIPTION, this.getDescription());
        row.put(DBManager.KEY_WEBSITE_LINK, this.getWebsiteLink());
        row.put(DBManager.KEY_ADDRESS, this.getAddress());

        return row;
    }

}
