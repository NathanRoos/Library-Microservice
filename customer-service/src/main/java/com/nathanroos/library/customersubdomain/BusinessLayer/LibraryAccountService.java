package com.nathanroos.library.customersubdomain.BusinessLayer;

import com.nathanroos.library.customersubdomain.PresentationLayer.LibraryAccountRequestModel;
import com.nathanroos.library.customersubdomain.PresentationLayer.LibraryAccountResponseModel;

import java.util.List;

public interface LibraryAccountService {

    List<LibraryAccountResponseModel> getAccounts();
    LibraryAccountResponseModel getAccountByAccountId(String accountId);
    LibraryAccountResponseModel addAccount(LibraryAccountRequestModel account);
    LibraryAccountResponseModel updateAccount(LibraryAccountRequestModel account, String accountId);
    void removeAccount(String accountId);

}
