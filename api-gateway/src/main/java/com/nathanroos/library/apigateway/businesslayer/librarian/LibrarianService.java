package com.nathanroos.library.apigateway.businesslayer.librarian;



import com.nathanroos.library.apigateway.presentationlayer.librarian.LibrarianRequestModel;
import com.nathanroos.library.apigateway.presentationlayer.librarian.LibrarianResponseModel;
import org.springframework.stereotype.Service;

import java.util.List;


public interface LibrarianService {

    public LibrarianResponseModel createLibrarian(LibrarianRequestModel librarianRequestModel);

    public LibrarianResponseModel getLibrarian(String librarianId);

    public List<LibrarianResponseModel> getworkers();

    public LibrarianResponseModel updateLibrarian(String librarianId, LibrarianRequestModel librarianRequestModel);

    public void deleteLibrarian(String librarianId);

}
