package com.example.user.mapper;

import com.example.user.dto.UserCreateRequest;
import com.example.user.dto.UserResponse;
import com.example.user.model.entity.UserAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakId", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    UserAccount toEntity(UserCreateRequest request);

    UserResponse toResponse(UserAccount user);
}
