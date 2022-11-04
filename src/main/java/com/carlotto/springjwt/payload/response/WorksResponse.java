package com.carlotto.springjwt.payload.response;

public class WorksResponse {
	
	private String clientName;
	
	private String workName;
	
	private String status;

	public WorksResponse(String clientName, String workName, String status) {
		this.clientName = clientName;
		this.workName = workName;
		this.status = status;
	}

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

	public void setStatus(String status) {
		this.status = status;
	}


}
