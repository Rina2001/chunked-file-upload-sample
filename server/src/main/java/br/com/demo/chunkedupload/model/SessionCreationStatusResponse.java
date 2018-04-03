package br.com.demo.chunkedupload.model;

import br.com.demo.chunkedupload.data.Session;

@javax.xml.bind.annotation.XmlRootElement
public class SessionCreationStatusResponse {
	public static SessionCreationStatusResponse fromSession(Session session) {
		SessionCreationStatusResponse model = new SessionCreationStatusResponse();

		model.setSessionId(session.getId());
		model.setUserId(session.getUser());
		model.setFileName(session.getFileInfo().getFileName());

		return model;
	}

	private String fileName;

	private String sessionId;

	private Long userId;

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * @return the userId
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @param sessionId
	 *            the sessionId to set
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

}
