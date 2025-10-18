package com.nathanroos.library.customersubdomain.BusinessLayer;


import com.nathanroos.library.customersubdomain.utils.Exceptions.InvalidInputException;
import com.nathanroos.library.customersubdomain.utils.Exceptions.NotFoundException;
import com.nathanroos.library.customersubdomain.DataAccessLayer.LibraryAccount;
import com.nathanroos.library.customersubdomain.DataAccessLayer.LibraryAccountIdentifier;
import com.nathanroos.library.customersubdomain.DataAccessLayer.LibraryAccountRepository;
import com.nathanroos.library.customersubdomain.MappingLayer.LibraryAccountRequestMapper;
import com.nathanroos.library.customersubdomain.MappingLayer.LibraryAccountResponseMapper;
import com.nathanroos.library.customersubdomain.PresentationLayer.LibraryAccountRequestModel;
import com.nathanroos.library.customersubdomain.PresentationLayer.LibraryAccountResponseModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LibraryAccountServiceImpl implements LibraryAccountService {

    private final LibraryAccountRepository libraryAccountRepository;

    private final LibraryAccountResponseMapper libraryAccountResponseMapper;

    private final LibraryAccountRequestMapper libraryAccountRequestMapper;

    public LibraryAccountServiceImpl(LibraryAccountRepository libraryAccountRepository, LibraryAccountResponseMapper libraryAccountResponseMapper, LibraryAccountRequestMapper libraryAccountRequestMapper) {
        this.libraryAccountRepository = libraryAccountRepository;
        this.libraryAccountResponseMapper = libraryAccountResponseMapper;
        this.libraryAccountRequestMapper = libraryAccountRequestMapper;
    }


    @Override
    public List<LibraryAccountResponseModel> getAccounts() {
        List<LibraryAccount> accounts = libraryAccountRepository.findAll();
        return libraryAccountResponseMapper.entityListToResponseModelList(accounts);
    }

    @Override
    public LibraryAccountResponseModel getAccountByAccountId(String accountId) {
        LibraryAccount account = getAccountObjectById(accountId);

        return libraryAccountResponseMapper.entityToResponseModel(account);
    }

    @Override
    public LibraryAccountResponseModel addAccount(LibraryAccountRequestModel account) {

        LibraryAccount account1 = libraryAccountRequestMapper.requestModelToEntity(account, new LibraryAccountIdentifier());

        LibraryAccount createdAccount = libraryAccountRepository.save(account1);

        validateAccountRequestModel(account);

        return libraryAccountResponseMapper.entityToResponseModel(createdAccount);
    }

    @Override
    public LibraryAccountResponseModel updateAccount(LibraryAccountRequestModel UpdateAccount, String accountId)
    {
        LibraryAccount account = getAccountObjectById(accountId);

        validateAccountRequestModel(UpdateAccount);



        account.setFirstname(UpdateAccount.getFirstname());
        account.setLastname(UpdateAccount.getLastname());
        account.setPhoneNumber(UpdateAccount.getPhoneNumber());
        account.setEmail(UpdateAccount.getEmail());

        LibraryAccount updatedAccount = libraryAccountRepository.save(account);
        return libraryAccountResponseMapper.entityToResponseModel(updatedAccount);
    }

    @Override
    public void removeAccount(String accountId) {
        LibraryAccount account = getAccountObjectById(accountId);

        libraryAccountRepository.delete(account);
    }

    private void validateAccountRequestModel(LibraryAccountRequestModel model) {
        if (model.getFirstname() == null || model.getFirstname().isBlank()) {
            throw new InvalidInputException("Invalid firstName: " + model.getFirstname());
        }
        if (model.getLastname() == null || model.getLastname().isBlank()) {
            throw new InvalidInputException("Invalid lastName: " + model.getLastname());
        }
        if (model.getEmail() == null || !model.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new InvalidInputException("Invalid email: " + model.getEmail());
        }
    }

    private LibraryAccount getAccountObjectById(String accountId) {
        try {
            UUID.fromString(accountId);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid accountId: " + accountId);
        }

        LibraryAccount account = this.libraryAccountRepository.findLibraryAccountByLibraryAccountIdentifier_AccountId(accountId);

        if (account == null) {
            throw new NotFoundException("Unknown accountId: " + accountId);
        }

        return account;
    }
}
