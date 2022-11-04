package com.carlotto.springjwt.controllers;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carlotto.springjwt.models.User;
import com.carlotto.springjwt.models.Works;
import com.carlotto.springjwt.payload.request.ResetPassRequest;
import com.carlotto.springjwt.payload.request.WorkRequest;
import com.carlotto.springjwt.payload.response.UserResponse;
import com.carlotto.springjwt.repository.UserRepository;
import com.carlotto.springjwt.repository.WorkRepository;
import com.carlotto.springjwt.security.services.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	WorkRepository workRepository;

	@Autowired
	PasswordEncoder encoder;
	
	@GetMapping("/all")
	public String allAccess() {
		return "Public Content.";
	}

	@GetMapping("/user")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<?> userAccess() {
		User user = getUser();
		return ResponseEntity.ok(new UserResponse(user));		
	}

	@GetMapping("/mod")
	@PreAuthorize("hasRole('MODERATOR')")
	public String moderatorAccess() {
		return "Moderator Board.";
	}

	@GetMapping("/admin")
	@PreAuthorize("hasRole('ADMIN')")
	public String adminAccess() {
		return "Admin Board.";
	}

	@PostMapping("/resetPassword")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<?> resetPass(@Valid @RequestBody ResetPassRequest resetPassRequest) {
		if (resetPassRequest.getPassword().equals(resetPassRequest.getConfirmPassword())) {
			User user = getUser();
			user.setPassword(encoder.encode(resetPassRequest.getPassword()));
			userRepository.save(user);
			return ResponseEntity.ok("Reset password for: " + user.getEmail());
		}else {
			return ResponseEntity.ok("Senhas não conferem");
		}
	}
	
	@PostMapping("/createWork")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> createWork(@Valid @RequestBody WorkRequest workRequest) {
		Works work = new Works();
		if(userRepository.findByUsername(workRequest.getClientName()) != null ) {
			work.setClientName(workRequest.getClientName());
			work.setWorkName(workRequest.getWorkName());
			work.setStatus(workRequest.getStatus());
			workRepository.save(work);
			return ResponseEntity.ok("Work criado com sucesso!");
		}
		return ResponseEntity.ok("Cliente não existe");
	}
	
	@GetMapping("/works")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<?> createWork() {
		User user = getUser();
		List<Works> works = workRepository.findAllByClientName(user.getUsername());
		return ResponseEntity.ok(works);
	}
	
	private User getUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		Optional<User> userOptional = userRepository.findByEmail(userDetails.getEmail());
		User user = userOptional.get();
		return user;
	}
	
}
