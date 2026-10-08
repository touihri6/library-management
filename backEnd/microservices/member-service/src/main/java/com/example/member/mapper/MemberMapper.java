package com.example.member.mapper;

import com.example.member.dto.MemberRequest;
import com.example.member.dto.MemberResponse;
import com.example.member.model.entity.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface MemberMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Member toEntity(MemberRequest request);

    MemberResponse toResponse(Member member);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(MemberRequest request, @MappingTarget Member member);
}
