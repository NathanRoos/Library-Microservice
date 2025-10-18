package com.nathanroos.library.libraryworkerssubdomain.MappingLayer;


import com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer.Librarian;
import com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer.LibrarianIdentifier;
import com.nathanroos.library.libraryworkerssubdomain.PresentationLayer.LibrarianRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface LibrarianRequestMapper {

    @Mappings({
            @Mapping(expression = "java(librarianIdentifier)", target = "librarianIdentifier"),
            @Mapping(source = "librarianRequestModel.firstname", target = "firstname"),
            @Mapping(source = "librarianRequestModel.lastname", target = "lastname"),
            @Mapping(source = "librarianRequestModel.email", target = "email"),
            @Mapping(source = "librarianRequestModel.libraryWorkerAddress", target = "libraryWorkerAddress"),
            @Mapping(source = "librarianRequestModel.position", target = "position"),
            @Mapping(source = "librarianRequestModel.librarianPhoneNumber", target = "librarianPhoneNumber"),

    })
    Librarian requestModelToEntity(LibrarianRequestModel librarianRequestModel, LibrarianIdentifier librarianIdentifier);


}
