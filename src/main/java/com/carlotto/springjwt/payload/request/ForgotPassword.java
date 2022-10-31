package com.carlotto.springjwt.payload.request;

import javax.validation.constraints.*;

public class ForgotPassword {
	
	@NotBlank
	private String email;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
