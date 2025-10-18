package com.nathanroos.library.apigateway.domainclientlayer.loan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathanroos.library.apigateway.ExceptionsHandling.HttpErrorInfo;
import com.nathanroos.library.apigateway.ExceptionsHandling.InvalidInputException;
import com.nathanroos.library.apigateway.ExceptionsHandling.NotFoundException;

import com.nathanroos.library.apigateway.presentationlayer.loan.LoanRequestModel;
import com.nathanroos.library.apigateway.presentationlayer.loan.LoanResponseModel;
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
public class LoanServiceClient {

    private final RestTemplate restTemplate;


    private final ObjectMapper mapper;

    private final String LOAN_SERVICE_BASE_URL;

    public LoanServiceClient(  RestTemplate restTemplate,
                               ObjectMapper mapper,
                               @Value("${app.loan-service.host}") String loanServiceHost,
                               @Value("${app.loan-service.port}") String loanServicePort
    ) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;

        LOAN_SERVICE_BASE_URL = "http://" + loanServiceHost + ":" + loanServicePort + "/api/v1/accounts";
    }


    public LoanResponseModel getLoanByLoanId(String accountId, String loanId) {

        try {
            String url = LOAN_SERVICE_BASE_URL + "/" + accountId + "/" + "loans" + "/" + loanId;
            log.debug("loan-service URL is: " + url);
            LoanResponseModel loanResponseModel = restTemplate.getForObject(url, LoanResponseModel.class);
            return loanResponseModel;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }


    public void deleteLoan(String accountId, String loanId) {
        try {
            String url = LOAN_SERVICE_BASE_URL + "/" + accountId + "/" + "loans" + "/" + loanId;
            log.debug("loan-service URL is: " + url);
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public LoanResponseModel addLoan(String accountId, LoanRequestModel loan) {
        try {
            String url = LOAN_SERVICE_BASE_URL + "/" + accountId + "/" + "loans";
            log.debug("loan-service URL is: " + url);
            return restTemplate.postForObject(url, loan, LoanResponseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public LoanResponseModel updateLoan(String accountId, LoanRequestModel loan, String loanId) {
        try {
            String url = LOAN_SERVICE_BASE_URL + "/" + accountId + "/" + "loans" + "/" + loanId;
            log.debug("loan-service URL is: " + url);
            restTemplate.put(url, loan);
            return getLoanByLoanId(accountId, loanId);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public List<LoanResponseModel> getLoans(String accountId) {
        try {
            String url = LOAN_SERVICE_BASE_URL + "/" + accountId + "/" + "loans";
            log.debug("loan-service URL is {}", url);

            ResponseEntity<List<LoanResponseModel>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<LoanResponseModel>>() {});

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
