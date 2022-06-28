package com.pge.dataload.vo;

public class RelatedManualLinkDetails {

	private String migratableReference=null;
	private String rmLinkTitle=null;
	private String rmEntityId=null;
	private String rmTypeArticleId=null;
	
	private String processingStatus=null;
	private String errorMessage=null;
	private String kaArticleId=null;
	
	public String getKaArticleId() {
		return kaArticleId;
	}
	public void setKaArticleId(String kaArticleId) {
		this.kaArticleId = kaArticleId;
	}
	public String getMigratableReference() {
		return migratableReference;
	}
	public void setMigratableReference(String migratableReference) {
		this.migratableReference = migratableReference;
	}
	public String getRmLinkTitle() {
		return rmLinkTitle;
	}
	public void setRmLinkTitle(String rmLinkTitle) {
		this.rmLinkTitle = rmLinkTitle;
	}
	public String getRmEntityId() {
		return rmEntityId;
	}
	public void setRmEntityId(String rmEntityId) {
		this.rmEntityId = rmEntityId;
	}
	public String getRmTypeArticleId() {
		return rmTypeArticleId;
	}
	public void setRmTypeArticleId(String rmTypeArticleId) {
		this.rmTypeArticleId = rmTypeArticleId;
	}
	public String getProcessingStatus() {
		return processingStatus;
	}
	public void setProcessingStatus(String processingStatus) {
		this.processingStatus = processingStatus;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	
}
