package com.nathanroos.library.librarySubdomain.mappinglayer;


import com.nathanroos.library.librarySubdomain.dataaccesslayer.Book;
import com.nathanroos.library.librarySubdomain.presentationlayer.BookController;
import com.nathanroos.library.librarySubdomain.presentationlayer.BookResponseModel;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Mapper(componentModel = "spring")
public interface BookResponseMapper {

    @Mapping(source = "bookIdentifier.bookId", target = "bookId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "genre", target = "genre")
    @Mapping(source = "author", target = "author")
    @Mapping(source = "copiesAvailable", target = "copiesAvailable")
    @Mapping(source = "firstname", target = "firstname")
    @Mapping(source = "lastname", target = "lastname")
    BookResponseModel entityToResponseModel(Book book);

    List<BookResponseModel> entityListToResponseModelList(List<Book> Books);

//    @AfterMapping
//    default void addLinks(@MappingTarget BookResponseModel responseModel) {
//        Link selfLink = linkTo(methodOn(BookController.class)
//                .getBookByBookId(responseModel.getBookId())).withSelfRel();
//        responseModel.add(selfLink);
//    }

}
