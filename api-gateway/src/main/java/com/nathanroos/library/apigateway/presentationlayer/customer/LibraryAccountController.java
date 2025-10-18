package com.nathanroos.library.apigateway.presentationlayer.customer;


import com.nathanroos.library.apigateway.businesslayer.customer.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/accounts")
public class LibraryAccountController {

    private final CustomerService customerService;

    public LibraryAccountController(CustomerService customerService) {
        this.customerService = customerService;
    }


    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<LibraryAccountResponseModel>> getAllAccounts() {
        log.debug("1. Request Received in API-Gateway Customers Controller: getAccount");
        return ResponseEntity.status(HttpStatus.OK).body(customerService.getAllAccounts());
    }

    @PutMapping(
            value = "{accountId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LibraryAccountResponseModel> updateAccount(@PathVariable String accountId, @RequestBody LibraryAccountRequestModel account) {
        log.debug("1. Request Received in API-Gateway Librarians Controller: updateAccount");
        return ResponseEntity.status(HttpStatus.OK).body(customerService.updateAccount(account, accountId));
    }

    @DeleteMapping(
            value = "{accountId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LibraryAccountResponseModel> deleteAccount(@PathVariable String accountId) {
        log.debug("1. Request Received in API-Gateway Librarians Controller: deleteAccount");
        customerService.removeAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LibraryAccountResponseModel> createAccount(@RequestBody LibraryAccountRequestModel account) {
        log.debug("1. Request Received in API-Gateway Librarians Controller: createAccount");
        LibraryAccountResponseModel account1 = customerService.addAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(account1);
    }

    @GetMapping(
            value = "{accountId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LibraryAccountResponseModel> getAccountByAccountId(@PathVariable String accountId) {
        log.debug("1. Request Received in API-Gateway Customers Controller: getAccountByAccountId");
        return ResponseEntity.status(HttpStatus.OK).body(customerService.getAccountByAccountId(accountId));
    }
}
