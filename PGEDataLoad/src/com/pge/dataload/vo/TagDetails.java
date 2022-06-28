package com.pge.dataload.vo;

public class TagDetails {
	
	private String vaItemName=null;
	private String kaItemRefKey=null;
	private String kaRecordId=null;
	private String kaItemName=null;
	private String processingStatus=null;
	private String errorCodes=null;
	private String errorMessage=null;
	private String dataErrors=null;
	
	
	
	public String getKaItemName() {
		return kaItemName;
	}
	public void setKaItemName(String kaItemName) {
		this.kaItemName = kaItemName;
	}
	public String getKaRecordId() {
		return kaRecordId;
	}
	public void setKaRecordId(String kaRecordId) {
		this.kaRecordId = kaRecordId;
	}
	public String getVaItemName() {
		return vaItemName;
	}
	public void setVaItemName(String vaItemName) {
		this.vaItemName = vaItemName;
	}
	public String getKaItemRefKey() {
		return kaItemRefKey;
	}
	public void setKaItemRefKey(String kaItemRefKey) {
		this.kaItemRefKey = kaItemRefKey;
	}
	public String getProcessingStatus() {
		return processingStatus;
	}
	public void setProcessingStatus(String processingStatus) {
		this.processingStatus = processingStatus;
	}
	public String getErrorCodes() {
		return errorCodes;
	}
	public void setErrorCodes(String errorCodes) {
		this.errorCodes = errorCodes;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getDataErrors() {
		return dataErrors;
	}
	public void setDataErrors(String dataErrors) {
		this.dataErrors = dataErrors;
	}
	
	

}
