package com.carlotto.springjwt.payload.response;

import com.carlotto.springjwt.models.User;

public class UserResponse {

	public UserResponse(User user) {
		this.email = user.getEmail();
		this.username = user.getUsername();
		this.saldo = user.getSaldo();
	}

	private String username;

	private String email;
	
	private Double saldo;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Double getSaldo() {
		return saldo;
	}

	public void setSaldo(Double saldo) {
		this.saldo = saldo;
	}

}
