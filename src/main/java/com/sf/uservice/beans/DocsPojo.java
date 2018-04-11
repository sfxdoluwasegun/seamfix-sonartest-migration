package com.sf.uservice.beans;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DocsPojo implements Serializable {
	
	private String matricNo;
	private String campus;
	private byte[] scannedDoc;
	
	public DocsPojo(){
	}
	
	public DocsPojo(String matricNo, 
			String campus,
			byte[] scannedDoc){
		
		this.matricNo = matricNo;
		this.campus = campus;
		this.scannedDoc = scannedDoc;
	}

	public String getMatricNo() {
		return matricNo;
	}

	public void setMatricNo(String matricNo) {
		this.matricNo = matricNo;
	}

	public byte[] getScannedDoc() {
		return scannedDoc;
	}

	public void setScannedDoc(final byte[] scannedDoc) {
		this.scannedDoc = scannedDoc;
	}

	public String getCampus() {
		return campus;
	}

	public void setCampus(String campus) {
		this.campus = campus;
	}

}