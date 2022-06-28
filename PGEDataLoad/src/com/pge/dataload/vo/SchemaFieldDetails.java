package com.pge.dataload.vo;

import java.util.List;

public class SchemaFieldDetails {

	private String background = null;
	private List<SchemaFieldDetails> detailsNodeList = null;
	
	private String contentHeaderHTML=null;
	private String contentHeader = null;
	private String description=null;
	private String question=null;
	private String knowledgeAlertDescription=null;
	private String oneSourceContent=null;
	
	private String summary=null;
	private String answer =null;
	
	private List<SchemaFieldDetails> questionAnswerList = null;
	
	private String highLevelProcess=null;
	private String detailedProcess = null;
	
	private String detailsTextArea=null;

	
	public String getContentHeaderHTML() {
		return contentHeaderHTML;
	}

	public void setContentHeaderHTML(String contentHeaderHTML) {
		this.contentHeaderHTML = contentHeaderHTML;
	}

	public String getBackground() {
		return background;
	}

	public void setBackground(String background) {
		this.background = background;
	}

	public List<SchemaFieldDetails> getDetailsNodeList() {
		return detailsNodeList;
	}

	public void setDetailsNodeList(List<SchemaFieldDetails> detailsNodeList) {
		this.detailsNodeList = detailsNodeList;
	}

	public String getContentHeader() {
		return contentHeader;
	}

	public void setContentHeader(String contentHeader) {
		this.contentHeader = contentHeader;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getKnowledgeAlertDescription() {
		return knowledgeAlertDescription;
	}

	public void setKnowledgeAlertDescription(String knowledgeAlertDescription) {
		this.knowledgeAlertDescription = knowledgeAlertDescription;
	}

	public String getOneSourceContent() {
		return oneSourceContent;
	}

	public void setOneSourceContent(String oneSourceContent) {
		this.oneSourceContent = oneSourceContent;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public List<SchemaFieldDetails> getQuestionAnswerList() {
		return questionAnswerList;
	}

	public void setQuestionAnswerList(List<SchemaFieldDetails> questionAnswerList) {
		this.questionAnswerList = questionAnswerList;
	}

	public String getHighLevelProcess() {
		return highLevelProcess;
	}

	public void setHighLevelProcess(String highLevelProcess) {
		this.highLevelProcess = highLevelProcess;
	}

	public String getDetailedProcess() {
		return detailedProcess;
	}

	public void setDetailedProcess(String detailedProcess) {
		this.detailedProcess = detailedProcess;
	}

	public String getDetailsTextArea() {
		return detailsTextArea;
	}

	public void setDetailsTextArea(String detailsTextArea) {
		this.detailsTextArea = detailsTextArea;
	}
	
	
	
}
