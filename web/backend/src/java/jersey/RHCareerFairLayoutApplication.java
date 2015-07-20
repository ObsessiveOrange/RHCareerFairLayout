package jersey;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import servlets.Data;
import servlets.Users;

@ApplicationPath("/api") // set the path to REST web services
public class RHCareerFairLayoutApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
	final Set<Class<?>> classes = new HashSet<Class<?>>();
	// register resources and features
	classes.add(MultiPartFeature.class);
	classes.add(JacksonFeature.class);
	classes.add(Data.class);
	classes.add(Users.class);
	return classes;
    }

    // public RHCareerFairLayoutApplication() {
    // packages("servlets");
    // }
}