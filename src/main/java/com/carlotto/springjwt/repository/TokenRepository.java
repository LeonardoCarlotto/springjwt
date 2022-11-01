package com.carlotto.springjwt.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carlotto.springjwt.models.InvalidToken;

@Repository
public interface TokenRepository extends JpaRepository<InvalidToken, Long> {

	Optional<InvalidToken> findByEmail(String email);

	Optional<InvalidToken> findByToken(String token);

	Boolean existsByEmail(String email);
	
	Boolean existsByToken(String token);

}
