package com.carlotto.springjwt.payload.request;

import javax.validation.constraints.NotBlank;

public class WorkRequest {
	
	@NotBlank
	private String clientName;
	
	@NotBlank
	private String workName;
	
	@NotBlank
	private String status;

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getWorkName() {
		return workName;
	}

	public void setWorkName(String workName) {
		this.workName = workName;
	}

	public String getStatus() {
		return status;
	}

	public void String (String status) {
		this.status = status;
	}

}
