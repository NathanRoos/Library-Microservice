package com.nathanroos.library.LoanSubdomain.DataAccessLayer;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends MongoRepository<Loan, String> {

    Loan findByLoanIdentifier_LoanId(String loanId);

    List<Loan> findAllByLoanIdentifier_LoanId(String loanId);

    List<Loan> findAllByLibraryAccountIdentifier_AccountId(String accountId);
    Loan findByLibraryAccountIdentifier_AccountId(String accountId);
    Loan findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId(String accountId, String loanId);
}
