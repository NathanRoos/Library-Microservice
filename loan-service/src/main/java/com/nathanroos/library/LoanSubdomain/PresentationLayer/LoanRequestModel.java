package com.nathanroos.library.LoanSubdomain.PresentationLayer;

import com.nathanroos.library.LoanSubdomain.DataAccessLayer.LoanStatusEnum;
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
    String accountId;
    String librarianId;
    String bookId;

}
