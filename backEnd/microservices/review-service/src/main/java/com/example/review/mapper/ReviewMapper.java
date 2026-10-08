package com.example.review.mapper;

import com.example.review.dto.ReviewRequest;
import com.example.review.dto.ReviewResponse;
import com.example.review.model.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface ReviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Review toEntity(ReviewRequest request);

    ReviewResponse toResponse(Review review);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(ReviewRequest request, @MappingTarget Review review);
}
