package com.nathanroos.library.librarySubdomain.presentationlayer;

import com.nathanroos.library.librarySubdomain.dataaccesslayer.GenreEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookResponseModel extends RepresentationModel<BookResponseModel> {

    String bookId;
    String firstname;
    String lastname;
    GenreEnum genre;
    String title;
    String author;
    Integer copiesAvailable;
    String imageUrl;
}
