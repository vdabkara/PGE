package com.pge.dataload.vo;

import java.sql.Timestamp;
import java.util.List;

public class DocumentDetails {

	private String typeArticleId=null;
	private String dynamicEntityId=null;
	private String locale=null;
	private Timestamp publishStartDate=null;
	private Timestamp publishEndDate=null;
	private String ownerId=null;
	private String majorVersion=null;
	private String minorVersion=null;
	private Timestamp lastModifiedDate=null;
	private String lastModifiedBy=null;
	private String publishedBy=null;
	private String weightValue;
	private String title=null;
	
	private String channelRegion=null;
	private List<TagDetails> topicsList=null;
	private List<TagDetails> productList=null;
	private List<TagDetails> departmentUserGroupList=null;
	private List<TagDetails> departmentCategoryList=null;
	
	private String htmlContent=null;
	private String relatedLinkContent=null;
	
	private List<RelatedManualLinkDetails> relatedManualLinksList= null;
	private List<InlineImageDetails> assetEDImagesList=null;
	private List<InlineImageDetails> otherInlineImages=null;
	private List<InnerLinkDetails> contentEDInnerLinksList = null;
	private List<InnerLinkDetails> otherInnerLinksList = null;
	private List<AttachmentDetails> attachmentsList= null;
	
	private String kaContentPattern=null;
	private String kaChannelRefKey=null;
	private String kaChannelRecordId=null;
	private String kaChannelName=null;
	private String kaRecordId=null;
	private String kaArticleId=null;
	private String kaDocumentId=null;
	private String kaVersionId=null;
	private String kaVersion=null;
	private String kaPublishStatus=null;
	private String allUserGroupsMapped=null;
	private String allCategoriesMapped=null;
	private String allProductsMapped=null;
	private String allImagesMapped=null;
	private String allInnerLinksMapped=null;
	private String allRelatedLinksMapped=null;
	private String allAttachmentsMapped=null;
	
	private String processingStatus=null;
	private String dataErrors=null;
	private String errorCodes=null;
	private String errorMessage=null;
	
	private SchemaFieldDetails schemaDetails = null;
	
