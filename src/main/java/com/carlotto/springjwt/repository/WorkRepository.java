package com.carlotto.springjwt.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carlotto.springjwt.models.Works;


public interface WorkRepository extends JpaRepository<Works, Long> {

	Optional<Works> findByWorkName(String workName);
	
	Optional<Works> findByClientName(String clientName);

	List<Works> findAllByClientName(String username);

}
