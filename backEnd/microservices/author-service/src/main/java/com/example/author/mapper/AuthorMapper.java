package com.example.author.mapper;

import com.example.author.dto.AuthorRequest;
import com.example.author.dto.AuthorResponse;
import com.example.author.model.entity.Author;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface AuthorMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Author toEntity(AuthorRequest request);

    AuthorResponse toResponse(Author author);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(AuthorRequest request, @MappingTarget Author author);
}
