package com.nathanroos.library.apigateway.domainclientlayer.librarian;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibrarianPhoneNumber {

    @Column(name = "phonenumber")
    private String phoneNumber;

    @Column(name = "phonetype")
    @Enumerated(EnumType.STRING)
    private PhoneTypeEnum type;

}
