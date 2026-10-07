package com.example.book.repository;

import com.example.book.model.entity.Book;
import com.example.book.model.enums.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    @Query("""
            select b from Book b
            where (:author is null or lower(b.author) like lower(concat('%', cast(:author as string), '%')))
              and (:genre is null or b.genre = :genre)
            """)
    Page<Book> search(@Param("author") String author, @Param("genre") Genre genre, Pageable pageable);
}