	public String getRelatedLinkContent() {
		return relatedLinkContent;
	}
	public void setRelatedLinkContent(String relatedLinkContent) {
		this.relatedLinkContent = relatedLinkContent;
	}
	public SchemaFieldDetails getSchemaDetails() {
		return schemaDetails;
	}
	public void setSchemaDetails(SchemaFieldDetails schemaDetails) {
		this.schemaDetails = schemaDetails;
	}
	public String getKaVersionId() {
		return kaVersionId;
	}
	public void setKaVersionId(String kaVersionId) {
		this.kaVersionId = kaVersionId;
	}
	public String getKaChannelRecordId() {
		return kaChannelRecordId;
	}
	public void setKaChannelRecordId(String kaChannelRecordId) {
		this.kaChannelRecordId = kaChannelRecordId;
	}
	public String getKaChannelName() {
		return kaChannelName;
	}
	public void setKaChannelName(String kaChannelName) {
		this.kaChannelName = kaChannelName;
	}
	public List<AttachmentDetails> getAttachmentsList() {
		return attachmentsList;
	}
	public void setAttachmentsList(List<AttachmentDetails> attachmentsList) {
		this.attachmentsList = attachmentsList;
	}
	public String getAllAttachmentsMapped() {
		return allAttachmentsMapped;
	}
	public void setAllAttachmentsMapped(String allAttachmentsMapped) {
		this.allAttachmentsMapped = allAttachmentsMapped;
	}
	public String getKaContentPattern() {
		return kaContentPattern;
	}
	public void setKaContentPattern(String kaContentPattern) {
		this.kaContentPattern = kaContentPattern;
	}
	public String getKaChannelRefKey() {
		return kaChannelRefKey;
	}
	public void setKaChannelRefKey(String kaChannelRefKey) {
		this.kaChannelRefKey = kaChannelRefKey;
	}
	public String getKaRecordId() {
		return kaRecordId;
	}
	public void setKaRecordId(String kaRecordId) {
		this.kaRecordId = kaRecordId;
	}
	public String getKaArticleId() {
		return kaArticleId;
	}
	public void setKaArticleId(String kaArticleId) {
		this.kaArticleId = kaArticleId;
	}
	public String getKaDocumentId() {
		return kaDocumentId;
	}
	public void setKaDocumentId(String kaDocumentId) {
		this.kaDocumentId = kaDocumentId;
	}
	public String getKaVersion() {
		return kaVersion;
	}
	public void setKaVersion(String kaVersion) {
		this.kaVersion = kaVersion;
	}
	public String getKaPublishStatus() {
		return kaPublishStatus;
	}
	public void setKaPublishStatus(String kaPublishStatus) {
		this.kaPublishStatus = kaPublishStatus;
	}
	public String getAllUserGroupsMapped() {
		return allUserGroupsMapped;
	}
	public void setAllUserGroupsMapped(String allUserGroupsMapped) {
		this.allUserGroupsMapped = allUserGroupsMapped;
	}
	public String getAllCategoriesMapped() {
		return allCategoriesMapped;
	}
	public void setAllCategoriesMapped(String allCategoriesMapped) {
		this.allCategoriesMapped = allCategoriesMapped;
	}
	public String getAllProductsMapped() {
		return allProductsMapped;
	}
	public void setAllProductsMapped(String allProductsMapped) {
		this.allProductsMapped = allProductsMapped;
	}
	public String getAllImagesMapped() {
		return allImagesMapped;
	}
	public void setAllImagesMapped(String allImagesMapped) {
		this.allImagesMapped = allImagesMapped;
	}
	public String getAllInnerLinksMapped() {
		return allInnerLinksMapped;
	}
	public void setAllInnerLinksMapped(String allInnerLinksMapped) {
		this.allInnerLinksMapped = allInnerLinksMapped;
	}
	public String getAllRelatedLinksMapped() {
		return allRelatedLinksMapped;
	}
	public void setAllRelatedLinksMapped(String allRelatedLinksMapped) {
		this.allRelatedLinksMapped = allRelatedLinksMapped;
	}
	public String getProcessingStatus() {
		return processingStatus;
	}
	public void setProcessingStatus(String processingStatus) {
		this.processingStatus = processingStatus;
	}
	public String getDataErrors() {
		return dataErrors;
	}
	public void setDataErrors(String dataErrors) {
		this.dataErrors = dataErrors;
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
	public List<InnerLinkDetails> getContentEDInnerLinksList() {
		return contentEDInnerLinksList;
	}
	public void setContentEDInnerLinksList(List<InnerLinkDetails> contentEDInnerLinksList) {
		this.contentEDInnerLinksList = contentEDInnerLinksList;
	}
	public List<InnerLinkDetails> getOtherInnerLinksList() {
		return otherInnerLinksList;
	}
	public void setOtherInnerLinksList(List<InnerLinkDetails> otherInnerLinksList) {
		this.otherInnerLinksList = otherInnerLinksList;
	}
	public List<InlineImageDetails> getOtherInlineImages() {
		return otherInlineImages;
	}
	public void setOtherInlineImages(List<InlineImageDetails> otherInlineImages) {
		this.otherInlineImages = otherInlineImages;
	}
	public List<InlineImageDetails> getAssetEDImagesList() {
		return assetEDImagesList;
	}
	public void setAssetEDImagesList(List<InlineImageDetails> assetEDImagesList) {
		this.assetEDImagesList = assetEDImagesList;
	}
	public List<RelatedManualLinkDetails> getRelatedManualLinksList() {
		return relatedManualLinksList;
	}
	public void setRelatedManualLinksList(List<RelatedManualLinkDetails> relatedManualLinksList) {
		this.relatedManualLinksList = relatedManualLinksList;
	}
	public String getHtmlContent() {
		return htmlContent;
	}
	public void setHtmlContent(String htmlContent) {
		this.htmlContent = htmlContent;
	}
	public String getChannelRegion() {
		return channelRegion;
	}
	public void setChannelRegion(String channelRegion) {
		this.channelRegion = channelRegion;
	}
	public List<TagDetails> getTopicsList() {
		return topicsList;
	}
	public void setTopicsList(List<TagDetails> topicsList) {
		this.topicsList = topicsList;
	}
	public List<TagDetails> getProductList() {
		return productList;
	}
	public void setProductList(List<TagDetails> productList) {
		this.productList = productList;
	}
	public List<TagDetails> getDepartmentUserGroupList() {
		return departmentUserGroupList;
	}
	public void setDepartmentUserGroupList(List<TagDetails> departmentUserGroupList) {
		this.departmentUserGroupList = departmentUserGroupList;
	}
	public List<TagDetails> getDepartmentCategoryList() {
		return departmentCategoryList;
	}
	public void setDepartmentCategoryList(List<TagDetails> departmentCategoryList) {
		this.departmentCategoryList = departmentCategoryList;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTypeArticleId() {
		return typeArticleId;
	}
	public void setTypeArticleId(String typeArticleId) {
		this.typeArticleId = typeArticleId;
	}
	public String getDynamicEntityId() {
		return dynamicEntityId;
	}
	public void setDynamicEntityId(String dynamicEntityId) {
		this.dynamicEntityId = dynamicEntityId;
	}
	public String getLocale() {
		return locale;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}
	public Timestamp getPublishStartDate() {
		return publishStartDate;
	}
	public void setPublishStartDate(Timestamp publishStartDate) {
		this.publishStartDate = publishStartDate;
	}
	public Timestamp getPublishEndDate() {
		return publishEndDate;
	}
	public void setPublishEndDate(Timestamp publishEndDate) {
		this.publishEndDate = publishEndDate;
	}
	public String getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
	public String getMajorVersion() {
		return majorVersion;
	}
	public void setMajorVersion(String majorVersion) {
		this.majorVersion = majorVersion;
	}
	public String getMinorVersion() {
		return minorVersion;
	}
	public void setMinorVersion(String minorVersion) {
		this.minorVersion = minorVersion;
	}
	public Timestamp getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(Timestamp lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
	public String getPublishedBy() {
		return publishedBy;
	}
	public void setPublishedBy(String publishedBy) {
		this.publishedBy = publishedBy;
	}
	public String getWeightValue() {
		return weightValue;
	}
	public void setWeightValue(String weightValue) {
		this.weightValue = weightValue;
	}
	
	
	
	
	
}
