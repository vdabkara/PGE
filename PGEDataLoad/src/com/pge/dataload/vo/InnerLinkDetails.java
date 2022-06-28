package com.pge.dataload.vo;

public class InnerLinkDetails {
	
	private String tagContent=null;
	private String hrefAttributeValue=null;
	private String innerLinkType=null;
	private String innerLinkTitle=null;
	private String innerLinkTypeId=null;
	
	private String processingStatus=null;
	private String errorMessage=null;
	private String kaDocumentId=null;
	private String kaInnerLinkURL=null;
	
	private DocumentDetails documentDetails = null;
	
	
	
	public DocumentDetails getDocumentDetails() {
		return documentDetails;
	}
	public void setDocumentDetails(DocumentDetails documentDetails) {
		this.documentDetails = documentDetails;
	}
	public String getTagContent() {
		return tagContent;
	}
	public void setTagContent(String tagContent) {
		this.tagContent = tagContent;
	}
	public String getInnerLinkType() {
		return innerLinkType;
	}
	public void setInnerLinkType(String innerLinkType) {
		this.innerLinkType = innerLinkType;
	}
	public String getInnerLinkTitle() {
		return innerLinkTitle;
	}
	public void setInnerLinkTitle(String innerLinkTitle) {
		this.innerLinkTitle = innerLinkTitle;
	}
	public String getInnerLinkTypeId() {
		return innerLinkTypeId;
	}
	public void setInnerLinkTypeId(String innerLinkTypeId) {
		this.innerLinkTypeId = innerLinkTypeId;
	}
	public String getKaInnerLinkURL() {
		return kaInnerLinkURL;
	}
	public void setKaInnerLinkURL(String kaInnerLinkURL) {
		this.kaInnerLinkURL = kaInnerLinkURL;
	}
	public String getHrefAttributeValue() {
		return hrefAttributeValue;
	}
	public void setHrefAttributeValue(String hrefAttributeValue) {
		this.hrefAttributeValue = hrefAttributeValue;
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
	public String getKaDocumentId() {
		return kaDocumentId;
	}
	public void setKaDocumentId(String kaDocumentId) {
		this.kaDocumentId = kaDocumentId;
	}
	
	

}
