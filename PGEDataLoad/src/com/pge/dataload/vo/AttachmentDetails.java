package com.pge.dataload.vo;


public class AttachmentDetails {

	private String attachmentTitle=null;
	private String attachmentName=null;
	private String attachmentPath=null;
	private String extension=null;
	private String processingStatus=null;
	private String errorMessage=null;
	private String attachmentSize=null;
	private String attachmentType=null;
	private String inlineAttachmentPath=null;
	
	public String getAttachmentType() {
		return attachmentType;
	}
	public void setAttachmentType(String attachmentType) {
		this.attachmentType = attachmentType;
	}
	public String getInlineAttachmentPath() {
		return inlineAttachmentPath;
	}
	public void setInlineAttachmentPath(String inlineAttachmentPath) {
		this.inlineAttachmentPath = inlineAttachmentPath;
	}
	public String getAttachmentSize() {
		return attachmentSize;
	}
	public void setAttachmentSize(String attachmentSize) {
		this.attachmentSize = attachmentSize;
	}
	public String getAttachmentTitle() {
		return attachmentTitle;
	}
	public void setAttachmentTitle(String attachmentTitle) {
		this.attachmentTitle = attachmentTitle;
	}
	public String getAttachmentName() {
		return attachmentName;
	}
	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}
	public String getAttachmentPath() {
		return attachmentPath;
	}
	public void setAttachmentPath(String attachmentPath) {
		this.attachmentPath = attachmentPath;
	}
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
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