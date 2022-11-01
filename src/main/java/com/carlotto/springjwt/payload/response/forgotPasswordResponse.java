package com.carlotto.springjwt.payload.response;

public class forgotPasswordResponse {
	private String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public forgotPasswordResponse(String token) {
		super();
		this.token = token;
	}

}
