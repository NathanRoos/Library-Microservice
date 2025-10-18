package com.nathanroos.library.librarySubdomain.mappinglayer;


import com.nathanroos.library.librarySubdomain.dataaccesslayer.Book;
import com.nathanroos.library.librarySubdomain.dataaccesslayer.BookIdentifier;
import com.nathanroos.library.librarySubdomain.presentationlayer.BookRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface BookRequestMapper {

    @Mappings({
            @Mapping(expression = "java(bookIdentifier)", target = "bookIdentifier"),
            @Mapping(source = "bookRequestModel.title", target = "title"),
            @Mapping(source = "bookRequestModel.author", target = "author"),
            @Mapping(source = "bookRequestModel.copiesAvailable", target = "copiesAvailable"),
            @Mapping(source = "bookRequestModel.genre", target = "genre"),
            @Mapping(source = "bookRequestModel.firstname", target = "firstname"),
            @Mapping(source = "bookRequestModel.lastname", target = "lastname")


    })
    Book requestModelToEntity(BookRequestModel bookRequestModel, BookIdentifier bookIdentifier);

}
