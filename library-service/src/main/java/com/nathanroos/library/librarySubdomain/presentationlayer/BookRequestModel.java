package com.nathanroos.library.librarySubdomain.presentationlayer;

import com.nathanroos.library.librarySubdomain.dataaccesslayer.GenreEnum;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

@Value
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BookRequestModel extends RepresentationModel<BookResponseModel> {
    String firstname;
    String lastname;
    GenreEnum genre;
    String title;
    String author;
    Integer copiesAvailable;
    String imageUrl;
}
