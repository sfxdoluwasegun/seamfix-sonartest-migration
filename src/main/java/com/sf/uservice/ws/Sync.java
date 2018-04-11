package com.sf.uservice.ws;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.jboss.logging.Logger;

import com.sf.unical.entity.main.alumni.Alumni;
import com.sf.unical.entity.main.docs.Docs;
import com.sf.uservice.beans.DocsPojo;
import com.sf.uservice.beans.RespPojo;
import com.sf.uservice.tools.MessageQueuer;
import com.sf.uservice.tools.QueryManager;

/**
 * Handles synchronization services
 * 
 * @author KOMOO
 * 
 */
@Path(value = "/sync")
public class Sync {

	private final Logger log = Logger.getLogger(Sync.class);
	
	@Inject
	private QueryManager queryManager;
	
	@Inject
	private MessageQueuer messageQueuer;
	
	/**
	 * Document synchronization and re-synchronization
	 * 
	 * @param documentObj
	 * @return 
	 */
	@POST
	@Path(value = "/docs")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public RespPojo synchScannedDocument(DocsPojo documentObj){
		
		try {
			Alumni alumni = queryManager.getAlumniByMatricNoAndEagerDocument(documentObj.getMatricNo());
			if(alumni != null && alumni.getDocs() != null){
				log.info("Queuing scanned document resynchronization");
				
				messageQueuer.queueResyncDocument(documentObj);
				
				return initClientResponse("RS", "Resync successful");
			}
			else {	
				messageQueuer.queueSyncDocument(documentObj);
				
				return initClientResponse("SS", "Sync successful");
			}
			
		} catch (Exception e) {
			return initClientResponse(e.getMessage(), e.getCause().getMessage());
		} 
	}
	
	/**
	 * Confirmation of synchronizated or re-synchronized Documents
	 * 
	 * @param RespPojo
	 * @return 
	 */
	@POST
	@Path(value = "/docs/conf")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public RespPojo confirmScannedDocument(DocsPojo documentObj){

		try {
			Alumni alumni = queryManager.getAlumniByMatricNoAndEagerDocument(documentObj.getMatricNo());

			if (alumni == null)
				 return initClientResponse("SF", "FAILED");
			
			Docs docs = alumni.getDocs();
			if (docs != null) 
				return initClientResponse("SC", "SUCCESS");
			
			 return initClientResponse("SF", "FAILED");
			
		} catch (Exception e) {
			return initClientResponse(e.getMessage(), e.getCause().getMessage());
		}
	}
	
	/**
	 * 
	 * @return
	 */
	@GET
	@Path(value = "/ping")
	@Produces(MediaType.APPLICATION_XML)
	public RespPojo ping(){
		
		return initClientResponse(String.valueOf(Status.OK.getStatusCode()), Status.OK.getReasonPhrase());
	}
	
	
	/**
	 *  Initialize web service reponse for clients
	 *  
	 * @param code
	 * @param status
	 * @return
	 */
	private RespPojo initClientResponse(String code, String status){
		
		RespPojo responsePojo = new RespPojo();
		responsePojo.setCode(code);
		responsePojo.setStatus(status);
		
		return responsePojo;
	}
	
}
