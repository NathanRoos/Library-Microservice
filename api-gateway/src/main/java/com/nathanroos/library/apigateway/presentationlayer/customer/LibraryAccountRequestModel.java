package com.nathanroos.library.apigateway.presentationlayer.customer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LibraryAccountRequestModel {

    String firstname;
    String lastname;
    String phoneNumber;
    String email;

}
