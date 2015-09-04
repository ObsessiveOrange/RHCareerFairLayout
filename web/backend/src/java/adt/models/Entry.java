package adt.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Entry {
    protected Long id;

    /*************************************************
     * Instance methods
     ***********************************************/
    public Entry() {
	this.id = null;
    }

    public Entry(Long id) {

	this.id = id;
    }

    public Long getId() {

	return id;
    }

    public void getId(Long id) {

	this.id = id;
    }

    @Override
    public String toString() {
	try {
	    return new ObjectMapper().writeValueAsString(this);
	} catch (JsonProcessingException e) {
	    // TODO: ServletLog.logEvent(e);

	    return null;
	}
    }
}
