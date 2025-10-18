package com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.UUID;

@Embeddable
@Getter
public class LibrarianIdentifier {

    @Column(name = "librarian_id")
    private String librarianId;

    public LibrarianIdentifier() {
        this.librarianId = UUID.randomUUID().toString();
    }

    public LibrarianIdentifier(String customerId) {
        this.librarianId = librarianId;
    }

}
