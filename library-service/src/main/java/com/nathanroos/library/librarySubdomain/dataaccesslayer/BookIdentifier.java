package com.nathanroos.library.librarySubdomain.dataaccesslayer;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.UUID;
@Embeddable
@Getter
public class BookIdentifier {

    @Column(name = "book_id")
    private String bookId;

    public BookIdentifier() {
        this.bookId = UUID.randomUUID().toString();
    }

    public BookIdentifier(String bookId) {
        this.bookId = bookId;
    }

}
