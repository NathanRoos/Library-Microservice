package com.nathanroos.library.LoanSubdomain.PresentationLayer;

import com.nathanroos.library.LoanSubdomain.BusinessLayer.LoanService;
import com.nathanroos.library.LoanSubdomain.utils.Exceptions.InvalidInputException;
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

    @GetMapping()
    public ResponseEntity<List<LoanResponseModel>> getAllLoansByAccountId(@PathVariable String accountId) {
        List<LoanResponseModel> loanResponseModels = loanService.getAllLoansByAccountId(accountId);
        return ResponseEntity.ok().body(loanResponseModels);
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponseModel> getLoanByLoanId(@PathVariable String accountId, @PathVariable String loanId) {
        if(loanId.length() != UUID_LENGTH){
            throw new InvalidInputException("The following Id is not the correct length: " + loanId);
        }

        return ResponseEntity.ok().body(loanService.getLoanByLoanId(accountId, loanId));
    }

    @PostMapping
    public ResponseEntity<LoanResponseModel> AddLoan( @RequestBody LoanRequestModel loanRequestModel, @PathVariable String accountId) {
        if (!accountId.equals(loanRequestModel.getAccountId())) {
            throw new InvalidInputException("AccountId in path and body must match.");
        }
        return new ResponseEntity<>(loanService.addLoan(loanRequestModel, accountId), HttpStatus.CREATED);
    }

    @PutMapping("/{loanId}")
    public ResponseEntity<LoanResponseModel> updateLoan(@PathVariable String accountId, @PathVariable String loanId, @RequestBody LoanRequestModel loanRequestModel)
    {
        return new ResponseEntity<>(loanService.updateLoan(accountId, loanRequestModel, loanId), HttpStatus.OK);
    }

    @DeleteMapping("/{loanId}")
    public ResponseEntity<Void> deleteLoan(@PathVariable String accountId, @PathVariable String loanId)
    {
        loanService.removeLoan(accountId, loanId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
