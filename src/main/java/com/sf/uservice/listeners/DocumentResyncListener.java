package com.sf.uservice.listeners;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.jboss.ejb3.annotation.ResourceAdapter;
import org.jboss.logging.Logger;

import com.sf.uservice.beans.DocsPojo;
import com.sf.uservice.ops.SynchronizationOps;


/**
 * Listen for incoming documents
 * Sends documents queued for re syncing for operation
 * 
 * @author KOMOO
 *
 */

@MessageDriven(mappedName = "queue/DocumentReSync", activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"), 
		@ActivationConfigProperty(propertyName="destination", propertyValue="queue/DocumentReSync"), 
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")})
@ResourceAdapter("activemq")
public class DocumentResyncListener implements MessageListener {
	
	private final Logger log = Logger.getLogger(getClass());

	@Inject
	private SynchronizationOps syncOps;

	@Override
	public void onMessage(Message message) {
		
		try{
			MapMessage mapMessage = (MapMessage) message;
			
			DocsPojo document = new DocsPojo();
			document.setMatricNo(mapMessage.getString("matricNumber"));
			document.setCampus(mapMessage.getString("campus"));
			document.setScannedDoc(mapMessage.getBytes("scannedBytes"));
			
			syncOps.resynchronizeAlumniDocument(document);
		}
		catch(JMSException e){
			log.error("DocumentResyncListener.onMessage", e);
		}
		
	}

}
