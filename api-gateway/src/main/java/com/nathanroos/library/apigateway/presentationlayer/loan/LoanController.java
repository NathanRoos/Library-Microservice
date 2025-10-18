package com.nathanroos.library.apigateway.presentationlayer.loan;

import com.nathanroos.library.apigateway.ExceptionsHandling.InvalidInputException;
import com.nathanroos.library.apigateway.businesslayer.loan.LoanService;
import com.nathanroos.library.apigateway.presentationlayer.loan.LoanRequestModel;
import com.nathanroos.library.apigateway.presentationlayer.loan.LoanResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/accounts/{accountId}/loans")
public class LoanController {


    private final LoanService loanService;

    private static final int UUID_LENGTH = 36;

    @Autowired
    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping(
            produces = "application/json"
    )
    public ResponseEntity<List<LoanResponseModel>> getAllLoansByAccountId(@PathVariable String accountId) {
        List<LoanResponseModel> loanResponseModels = loanService.getAllLoansByAccountId(accountId);
        return ResponseEntity.status(HttpStatus.OK).body(loanResponseModels);
    }

    @GetMapping(
            value = "/{loanId}",
            produces = "application/json"
    )
    public ResponseEntity<LoanResponseModel> getLoanByLoanId(@PathVariable String accountId, @PathVariable String loanId) {
        if(loanId.length() != UUID_LENGTH){
            throw new InvalidInputException("The following Id is not the correct length: " + loanId);
        }

        return ResponseEntity.status(HttpStatus.OK).body(loanService.getLoanByLoanId(accountId, loanId));
    }

    @PostMapping(
            consumes = {"application/json"},
            produces = {"application/json"}
    )
    public ResponseEntity<LoanResponseModel> AddLoan(@RequestBody LoanRequestModel loanRequestModel, @PathVariable String accountId) {
        if (!accountId.equals(loanRequestModel.getAccountId())) {
            throw new InvalidInputException("AccountId in path and body must match.");
        }
        LoanResponseModel loanResponseModel = loanService.addLoan(loanRequestModel, accountId);
        return ResponseEntity.status(HttpStatus.CREATED).body(loanResponseModel);
    }

    @PutMapping(
            value = "/{loanId}",
            consumes = {"application/json"},
            produces = {"application/json"}
    )
    public ResponseEntity<LoanResponseModel> updateLoan(@PathVariable String accountId, @PathVariable String loanId, @RequestBody LoanRequestModel loanRequestModel)
    {
        return ResponseEntity.status(HttpStatus.OK).body(loanService.updateLoan(accountId, loanRequestModel, loanId));
    }

    @DeleteMapping(
            value = "/{loanId}",
            produces = {"application/json"}
    )
    public ResponseEntity<Void> deleteLoan(@PathVariable String accountId, @PathVariable String loanId)
    {
        loanService.removeLoan(accountId, loanId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
