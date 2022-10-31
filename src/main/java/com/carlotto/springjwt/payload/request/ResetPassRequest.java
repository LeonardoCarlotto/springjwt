package com.carlotto.springjwt.payload.request;

import javax.validation.constraints.NotBlank;

public class ResetPassRequest {
	@NotBlank
	private String confirmPassword;

	@NotBlank
	private String password;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

}
