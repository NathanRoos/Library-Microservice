package com.nathanroos.library.apigateway.presentationlayer.librarian;


import com.nathanroos.library.apigateway.domainclientlayer.librarian.LibrarianPhoneNumber;
import com.nathanroos.library.apigateway.domainclientlayer.librarian.Position;
import com.nathanroos.library.apigateway.domainclientlayer.librarian.ProvinceEnum;
import com.nathanroos.library.apigateway.domainclientlayer.librarian.WorkerAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LibrarianResponseModel extends RepresentationModel<LibrarianResponseModel> {

    String librarianId;
    String firstname;
    String lastname;
    String email;
//    List<Loan> booksLoaned;
//    WorkerAddress employeeAddress;


    WorkerAddress libraryWorkerAddress;


    Position position;
    LibrarianPhoneNumber librarianPhoneNumber;


}
