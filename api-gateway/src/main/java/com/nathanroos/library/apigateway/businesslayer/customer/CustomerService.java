package com.nathanroos.library.apigateway.businesslayer.customer;

import com.nathanroos.library.apigateway.presentationlayer.customer.LibraryAccountRequestModel;
import com.nathanroos.library.apigateway.presentationlayer.customer.LibraryAccountResponseModel;
import org.springframework.stereotype.Service;

import java.util.List;


public interface CustomerService {

    List<LibraryAccountResponseModel> getAllAccounts();
    LibraryAccountResponseModel getAccountByAccountId(String customerId);
    LibraryAccountResponseModel addAccount(LibraryAccountRequestModel account);
    LibraryAccountResponseModel updateAccount(LibraryAccountRequestModel account, String accountId);
    void removeAccount(String accountId);

}
