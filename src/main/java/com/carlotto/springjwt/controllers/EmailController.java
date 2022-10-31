package com.carlotto.springjwt.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailController {
	@Autowired
	private JavaMailSender mailSender;

	public String sendMail(String Subject, String mensagem, String email) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setSubject(Subject);
		message.setText(mensagem);
		message.setTo(email);
		message.setFrom("carlotto.mail@gmail.com");

		try {
			mailSender.send(message);
			return "Email enviado com sucesso!";
		} catch (Exception e) {
			e.printStackTrace();
			return "Erro ao enviar email.";
		}
	}
}
