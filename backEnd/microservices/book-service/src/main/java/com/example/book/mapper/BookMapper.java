package com.example.book.mapper;

import com.example.book.dto.BookRequest;
import com.example.book.dto.BookResponse;
import com.example.book.model.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface BookMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Book toEntity(BookRequest request);

    @Mapping(target = "available", expression = "java(book.getAvailableCopies() > 0)")
    BookResponse toResponse(Book book);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(BookRequest request, @MappingTarget Book book);
}
