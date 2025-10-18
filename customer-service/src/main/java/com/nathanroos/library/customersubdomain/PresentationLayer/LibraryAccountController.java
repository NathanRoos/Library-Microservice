package com.nathanroos.library.customersubdomain.PresentationLayer;

import com.nathanroos.library.customersubdomain.utils.Exceptions.InvalidInputException;
import com.nathanroos.library.customersubdomain.BusinessLayer.LibraryAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/accounts")
public class LibraryAccountController {

    private final LibraryAccountService libraryAccountService;

    private static final int UUID_LENGTH = 36;

    @Autowired
    public LibraryAccountController(LibraryAccountService libraryAccountService) {
        this.libraryAccountService = libraryAccountService;
    }

   @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LibraryAccountResponseModel>> getAccounts() {
        return ResponseEntity.ok().body(libraryAccountService.getAccounts());
    }

    @GetMapping(value = "{accountId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LibraryAccountResponseModel> getAccountByAccountId(@PathVariable String accountId) {
        if (accountId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid accountId provided: " + accountId);
        }
        return ResponseEntity.ok().body(libraryAccountService.getAccountByAccountId(accountId));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LibraryAccountResponseModel> addAccount(@RequestBody LibraryAccountRequestModel libraryAccountRequestModel) {
        return new ResponseEntity<>(libraryAccountService.addAccount(libraryAccountRequestModel), HttpStatus.CREATED);
    }

    @PutMapping(value = "{accountId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LibraryAccountResponseModel> updateAccount(@PathVariable String accountId, @RequestBody LibraryAccountRequestModel libraryAccountRequestModel) {
        return new ResponseEntity<>(libraryAccountService.updateAccount(libraryAccountRequestModel, accountId), HttpStatus.OK);
    }

    @DeleteMapping(value = "{accountId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LibraryAccountResponseModel> deleteAccount(@PathVariable String accountId) {
        libraryAccountService.removeAccount(accountId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
