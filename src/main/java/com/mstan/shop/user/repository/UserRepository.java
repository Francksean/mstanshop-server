package com.mstan.shop.user.repository;

import com.mstan.shop.user.entity.AuthProvider;
import com.mstan.shop.user.entity.RoleName;
import com.mstan.shop.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByAuthProviderAndProviderId(AuthProvider authProvider, String providerId);

    List<User> findByRoles_Name(RoleName roleName);
}
