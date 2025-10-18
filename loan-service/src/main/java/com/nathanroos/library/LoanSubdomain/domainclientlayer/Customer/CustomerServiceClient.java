package com.nathanroos.library.LoanSubdomain.domainclientlayer.Customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathanroos.library.LoanSubdomain.utils.Exceptions.HttpErrorInfo;
import com.nathanroos.library.LoanSubdomain.utils.Exceptions.InvalidInputException;
import com.nathanroos.library.LoanSubdomain.utils.Exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Slf4j
@Component
public class CustomerServiceClient {

    private final RestTemplate restTemplate;

    private final ObjectMapper mapper;

    private final String CUSTOMER_SERVICE_BASE_URL;

    public CustomerServiceClient(  RestTemplate restTemplate,
                                    ObjectMapper mapper,
                                    @Value("${app.customer-service.host}") String customerServiceHost,
                                    @Value("${app.customer-service.port}") String customerServicePort
    ) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;

        CUSTOMER_SERVICE_BASE_URL = "http://" + customerServiceHost + ":" + customerServicePort + "/api/v1/accounts";
    }

    public CustomerModel getAccountByAccountId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new InvalidInputException("CustomerId cannot be null or empty");
        }

        try {
            String url = CUSTOMER_SERVICE_BASE_URL + "/" + customerId;
            log.debug("customer-service URL is: " + url);

            CustomerModel libraryAccountResponseModel = restTemplate.getForObject(url, CustomerModel.class);

            if (libraryAccountResponseModel == null) {
                throw new NotFoundException("Customer not found for ID: " + customerId);
            }

            return libraryAccountResponseModel;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }



    public CustomerModel addAccount(CustomerModel account) {
        if (account == null) {
            throw new InvalidInputException("Customer request cannot be null");
        }

        try {
            String url = CUSTOMER_SERVICE_BASE_URL;
            log.debug("customer-service URL is: " + url);
            return restTemplate.postForObject(url, account, CustomerModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }


    public CustomerModel updateAccount(CustomerModel account, String accountId) {
        if (account == null) {
            throw new InvalidInputException("Account request cannot be null");
        }
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new InvalidInputException("Account ID cannot be null or empty");
        }

        try {
            String url = CUSTOMER_SERVICE_BASE_URL + "/" + accountId;
            log.debug("customer-service URL is: " + url);
            restTemplate.put(url, account);
            CustomerModel updatedAccount = getAccountByAccountId(accountId);
            if (updatedAccount == null) {
                throw new NotFoundException("Customer not found for ID: " + accountId);
            }
            return updatedAccount;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }


    public void removeAccount(String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new InvalidInputException("Account ID cannot be null or empty.");
        }
        try {
            String url = CUSTOMER_SERVICE_BASE_URL + "/" + accountId;
            log.debug("customer-service URL is: " + url);
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }


    public List<CustomerModel> getAccounts() {
        try {
            log.debug("customer-service URL is {}", CUSTOMER_SERVICE_BASE_URL);

            ResponseEntity<List<CustomerModel>> response = restTemplate.exchange(
                    CUSTOMER_SERVICE_BASE_URL,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<CustomerModel>>() {}
            );

            List<CustomerModel> body = response.getBody();
            return body != null ? body : Collections.emptyList();  // Return empty list if body is null
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }



    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        }
        catch (IOException ioex) {
            return ioex.getMessage();
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {

        if (ex.getStatusCode() == NOT_FOUND) {
            return new NotFoundException(getErrorMessage(ex));
        }
        if (ex.getStatusCode() == UNPROCESSABLE_ENTITY) {
            return new InvalidInputException(getErrorMessage(ex));
        }

        log.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
        log.warn("Error body: {}", ex.getResponseBodyAsString());

        return ex;
    }
}
