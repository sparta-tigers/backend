package com.sparta.spartatigers.domain.foundation.user.auth.repository;

import com.sparta.spartatigers.domain.foundation.user.auth.model.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository
    extends CrudRepository<RefreshToken, String> {}
