package com.nathanroos.library.customersubdomain.MappingLayer;


import com.nathanroos.library.customersubdomain.DataAccessLayer.LibraryAccount;
import com.nathanroos.library.customersubdomain.DataAccessLayer.LibraryAccountIdentifier;
import com.nathanroos.library.customersubdomain.PresentationLayer.LibraryAccountRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface LibraryAccountRequestMapper {

    @Mappings({
            @Mapping(expression = "java(libraryAccountIdentifier)", target = "libraryAccountIdentifier"),
            @Mapping(source = "libraryAccountRequestModel.firstname", target = "firstname"),
            @Mapping(source = "libraryAccountRequestModel.lastname", target = "lastname"),
            @Mapping(source = "libraryAccountRequestModel.email", target = "email"),
            @Mapping(source = "libraryAccountRequestModel.phoneNumber", target = "phoneNumber"),
    })
    LibraryAccount requestModelToEntity(LibraryAccountRequestModel libraryAccountRequestModel, LibraryAccountIdentifier libraryAccountIdentifier); //LibraryAccount
}
