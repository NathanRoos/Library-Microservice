package com.nathanroos.library.librarySubdomain.dataaccesslayer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Integer> {

    Book findAllByBookIdentifier_BookId(String bookId);
}
