package com.nathanroos.library.apigateway.businesslayer.loan;

import com.nathanroos.library.apigateway.ExceptionsHandling.NotFoundException;
import com.nathanroos.library.apigateway.domainclientlayer.loan.LoanServiceClient;
import com.nathanroos.library.apigateway.presentationlayer.loan.LoanController;
import com.nathanroos.library.apigateway.presentationlayer.loan.LoanRequestModel;
import com.nathanroos.library.apigateway.presentationlayer.loan.LoanResponseModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class LoanServiceImpl implements LoanService {

    private final LoanServiceClient loanServiceClient;


    public LoanServiceImpl(LoanServiceClient loanServiceClient) {
        this.loanServiceClient = loanServiceClient;
    }

    @Override
    public List<LoanResponseModel> getAllLoansByAccountId(String accountId) {
        return this.loanServiceClient.getLoans(accountId).stream().map(this::addLinks).toList();

    }

    @Override
    public LoanResponseModel getLoanByLoanId(String accountId, String loanId) {
        LoanResponseModel loan = loanServiceClient.getLoanByLoanId(accountId, loanId);
        if (loan == null) {
            throw new NotFoundException("Loan with ID " + loanId + " not found for account " + accountId);
        }
        return addLinks(loan);
    }


    @Override
    public LoanResponseModel addLoan(LoanRequestModel loan, String accountId) {
         LoanResponseModel loanResponseModel  =   loanServiceClient.addLoan(accountId, loan);
        System.out.println(loanResponseModel.getCustomer_firstname());

        return this.addLinks(loanResponseModel);
    }

    @Override
    public LoanResponseModel updateLoan(String accountId, LoanRequestModel loan, String loanId) {
        return this.addLinks(loanServiceClient.updateLoan( accountId,loan, loanId));
    }

    @Override
    public void removeLoan(String accountId, String loanId) {
        loanServiceClient.deleteLoan(accountId, loanId);
    }

    private LoanResponseModel addLinks(LoanResponseModel loan) {
        Link selfLink = linkTo(methodOn(LoanController.class)
                .getLoanByLoanId(loan.getAccountId(), loan.getLoanId()))
                .withSelfRel();
        loan.add(selfLink);

        Link allLoansLink = linkTo(methodOn(LoanController.class)
                .getAllLoansByAccountId(loan.getLoanId()))
                .withRel("loans");
        loan.add(allLoansLink);

        return loan;
    }

}
