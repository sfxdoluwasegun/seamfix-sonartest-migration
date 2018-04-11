/**
 * 
 */
package com.sf.uservice.enums;

/**
 * @author KOMOO
 */
public enum PortalSettings {

	AMAZON_USERNAME("AWS_USERNAME", "doluwasegun@seamfix.com", "Amazon WS username"), 
	AMAZON_ACCESS_KEY("AWS_ACCESS ID", "AKIAJX7DZC3A355X2FRQ", "AWS Access Key ID"), 
	AMAZON_SECRET_KEY("AWS_KEY", "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", "AWS Secret Access Key"), 
	AMAZON_BUCKET_NAME("AWS_BUCKET", "unical-itranscript-docs", "AWS Bucket name"),
	DOCUMENT_STORAGE_DIRECTORY("DOCUMENT_STORAGE_DIRECTORY", "/opt/liferay-portal-6.0.6/tomcat-6.0.29/webapps/sync_documents", "Directory for temporarily storing documents during sync");
	
	private String name;
	private String value;
	private String description;
	
	private PortalSettings(String name,
			String value,
			String description){
	
		setName(name);
		setValue(value);
		setDescription(description);
	}
	
	public PortalSettings fromName(String name){
		
		for(PortalSettings setting : PortalSettings.values()){
			if(setting.getName().equalsIgnoreCase(name))
				return setting;
		}
		
		return null;
	}

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	private void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	private void setDescription(String description) {
		this.description = description;
	}
	
}
