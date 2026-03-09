package com.sparta.spartatigers.domain.auth.repository;

import com.sparta.spartatigers.domain.auth.model.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

}
