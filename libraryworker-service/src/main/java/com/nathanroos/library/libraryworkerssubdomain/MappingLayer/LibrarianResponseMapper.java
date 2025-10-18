package com.nathanroos.library.libraryworkerssubdomain.MappingLayer;


import com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer.Librarian;
import com.nathanroos.library.libraryworkerssubdomain.PresentationLayer.LibrarianController;
import com.nathanroos.library.libraryworkerssubdomain.PresentationLayer.LibrarianResponseModel;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Mapper(componentModel = "spring")
public interface LibrarianResponseMapper {

    @Mapping(source = "librarianIdentifier.librarianId", target = "librarianId")
    @Mapping(source = "firstname", target = "firstname")
    @Mapping(source = "lastname", target = "lastname")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "libraryWorkerAddress",target = "libraryWorkerAddress")
    @Mapping(source = "position",target = "position")
    @Mapping(source = "librarianPhoneNumber",target = "librarianPhoneNumber")
    LibrarianResponseModel entityToResponseModel(Librarian librarian);

    List<LibrarianResponseModel> entityListToResponseModelList(List<Librarian> workers);

    @AfterMapping
    default void addLinks(@MappingTarget LibrarianResponseModel responseModel) {
        Link selfLink = linkTo(methodOn(LibrarianController.class)
                .getLibrarianByLibrarianId(responseModel.getLibrarianId())).withSelfRel();
        responseModel.add(selfLink);
    }

}
