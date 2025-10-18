package com.nathanroos.library.apigateway.presentationlayer.library;

import com.nathanroos.library.apigateway.domainclientlayer.library.GenreEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BookRequestModel {

    String firstname;
    String lastname;
    GenreEnum genre;
    String title;
    String author;
    Integer copiesAvailable;

}
