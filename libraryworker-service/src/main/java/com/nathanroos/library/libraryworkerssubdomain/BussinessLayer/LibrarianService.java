package com.nathanroos.library.libraryworkerssubdomain.BussinessLayer;


import com.nathanroos.library.libraryworkerssubdomain.PresentationLayer.LibrarianRequestModel;
import com.nathanroos.library.libraryworkerssubdomain.PresentationLayer.LibrarianResponseModel;

import java.util.List;

public interface LibrarianService {

    List<LibrarianResponseModel> getWorkers();
    LibrarianResponseModel getLibrarianByLibrarianId(String librarianId);
    LibrarianResponseModel addLibrarian(LibrarianRequestModel librarian);
    LibrarianResponseModel updateLibrarian(LibrarianRequestModel librarian, String librarianId);
    void removeLibrarian(String librarianId);

}
