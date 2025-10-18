package com.nathanroos.library.apigateway.presentationlayer.loan;

import com.nathanroos.library.apigateway.domainclientlayer.loan.LoanStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
