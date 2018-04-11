package com.sf.uservice.beans;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RespPojo implements Serializable {
	
	private String status;
	private String description;
	private String code;
	private String entity;
	
	public RespPojo(){
	}
	
	public RespPojo(String status, 
					String description,
					String code,
					String entity){
		
		this.status = status;
		this.description = description;
		this.code = code;
		this.entity = entity;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

}