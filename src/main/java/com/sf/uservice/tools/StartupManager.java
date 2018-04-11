package com.sf.uservice.tools;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import com.sf.uservice.beans.ApplicationBean;

@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@AccessTimeout(unit = TimeUnit.MINUTES, value = 5)
public class StartupManager {

	private Logger log = Logger.getLogger(getClass());

	@Inject
	private ApplicationBean appBean;

	@PostConstruct
	public void initialize() {
		log.info("Starting up Unical Service");
		log.info("Document storage directory " + appBean.getDocumentStorageDirectory());

	}


}
