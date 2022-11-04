package com.carlotto.springjwt.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carlotto.springjwt.models.ERole;
import com.carlotto.springjwt.models.InvalidToken;
import com.carlotto.springjwt.models.Role;
import com.carlotto.springjwt.models.User;
import com.carlotto.springjwt.payload.request.ForgotPassword;
import com.carlotto.springjwt.payload.request.LoginRequest;
import com.carlotto.springjwt.payload.request.ResetPassRequest;
import com.carlotto.springjwt.payload.request.SignupRequest;
import com.carlotto.springjwt.payload.response.JwtResponse;
import com.carlotto.springjwt.payload.response.MessageResponse;
import com.carlotto.springjwt.payload.response.forgotPasswordResponse;
import com.carlotto.springjwt.repository.RoleRepository;
import com.carlotto.springjwt.repository.TokenRepository;
import com.carlotto.springjwt.repository.UserRepository;
import com.carlotto.springjwt.security.jwt.AuthTokenFilter;
import com.carlotto.springjwt.security.jwt.JwtUtils;
import com.carlotto.springjwt.security.services.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;
	
	@Autowired
	EmailController email;
	
	@Autowired
	TokenRepository tokenRepository;
	
	@Autowired
	AuthTokenFilter authTokenFilter;
	
	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		Authentication authentication = null;
		if(!loginRequest.getEmail().isEmpty()) {
			if(userRepository.existsByEmail(loginRequest.getEmail())){
				Optional<User> user = userRepository.findByEmail(loginRequest.getEmail());
				authentication = authenticationManager.authenticate(
						new UsernamePasswordAuthenticationToken(user.get().getUsername(), loginRequest.getPassword()));
			}
		}else {
			authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
		}

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
				.collect(Collectors.toList());

		return ResponseEntity.ok(
				new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles));
	}
	
	@PostMapping("/forgotPassword")
	public ResponseEntity<?> registerUser(@Valid @RequestBody ForgotPassword forgotPassword) {
		String jwt = null;
		if(userRepository.existsByEmail(forgotPassword.getEmail())) {
			Optional<User> userOp = userRepository.findByEmail(forgotPassword.getEmail());
			User user = userOp.get();
			Random ramdom = new Random();
			String psw = "teste" + ramdom.nextInt(0, 10);
			System.out.println(psw);
			user.setPassword(encoder.encode(psw));
			userRepository.save(user);
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(user.getUsername(), psw));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			jwt = jwtUtils.generateJwtTokenForgotPassword(authentication);
			fireToken(user, jwt);
		}
		String link = "localhost:8081/api/auth/forgotPasswordPage/";
		String msg = "Olá, esse é o link  para redefinir sua senha!" + System.lineSeparator() + link + jwt;
		if(email.sendMail("Redefinir Senha", msg, forgotPassword.getEmail())) {
			return ResponseEntity.ok(new MessageResponse("Você recebeu um e-mail para redefinir a senha, verifique em seu endereço: " + forgotPassword.getEmail()));
		}else {
			return ResponseEntity.ok("Tivemos um problema ao enviar o e-mail, tente novamente mais tarde");
		}
	}
	
	@GetMapping("/forgotPasswordPage/{token}")
	public ResponseEntity<?>  getToken(@PathVariable String token) {
		if(token != null) {
			return ResponseEntity.ok(new forgotPasswordResponse(token));
		}
		return ResponseEntity.ok("Não foi possivel retornar o token");
	}
	
	@PostMapping("/forgotPasswordPage/{token}")
	public ResponseEntity<?>  forgotPasswordPage(@Valid @RequestBody ResetPassRequest resetPassRequest, @PathVariable String token) {
		if(token != null) {
			if (resetPassRequest.getPassword().equals(resetPassRequest.getConfirmPassword())) {
				String username = jwtUtils.getUserNameFromJwtToken(token);
				Optional<User> userOpt = userRepository.findByUsername(username);
				User user = userOpt.get();
				user.setPassword(encoder.encode(resetPassRequest.getPassword()));
				userRepository.save(user);
				return ResponseEntity.ok("Senha redefinida com sucesso");
			}
		}
		return ResponseEntity.ok("Não foi possivel redefinir a senha");
	}
	
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
		}

		// Create new user's account
		User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
				encoder.encode(signUpRequest.getPassword()));

		Set<String> strRoles = signUpRequest.getRole();
		Set<Role> roles = new HashSet<>();

		if (strRoles == null) {
			Role userRole = roleRepository.findByName(ERole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			roles.add(userRole);
		} else {
			strRoles.forEach(role -> {
				switch (role) {
				case "ROLE_ADMIN":
					Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(adminRole);

					break;
				case "ROLE_MODERATOR":
					Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(modRole);

					break;
				default:
					Role userRole = roleRepository.findByName(ERole.ROLE_USER)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(userRole);
				}
			});
		}

		user.setRoles(roles);
		userRepository.save(user);
		
		String msg = "Olá " + user.getUsername() + ", obrigado por se cadastrar!";
		email.sendMail("Cadastro", msg, user.getEmail());
		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}
	
	@GetMapping("/logout")
	public ResponseEntity<?> logout(HttpServletRequest request) {
		String token = authTokenFilter.getToken(request);
		String username = jwtUtils.getUserNameFromJwtToken(token);
		Optional<User> userOpt = userRepository.findByUsername(username);
		User user = userOpt.get();
		if(fireToken(user, token)) {
			return ResponseEntity.ok("Logout!");
		}
		return ResponseEntity.ok("Estamos com problemas!");
	}
	
	private boolean fireToken(User user, String token) {
		try {
			InvalidToken invalidToken = null;
			invalidToken = new InvalidToken(user.getEmail(), token);
			tokenRepository.save(invalidToken);
			return true;
		}catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
}
