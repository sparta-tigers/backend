package com.sparta.spartatigers.domain.auth.repository;

import com.sparta.spartatigers.domain.auth.model.OAuthProvider;
import com.sparta.spartatigers.domain.auth.model.Oauth;
import com.sparta.spartatigers.domain.user.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthRepository extends JpaRepository<Oauth, Long> {

    Optional<Oauth> findByProviderAndProviderId(OAuthProvider provider, String providerId);
    boolean existsByUserAndProvider(User user, OAuthProvider provider);
}
