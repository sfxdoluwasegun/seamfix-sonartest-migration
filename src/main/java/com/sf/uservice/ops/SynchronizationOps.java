package com.sf.uservice.ops;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.naming.NamingException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.hibernate.HibernateException;
import org.jboss.logging.Logger;

import com.sf.unical.entity.main.alumni.Alumni;
import com.sf.unical.entity.main.docs.Docs;
import com.sf.unical.entity.request.requesthistory.RequestHistory;
import com.sf.unical.entity.request.requestpassport.RequestPassport;
import com.sf.uservice.beans.ApplicationBean;
import com.sf.uservice.beans.DocsPojo;
import com.sf.uservice.tools.AmazonManager;
import com.sf.uservice.tools.QueryManager;

/**
 * Handle Operations for alumni data and document synchronization
 * 
 * @author KOMOO
 */
@Stateless
public class SynchronizationOps {

	private final Logger log = Logger.getLogger(getClass());
	
	@Inject
	private ApplicationBean appBean;
	
	@Inject
	private QueryManager queryManager;
	
	@Inject
	private AmazonManager amazonManager;
	
	public SynchronizationOps() {
	}
	
	/**
	 * Synchronize document data to AWS S3 and create/update data 
	 * 
	 * @param pojo
	 */
	public void synchronizeAlumniDocument(DocsPojo documentPojo){
		
		String alumnusMatricNo = documentPojo.getMatricNo();
		String formattedMatricNo = alumnusMatricNo.replaceAll("/", "^");
		
		String parentDirectory = new StringBuilder(appBean.getDocumentStorageDirectory())
				.append("/").append(formattedMatricNo)
				.toString();
		String filePath = new StringBuilder().append("/")
				.append(Calendar.getInstance().getTimeInMillis())
				.append("^").append(formattedMatricNo)
				.append(".pdf")
				.toString();
		String tempFileLocationPath = new StringBuilder(parentDirectory).append(filePath).toString();
		String amazonS3Key = new StringBuilder().append(formattedMatricNo).append(filePath).toString();
		
		File documentFile = null;
		try{
			 documentFile = new File(tempFileLocationPath);
			
			FileUtils.writeByteArrayToFile(documentFile, documentPojo.getScannedDoc());
			
			amazonManager.uploadFileSingleOperation(documentFile, amazonS3Key);
			log.info("Uploaded file with AmazonS3Key - " + amazonS3Key);
			
			Alumni alumni = queryManager.getAlumniByMatricNoAndEagerDocument(documentPojo.getMatricNo());
			
			if(alumni != null)
				createOrUpdateAlumniDocument(alumni, amazonS3Key);
			else
				createNewAlumniAndDocumentRecord(documentPojo.getMatricNo(), documentPojo.getCampus(), amazonS3Key);
				
		}
		catch (Exception e){
			log.error("SynchronizationOps.synchronizeAlumniDocument", e);
		}
		finally{
			FileUtils.deleteQuietly(new File(parentDirectory));
		}
	}
	
	/**
	 * Handle resynchronization of alumni document. {@link Alumni} with processed transcript
	 * requests are declined resync.
	 * 
	 * @param pojo
	 */
	public void resynchronizeAlumniDocument(DocsPojo pojo) {
		
		try{
			/*Alumni alumni = queryManager.getAlumniByMatricNoAndEagerDocument(pojo.getMatricNo());
			if(alumni == null)
				return;*/
			
			/*RequestPassport passport = queryManager.getRequestPassportByAlumni(alumni);
			if(passport != null){
				if(isRequestProcessedAlready(passport)){
					log.debug("Document Resync declined for Processed RequestPassport: " + pojo.getMatricNo());
					return;
				}
			}*/
			
			synchronizeAlumniDocument(pojo);
			
		}
		catch (Exception e){
			log.error("SynchronizationOps.replaceAlumniScannedDocument", e);
		}
	}
	
	/**
	 * Create {@link Docs} and update or create {@link Alumni} for the document
	 * 
	 * @param documentObj
	 * @param amazonS3Key
	 * 
	 * @throws NamingException 
	 * @throws NumberFormatException 
	 */
	private void createOrUpdateAlumniDocument(Alumni alumni, String amazonS3Key) 
			throws NumberFormatException, NamingException {
		
		Docs alumniDoc = alumni.getDocs();
		
		if(alumniDoc == null){
			alumniDoc = createNewDocument(amazonS3Key);
			
			alumni.setDocs(alumniDoc);
			
			queryManager.getService().updateRecord(alumni);
			return;
		}
		else
		{
			String deleteKey = alumniDoc.getPath();
			
			alumniDoc.setPath(amazonS3Key);
			alumniDoc.setDocument(null);
			
			queryManager.getService().updateRecord(alumniDoc);
			
			// delete exisiting file on amazon S3
			amazonManager.deleteFileSingleOperation(deleteKey);
			
			return;
		}
	}
	
	/**
	 * Create new {@link Alumni} and {@link Docs} entries
	 * 
	 * @param matricNo
	 * @param campus
	 * @param amazonS3Key
	 */
	private void createNewAlumniAndDocumentRecord(String matricNo, String campus, String amazonS3Key){
		try {
			
			Docs alumniDoc = createNewDocument(amazonS3Key);
			
			String uniqueId =  RandomStringUtils.randomNumeric(10);
			while (queryManager.getAlumniByOrbitaId(Long.valueOf(uniqueId)) != null){
				uniqueId = RandomStringUtils.randomNumeric(10);
			}
			
			Alumni alumni = new Alumni();
			alumni.setMatricNo(matricNo);
			alumni.setDigitized(false);
			alumni.setCampus(campus);
			alumni.setOrbitaId(Long.valueOf(uniqueId));
			alumni.setDocs(alumniDoc);
			
			queryManager.getService().createNewRecord(alumni);
			
		} catch (HibernateException | NamingException e) {
			log.error("SynchronizationOps.createOrUpdateAlumniDocument", e);
		} 
	}
	
	/**
	 * Create new {@link Docs} with the new amazonKey
	 * 
	 * @param amazonS3Key
	 * @return created document
	 */
	private Docs createNewDocument(String amazonS3Key){
		Docs doc = new Docs();
		doc.setDate(new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
		doc.setDocument(null);
		doc.setPath(amazonS3Key);
		
		doc = (Docs) queryManager.getService().createNewRecord(doc);
		
		return doc;
	}
	
	/**
	 * Checks if a {@link RequestPassport} is processed or shipped.
	 * 
	 * @param passport
	 * @return
	 */
	private boolean isRequestProcessedAlready(RequestPassport passport){
		boolean processed = false;
		
		List<RequestHistory> requestHistories = queryManager.getRequestHistoryByRequestPassport(passport);
		
		if (requestHistories != null){
			for (RequestHistory requestHistory : requestHistories){
				if (requestHistory.isProcessed() || requestHistory.isShipped()){
					processed = true;
				}
			}
		}
		return processed;
	}
	
}
