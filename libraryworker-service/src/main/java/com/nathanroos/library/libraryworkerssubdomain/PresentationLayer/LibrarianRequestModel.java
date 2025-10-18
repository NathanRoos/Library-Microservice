package com.nathanroos.library.libraryworkerssubdomain.PresentationLayer;

import com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer.LibrarianPhoneNumber;
import com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer.Position;
import com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer.WorkerAddress;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LibrarianRequestModel {


    String firstname;
    String lastname;
    String email;
    WorkerAddress libraryWorkerAddress;
    Position position;
    LibrarianPhoneNumber librarianPhoneNumber;

}
