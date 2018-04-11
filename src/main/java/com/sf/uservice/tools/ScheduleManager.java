package com.sf.uservice.tools;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.DependsOn;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.naming.NamingException;

import org.hibernate.HibernateException;
import org.jboss.ejb3.annotation.TransactionTimeout;
import org.jboss.logging.Logger;
import org.joda.time.LocalDateTime;

import com.sf.unical.entity.main.alumni.Alumni;
import com.sf.unical.entity.main.docs.Docs;
import com.sf.uservice.beans.DocsPojo;

@Startup
@DependsOn(value = "StartupManager")
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@AccessTimeout(unit = TimeUnit.MINUTES, value = 5)
public class ScheduleManager {

	private Logger log = Logger.getLogger(getClass());

	@Inject
	private QueryManager queryManager;
	
	@Inject
	private MessageQueuer messageQueuer;


	/**
	 * Initiate pushing of existing documents with stored bytes to AWS S3
	 */
	@Schedule(hour = "*", minute = "*/20", persistent=false, info="Every 20 minutes")
	@TransactionTimeout(unit = TimeUnit.MINUTES, value = 10)
	public void beginDocumentMigrationToAmazon() {
		
		log.info("beginDocumentMigrationToAmazon schedule triggered at " + LocalDateTime.now().toString());
		
		List<Docs> documentList = queryManager.getDocumentsWithBinaryData();
		
//		while (documentList != null && !documentList.isEmpty()){
			for (Docs document : documentList) {
				
				Alumni alumnus;
				try{
					alumnus = queryManager.getAlumniByDocumentId(document.getId());
					if(alumnus != null){
						DocsPojo documentPojo = new DocsPojo();
						documentPojo.setMatricNo(alumnus.getMatricNo());
						documentPojo.setCampus(alumnus.getCampus());
						documentPojo.setScannedDoc(document.getDocument());
						
						messageQueuer.queueSyncDocument(documentPojo);
					}
				}catch(HibernateException | NamingException e){
					continue;
				}
			}
			//reload list
			// documentList = queryManager.getDocumentsWithBinaryData();
//		}
		
	}

}
