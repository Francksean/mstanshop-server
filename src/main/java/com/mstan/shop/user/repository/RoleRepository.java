package com.mstan.shop.user.repository;

import com.mstan.shop.user.entity.Role;
import com.mstan.shop.user.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
