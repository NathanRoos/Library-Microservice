package com.nathanroos.library.LoanSubdomain.BusinessLayer;

import com.nathanroos.library.LoanSubdomain.PresentationLayer.LoanRequestModel;
import com.nathanroos.library.LoanSubdomain.PresentationLayer.LoanResponseModel;

import java.util.List;

public interface LoanService {

//    List<LoanResponseModel> getLoans();
    LoanResponseModel getLoanByLoanId(String accountId, String loanId);
    List<LoanResponseModel> getAllLoansByAccountId(String accountId);
    LoanResponseModel addLoan(LoanRequestModel loan, String accountId);
    LoanResponseModel updateLoan(String accountId, LoanRequestModel loan, String loanId);
    void removeLoan(String accountId, String loanId);

}
