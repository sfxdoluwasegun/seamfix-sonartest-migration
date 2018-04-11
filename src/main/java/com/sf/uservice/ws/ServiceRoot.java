package com.sf.uservice.ws;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Base path for UCL REST services
 * 
 * @author KOMOO
 */
@ApplicationPath(value = "/uservice")
public class ServiceRoot extends Application {

	@Override
	public Set<Class<?>> getClasses() {
	
		final Set<Class<?>> serviceClasses = new HashSet<Class<?>>();
		serviceClasses.add(Sync.class);
		
		return serviceClasses;
	}
}
