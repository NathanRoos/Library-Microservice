package com.nathanroos.library.customersubdomain.MappingLayer;


import com.nathanroos.library.customersubdomain.DataAccessLayer.LibraryAccount;
import com.nathanroos.library.customersubdomain.PresentationLayer.LibraryAccountController;
import com.nathanroos.library.customersubdomain.PresentationLayer.LibraryAccountResponseModel;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Mapper(componentModel = "spring")
public interface LibraryAccountResponseMapper {

    @Mapping(source = "libraryAccountIdentifier.accountId", target = "accountId")
    @Mapping(source = "firstname", target = "firstname")
    @Mapping(source = "lastname", target = "lastname")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(source = "email", target = "email")
    LibraryAccountResponseModel entityToResponseModel(LibraryAccount account);

    List<LibraryAccountResponseModel> entityListToResponseModelList(List<LibraryAccount> accounts);

    @AfterMapping
    default void addLinks(@MappingTarget LibraryAccountResponseModel responseModel) {
        Link selfLink = linkTo(methodOn(LibraryAccountController.class)
                .getAccountByAccountId(responseModel.getAccountId())).withSelfRel();
        responseModel.add(selfLink);
    }

}
