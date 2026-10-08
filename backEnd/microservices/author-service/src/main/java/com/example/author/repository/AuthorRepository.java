package com.example.author.repository;

import com.example.author.model.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    @Query("""
            select a from Author a
            where (:lastName is null or lower(a.lastName) like lower(concat('%', cast(:lastName as string), '%')))
              and (:nationality is null or lower(a.nationality) = lower(cast(:nationality as string)))
            """)
    Page<Author> search(@Param("lastName") String lastName,
                        @Param("nationality") String nationality,
                        Pageable pageable);
}
