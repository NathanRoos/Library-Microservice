package com.nathanroos.library.apigateway.presentationlayer.library;

import com.nathanroos.library.apigateway.domainclientlayer.library.GenreEnum;
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
