package com.nathanroos.library.customersubdomain.DataAccessLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.UUID;

@Getter
@Embeddable
public class LibraryAccountIdentifier {

    @Column(name = "customer_id")
    private String accountId;

    public LibraryAccountIdentifier() {
        this.accountId = UUID.randomUUID().toString();
    }

    public LibraryAccountIdentifier(String accountId) {
        this.accountId = accountId;
    }

}
