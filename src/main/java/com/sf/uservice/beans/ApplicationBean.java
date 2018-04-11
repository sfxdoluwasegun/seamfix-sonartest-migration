package com.sf.uservice.beans;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.jms.MapMessage;
import javax.naming.NamingException;

import org.hibernate.HibernateException;
import org.jboss.logging.Logger;

import com.sf.unical.entity.main.settings.Settings;
import com.sf.uservice.enums.PortalSettings;
import com.sf.uservice.tools.QueryManager;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

@Singleton
public class ApplicationBean {

	private Logger log = Logger.getLogger(getClass());
	
	private String awsusername ;
	private String awskey ;
	private String awssecret ;
	private String awsbucket ;
	private String documentStorageDirectory;
	
	private Map<String, MapMessage> scannedFileMap = ExpiringMap.builder().expiration(15, TimeUnit.MINUTES).expirationPolicy(ExpirationPolicy.ACCESSED).build();
	
	@Inject
	private QueryManager queryManager;
	
	@PostConstruct
	public void initialize(){
		
		loadPortalSettings();
	}
	
	private void loadPortalSettings() {
		for(PortalSettings portalSetting : PortalSettings.values()){
			
			String value = null;
			try{
				Settings setting = queryManager.createSettings(portalSetting.getName(), 
						portalSetting.getValue(), 
						portalSetting.getDescription());
				
				value = setting.getValue();
			}
			catch(HibernateException | NamingException ex){
				log.error("ApplicationBean.loadPortalSettings", ex);
				log.error(portalSetting.name() + " setting not loaded");
				
				break;
			}
			
			if(PortalSettings.AMAZON_USERNAME.equals(portalSetting))
				setAwsusername(value);
			if(PortalSettings.AMAZON_ACCESS_KEY.equals(portalSetting))
				setAwskey(value);
			if(PortalSettings.AMAZON_SECRET_KEY.equals(portalSetting))
				setAwssecret(value);
			if(PortalSettings.AMAZON_BUCKET_NAME.equals(portalSetting))
				setAwsbucket(value);
			if(PortalSettings.DOCUMENT_STORAGE_DIRECTORY.equals(portalSetting))
				setDocumentStorageDirectory(value);
		}
		
		log.info("Portal settings loaded");
	}
	
	public String getAwsusername() {
		return awsusername;
	}

	public void setAwsusername(String awsusername) {
		this.awsusername = awsusername;
	}

	public String getAwskey() {
		return awskey;
	}

	public void setAwskey(String awskey) {
		this.awskey = awskey;
	}

	public String getAwssecret() {
		return awssecret;
	}

	public void setAwssecret(String awssecret) {
		this.awssecret = awssecret;
	}

	public String getAwsbucket() {
		return awsbucket;
	}

	public void setAwsbucket(String awsbucket) {
		this.awsbucket = awsbucket;
	}

	public String getDocumentStorageDirectory() {
		return documentStorageDirectory;
	}

	public void setDocumentStorageDirectory(String documentStorageDirectory) {
		this.documentStorageDirectory = documentStorageDirectory;
	}

	public Map<String, MapMessage> getScannedFileMap() {
		return scannedFileMap;
	}

	public void setScannedFileMap(Map<String, MapMessage> scannedFileMap) {
		this.scannedFileMap = scannedFileMap;
	}

}
