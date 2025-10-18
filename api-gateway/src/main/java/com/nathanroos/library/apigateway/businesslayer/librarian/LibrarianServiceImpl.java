package com.nathanroos.library.apigateway.businesslayer.librarian;




import com.nathanroos.library.apigateway.ExceptionsHandling.NotFoundException;
import com.nathanroos.library.apigateway.domainclientlayer.librarian.LibrarianServiceClient;
import com.nathanroos.library.apigateway.presentationlayer.customer.LibraryAccountController;
import com.nathanroos.library.apigateway.presentationlayer.customer.LibraryAccountResponseModel;
import com.nathanroos.library.apigateway.presentationlayer.librarian.LibrarianController;
import com.nathanroos.library.apigateway.presentationlayer.librarian.LibrarianRequestModel;
import com.nathanroos.library.apigateway.presentationlayer.librarian.LibrarianResponseModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class LibrarianServiceImpl implements LibrarianService {
    private final LibrarianServiceClient librarianServiceClient;



    public LibrarianServiceImpl(LibrarianServiceClient librarianServiceClient) {
        this.librarianServiceClient = librarianServiceClient;
    }

    @Override
    public LibrarianResponseModel createLibrarian(LibrarianRequestModel librarianRequestModel) {
        return addLinks(librarianServiceClient.createLibrarian(librarianRequestModel));
    }

    @Override
    public LibrarianResponseModel getLibrarian(String librarianId) {
        LibrarianResponseModel account = librarianServiceClient.getLibrarian(librarianId);
        if (account == null) {
            throw new NotFoundException("Librarian with ID " + librarianId + " not found");
        }
        return addLinks(account);
    }


    @Override
    public List<LibrarianResponseModel> getworkers( ) {
        return librarianServiceClient.getWorkers().stream().map(this::addLinks).toList();
    }

    @Override
    public LibrarianResponseModel updateLibrarian(String librarianId, LibrarianRequestModel librarianRequestModel) {
        return addLinks(librarianServiceClient.updateLibrarian(librarianId, librarianRequestModel));
    }

    @Override
    public void deleteLibrarian(String librarianId) {
        librarianServiceClient.deleteLibrarian(librarianId);
    }

    private LibrarianResponseModel addLinks(LibrarianResponseModel account) {
        Link selfLink = linkTo(methodOn(LibrarianController.class)
                .getLibrarian(account.getLibrarianId()))
                .withSelfRel();
        account.add(selfLink);

        Link allAccountsLink = linkTo(methodOn(LibrarianController.class)
                .getWorkers())
                .withRel("librarians");
        account.add(allAccountsLink);

        return account;
    }
}
