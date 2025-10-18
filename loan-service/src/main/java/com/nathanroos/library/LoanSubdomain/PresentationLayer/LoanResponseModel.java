package com.nathanroos.library.LoanSubdomain.PresentationLayer;

import com.nathanroos.library.LoanSubdomain.DataAccessLayer.LoanStatusEnum;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanResponseModel extends RepresentationModel<LoanResponseModel> {

    String loanId;
    String accountId;
    String librarianId;
    String bookId;
    String customer_firstname;
    String customer_lastname;
    String librarian_firstname;
    String librarian_lastname;
    String title;
    String author;
    LoanStatusEnum loanStatus;
    Date loanDate;
    Date dueDate;


}
