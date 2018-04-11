package com.sf.uservice.tools;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.jboss.logging.Logger;

import com.sf.uservice.beans.ApplicationBean;
import com.sf.uservice.beans.DocsPojo;

/**
 * Handles queuing/sending messages to {@link Destination}s
 * 
 * @author KOMOO
 */
@Stateless
public class MessageQueuer {

	private final Logger log = Logger.getLogger(getClass());
	
	@Resource(lookup = "java:/AmqConnectionFactory")
	private ConnectionFactory jmsConnectionFactory;
	
	@Resource(lookup = "java:/jms/queue/DocumentSync")
	private Queue documentSyncQueue;
	
	@Resource(lookup = "java:/jms/queue/DocumentReSync")
	private Queue documentResyncQueue;
	
	private Connection connection;
	private Session session;
	
	@Inject
	private ApplicationBean applicationBean;
	
	/**
	 * Perform bean initialization
	 */
	@PostConstruct
	public void initialize(){
		try {
			connection = jmsConnectionFactory.createConnection();
			connection.start();
			
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
		} catch (JMSException e) {
			log.error("MessageQueuer.initialize: Error connecting to queue factory", e);
		}
	}
	
	/**
	 * Perform clean up before container removes bean 
	 * 
	 */
	@PreDestroy
	public void cleanup(){
		closeSession(session);
		closeConnection(connection);
	}
	
	
	/**
	 * Queue document for syncing
	 * 
	 * @param documentPojo
	 */
	public void queueSyncDocument(DocsPojo documentPojo){
		MessageProducer messageProducer = null;
		Map<String, MapMessage> scannedFileMap = applicationBean.getScannedFileMap();
		
		try {
			if(scannedFileMap.containsKey(documentPojo.getMatricNo()))
				return;
			
			messageProducer = session.createProducer(documentSyncQueue);
			messageProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
			
			MapMessage mapMessage = (MapMessage) session.createMapMessage();
			
			mapMessage.setString("matricNumber", documentPojo.getMatricNo());
			mapMessage.setString("campus", documentPojo.getCampus());
			mapMessage.setBytes("scannedBytes", documentPojo.getScannedDoc());
			
			scannedFileMap.put(documentPojo.getMatricNo(), mapMessage);
			messageProducer.send(mapMessage);
			
		} catch (JMSException e) {
			log.error("MessageQueuer.queueSyncDocument", e);
		} 
		finally{
			closeMessageProducer(messageProducer);
		}		
	}
	
	
	/**
	 * Queue document for re-syncing
	 * 
	 * @param documentPojo
	 */
	public void queueResyncDocument(DocsPojo documentPojo){
		MessageProducer messageProducer = null;
		
		try {
			messageProducer = session.createProducer(documentResyncQueue);
			messageProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
			
			MapMessage mapMessage = (MapMessage) session.createMapMessage();
			
			mapMessage.setString("matricNumber", documentPojo.getMatricNo());
			mapMessage.setString("campus", documentPojo.getCampus());
			mapMessage.setBytes("scannedBytes", documentPojo.getScannedDoc());
			
			messageProducer.send(mapMessage);
			
		} catch (JMSException e) {
			log.error("MessageQueuer.queueSyncDocument", e);
		} 
		finally{
			closeMessageProducer(messageProducer);
		}		
	}
	
	
	/**
	 * Handle closing of {@link MessageProducer} resource
	 * 
	 * @param producer
	 */
	private void closeMessageProducer(MessageProducer producer){
		if(producer != null)
			try{
				producer.close();
				
			}catch(JMSException e){
				log.error("MessageQueuer.closeMessageProducer", e);
			}
		
	}
	
	/**
	 * Close an opened JMSSession.
	 * 
	 * @param session
	 */
	private void closeSession(Session session){

		if (session != null)
			try {
				session.close();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				log.error("", e);
			}
	}

	/**
	 * Close an opened JMSConnection.
	 * 
	 * @param connection
	 */
	private void closeConnection(Connection connection){

		if (connection != null)
			try {
				connection.close();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				log.error("", e);
			}
	}
}
