package com.nathanroos.library.apigateway.domainclientlayer.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathanroos.library.apigateway.ExceptionsHandling.HttpErrorInfo;
import com.nathanroos.library.apigateway.ExceptionsHandling.InvalidInputException;
import com.nathanroos.library.apigateway.ExceptionsHandling.NotFoundException;
import com.nathanroos.library.apigateway.presentationlayer.customer.LibraryAccountRequestModel;
import com.nathanroos.library.apigateway.presentationlayer.customer.LibraryAccountResponseModel;
import com.nathanroos.library.apigateway.presentationlayer.librarian.LibrarianResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
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

    public LibraryAccountResponseModel getAccountByAccountId(String customerId) {
        try {
            String url = CUSTOMER_SERVICE_BASE_URL + "/" + customerId;
            log.debug("customer-service URL is: " + url);
            LibraryAccountResponseModel libraryAccountResponseModel = restTemplate.getForObject(url, LibraryAccountResponseModel.class);
            return libraryAccountResponseModel;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public LibraryAccountResponseModel addAccount(LibraryAccountRequestModel account) {
        try {
            String url = CUSTOMER_SERVICE_BASE_URL;
            log.debug("customer-service URL is: " + url);
            return restTemplate.postForObject(url, account, LibraryAccountResponseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public LibraryAccountResponseModel updateAccount(LibraryAccountRequestModel account, String accountId) {
        try {
            String url = CUSTOMER_SERVICE_BASE_URL + "/" + accountId;
            log.debug("customer-service URL is: " + url);
            restTemplate.put(url, account);
            return getAccountByAccountId(accountId);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public void removeAccount(String accountId) {
        try {
            String url = CUSTOMER_SERVICE_BASE_URL + "/" + accountId;
            log.debug("customer-service URL is: " + url);
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public List<LibraryAccountResponseModel> getAccounts() {
        try {
            log.debug("customer-service URL is {}", CUSTOMER_SERVICE_BASE_URL);

            ResponseEntity<List<LibraryAccountResponseModel>> response = restTemplate.exchange(CUSTOMER_SERVICE_BASE_URL, HttpMethod.GET, null, new ParameterizedTypeReference<List<LibraryAccountResponseModel>>() {});

            return response.getBody();
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
