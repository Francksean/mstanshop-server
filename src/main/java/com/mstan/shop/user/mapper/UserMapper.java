package com.mstan.shop.user.mapper;

import com.mstan.shop.user.dto.UserAdminResponse;
import com.mstan.shop.user.dto.UserProfileResponse;
import com.mstan.shop.user.entity.Role;
import com.mstan.shop.user.entity.RoleName;
import com.mstan.shop.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserProfileResponse toProfileResponse(User user);

    @Mapping(target = "roles", expression = "java(toRoleNames(user.getRoles()))")
    UserAdminResponse toAdminResponse(User user);

    default Set<RoleName> toRoleNames(Set<Role> roles) {
        return roles.stream().map(Role::getName).collect(Collectors.toSet());
    }
}
