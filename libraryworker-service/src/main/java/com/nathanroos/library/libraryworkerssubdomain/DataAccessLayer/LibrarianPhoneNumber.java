package com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class LibrarianPhoneNumber {

    @Column(name = "phonenumber")
    private String phoneNumber;

    @Column(name = "phonetype")
    @Enumerated(EnumType.STRING)
    private PhoneTypeEnum type;

}
