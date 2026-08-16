package com.mstan.shop.user.service;

import com.mstan.shop.common.exception.ResourceNotFoundException;
import com.mstan.shop.common.util.PagedResponse;
import com.mstan.shop.user.dto.UpdateProfileRequest;
import com.mstan.shop.user.dto.UpdateUserRolesRequest;
import com.mstan.shop.user.dto.UpdateUserStatusRequest;
import com.mstan.shop.user.dto.UserAdminResponse;
import com.mstan.shop.user.dto.UserProfileResponse;
import com.mstan.shop.user.entity.Role;
import com.mstan.shop.user.entity.RoleName;
import com.mstan.shop.user.entity.User;
import com.mstan.shop.user.mapper.UserMapper;
import com.mstan.shop.user.repository.RoleRepository;
import com.mstan.shop.user.repository.UserRepository;
import com.mstan.shop.user.repository.UserSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        return userMapper.toProfileResponse(findUser(userId));
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = findUser(userId);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        userRepository.save(user);
        return userMapper.toProfileResponse(user);
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserAdminResponse> getAllUsers(RoleName role, Boolean enabled, String search, Pageable pageable) {
        Specification<User> spec = Specification.allOf(
                UserSpecifications.notDeleted(),
                UserSpecifications.hasRole(role),
                UserSpecifications.isEnabled(enabled),
                UserSpecifications.emailContains(search)
        );

        Page<UserAdminResponse> page = userRepository.findAll(spec, pageable).map(userMapper::toAdminResponse);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public UserAdminResponse getUserByIdAdmin(UUID userId) {
        return userMapper.toAdminResponse(findUser(userId));
    }

    @Transactional
    public UserAdminResponse updateUserStatus(UUID userId, UpdateUserStatusRequest request) {
        User user = findUser(userId);
        user.setEnabled(request.enabled());
        userRepository.save(user);
        return userMapper.toAdminResponse(user);
    }

    @Transactional
    public UserAdminResponse updateUserRoles(UUID userId, UpdateUserRolesRequest request, UUID currentAdminId) {
        if (userId.equals(currentAdminId)) {
            throw new IllegalArgumentException("Un administrateur ne peut pas modifier ses propres rôles");
        }

        User user = findUser(userId);

        Set<Role> roles = new LinkedHashSet<>();
        for (RoleName roleName : request.roles()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> ResourceNotFoundException.of("Rôle", roleName));
            roles.add(role);
        }

        user.setRoles(roles);
        userRepository.save(user);
        return userMapper.toAdminResponse(user);
    }

    @Transactional
    public void softDeleteUser(UUID userId, UUID currentAdminId) {
        if (userId.equals(currentAdminId)) {
            throw new IllegalArgumentException("Un administrateur ne peut pas supprimer son propre compte");
        }

        User user = findUser(userId);
        user.setDeleted(true);
        user.setDeletedAt(Instant.now());
        user.setEnabled(false);
        userRepository.save(user);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", userId));
    }
}
