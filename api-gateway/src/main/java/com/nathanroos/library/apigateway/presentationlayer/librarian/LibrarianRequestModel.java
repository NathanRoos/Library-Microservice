package com.nathanroos.library.apigateway.presentationlayer.librarian;


import com.nathanroos.library.apigateway.domainclientlayer.librarian.LibrarianPhoneNumber;
import com.nathanroos.library.apigateway.domainclientlayer.librarian.Position;
import com.nathanroos.library.apigateway.domainclientlayer.librarian.WorkerAddress;
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
//    List<Loan> booksLoaned;
    WorkerAddress libraryWorkerAddress;
    Position position;
    LibrarianPhoneNumber librarianPhoneNumber;

}
