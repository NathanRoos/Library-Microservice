package com.nathanroos.library.libraryworkerssubdomain.PresentationLayer;


import com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer.LibrarianPhoneNumber;
import com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer.Position;
import com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer.ProvinceEnum;
import com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer.WorkerAddress;
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
//    WorkerAddress employeeAddress;

    WorkerAddress libraryWorkerAddress;

    Position position;
    LibrarianPhoneNumber librarianPhoneNumber;


}
