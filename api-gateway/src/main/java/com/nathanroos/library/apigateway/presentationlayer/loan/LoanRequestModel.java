package com.nathanroos.library.apigateway.presentationlayer.loan;

import com.nathanroos.library.apigateway.domainclientlayer.loan.LoanStatusEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Date;


@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoanRequestModel {


    LoanStatusEnum loanStatus;
    Date loanDate;
    Date dueDate;
    String librarianId;
    String bookId;
    String accountId;

}
