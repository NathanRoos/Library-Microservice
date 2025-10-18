package com.nathanroos.library.customersubdomain.PresentationLayer;

import lombok.*;


@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LibraryAccountRequestModel {

    String firstname;
    String lastname;
    String phoneNumber;
    String email;

}
