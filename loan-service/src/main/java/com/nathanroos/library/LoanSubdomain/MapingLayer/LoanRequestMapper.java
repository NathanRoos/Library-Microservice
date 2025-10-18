package com.nathanroos.library.LoanSubdomain.MapingLayer;

import com.nathanroos.library.LoanSubdomain.DataAccessLayer.Loan;
import com.nathanroos.library.LoanSubdomain.DataAccessLayer.LoanIdentifier;
import com.nathanroos.library.LoanSubdomain.PresentationLayer.LoanRequestModel;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.Customer.CustomerModel;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.Library.LibraryModel;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.LibraryWorker.LibraryWorkerModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface LoanRequestMapper {

    @Mappings({
            @Mapping(expression = "java(loanIdentifier)", target = "loanIdentifier"),
            @Mapping(expression = "java(customerModel)", target = "libraryAccountIdentifier"),
            @Mapping(expression = "java(libraryWorkerModel)", target = "librarianIdentifier"),
            @Mapping(expression = "java(libraryModel)", target = "bookIdentifier"),

            @Mapping(source = "loanRequestModel.loanStatus", target = "loanStatus"),
            @Mapping(source = "loanRequestModel.loanDate", target = "loanDate"),
            @Mapping(source = "loanRequestModel.dueDate", target = "dueDate")
    })
    Loan requestModelToEntity(LoanRequestModel loanRequestModel, LoanIdentifier loanIdentifier,
                              CustomerModel customerModel, LibraryWorkerModel libraryWorkerModel,
                              LibraryModel libraryModel);


}
