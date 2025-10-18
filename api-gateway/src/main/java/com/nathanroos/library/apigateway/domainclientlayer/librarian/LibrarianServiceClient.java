package com.nathanroos.library.apigateway.domainclientlayer.librarian;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathanroos.library.apigateway.ExceptionsHandling.HttpErrorInfo;
import com.nathanroos.library.apigateway.ExceptionsHandling.InvalidInputException;
import com.nathanroos.library.apigateway.ExceptionsHandling.NotFoundException;
import com.nathanroos.library.apigateway.presentationlayer.customer.LibraryAccountResponseModel;
import com.nathanroos.library.apigateway.presentationlayer.librarian.LibrarianRequestModel;
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
public class LibrarianServiceClient {

    private final RestTemplate restTemplate;


    private final ObjectMapper mapper;

    private final String LIBRARIAN_SERVICE_BASE_URL;


    public LibrarianServiceClient(  RestTemplate restTemplate,
                                  ObjectMapper mapper,
                                  @Value("${app.libraryworker-service.host}") String librariansServiceHost,
                                  @Value("${app.libraryworker-service.port}") String librariansServicePort
    ) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;

        LIBRARIAN_SERVICE_BASE_URL = "http://" + librariansServiceHost + ":" + librariansServicePort + "/api/v1/workers";
    }

    public LibrarianResponseModel getLibrarian(String librarianId) {
        try {
            String url = LIBRARIAN_SERVICE_BASE_URL + "/" + librarianId;
            log.debug("Libraryworker-service URL is: " + url);
            LibrarianResponseModel librarianResponseModel = restTemplate.getForObject(url, LibrarianResponseModel.class);
            return librarianResponseModel;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public List<LibrarianResponseModel> getWorkers() {
        try {
            log.debug("libraryworker-service URL is {}", LIBRARIAN_SERVICE_BASE_URL);

            ResponseEntity<List<LibrarianResponseModel>> response = restTemplate.exchange(LIBRARIAN_SERVICE_BASE_URL, HttpMethod.GET, null, new ParameterizedTypeReference<List<LibrarianResponseModel>>() {});

            return response.getBody();
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public LibrarianResponseModel createLibrarian(LibrarianRequestModel librarianRequestModel) {
        try {
            String url = LIBRARIAN_SERVICE_BASE_URL;
            log.debug("Libraryworker-service URL is: " + url);
            return restTemplate.postForObject(url, librarianRequestModel, LibrarianResponseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public LibrarianResponseModel updateLibrarian(String librarianId, LibrarianRequestModel librarianRequestModel) {
        try {
            String url = LIBRARIAN_SERVICE_BASE_URL + "/" + librarianId;
            log.debug("Libraryworker-service URL is: " + url);
            restTemplate.put(url, librarianRequestModel);
            return getLibrarian(librarianId);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public void deleteLibrarian(String librarianId) {
        try {
            String url = LIBRARIAN_SERVICE_BASE_URL + "/" + librarianId;
            log.debug("Libraryworker-service URL is: " + url);
            restTemplate.delete(url);
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
