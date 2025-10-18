package com.nathanroos.library.apigateway.businesslayer.customer;

import com.nathanroos.library.apigateway.ExceptionsHandling.NotFoundException;
import com.nathanroos.library.apigateway.domainclientlayer.customer.CustomerServiceClient;
import com.nathanroos.library.apigateway.presentationlayer.customer.LibraryAccountController;
import com.nathanroos.library.apigateway.presentationlayer.customer.LibraryAccountRequestModel;
import com.nathanroos.library.apigateway.presentationlayer.customer.LibraryAccountResponseModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerServiceClient customerServiceClient;

    public CustomerServiceImpl(CustomerServiceClient customerServiceClient) {
        this.customerServiceClient = customerServiceClient;
    }


    @Override
    public List<LibraryAccountResponseModel> getAllAccounts() {
        return this.customerServiceClient.getAccounts().stream().map(this::addLinks).toList();
    }

    @Override
    public LibraryAccountResponseModel getAccountByAccountId(String customerId) {
        LibraryAccountResponseModel account = customerServiceClient.getAccountByAccountId(customerId);
        if (account == null) {
            throw new NotFoundException("Account with ID " + customerId + " not found.");
        }
        return this.addLinks(account);
    }


    @Override
    public LibraryAccountResponseModel addAccount(LibraryAccountRequestModel account) {
        return this.addLinks(customerServiceClient.addAccount(account));
    }

    @Override
    public LibraryAccountResponseModel updateAccount(LibraryAccountRequestModel account, String accountId) {
        return this.addLinks(customerServiceClient.updateAccount(account, accountId));
    }

    @Override
    public void removeAccount(String accountId) {
        customerServiceClient.removeAccount(accountId);
    }

    private LibraryAccountResponseModel addLinks(LibraryAccountResponseModel account) {
        Link selfLink = linkTo(methodOn(LibraryAccountController.class)
                .getAccountByAccountId(account.getAccountId()))
                .withSelfRel();
        account.add(selfLink);

        Link allAccountsLink = linkTo(methodOn(LibraryAccountController.class)
                .getAllAccounts())
                .withRel("accounts");
        account.add(allAccountsLink);

        return account;
    }
}
