package com.nathanroos.library.apigateway.businesslayer.loan;

import com.nathanroos.library.apigateway.presentationlayer.loan.LoanRequestModel;
import com.nathanroos.library.apigateway.presentationlayer.loan.LoanResponseModel;

import java.util.List;

public interface LoanService {

    LoanResponseModel getLoanByLoanId(String accountId, String loanId);
    List<LoanResponseModel> getAllLoansByAccountId(String accountId);
    LoanResponseModel addLoan(LoanRequestModel loan, String accountId);
    LoanResponseModel updateLoan(String accountId, LoanRequestModel loan, String loanId);
    void removeLoan(String accountId, String loanId);


}
