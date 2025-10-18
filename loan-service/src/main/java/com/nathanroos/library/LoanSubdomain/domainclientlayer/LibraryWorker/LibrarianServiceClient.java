package com.nathanroos.library.LoanSubdomain.domainclientlayer.LibraryWorker;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.nathanroos.library.LoanSubdomain.domainclientlayer.LibraryWorker.LibraryWorkerModel;
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

    public LibraryWorkerModel getLibrarian(String librarianId) {
        if (librarianId == null || librarianId.trim().isEmpty()) {
            throw new InvalidInputException("Librarian ID cannot be null or empty.");
        }

        try {
            String url = LIBRARIAN_SERVICE_BASE_URL + "/" + librarianId;
            log.debug("Libraryworker-service URL is: " + url);
            LibraryWorkerModel librarianResponseModel = restTemplate.getForObject(url, LibraryWorkerModel.class);

            if (librarianResponseModel == null) {
                throw new NotFoundException("Librarian not found for ID: " + librarianId);
            }

            return librarianResponseModel;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }



    public List<LibraryWorkerModel> getWorkers() {
        try {
            log.debug("librarian-service URL is {}", LIBRARIAN_SERVICE_BASE_URL);

            ResponseEntity<List<LibraryWorkerModel>> response = restTemplate.exchange(
                    LIBRARIAN_SERVICE_BASE_URL, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<LibraryWorkerModel>>() {});

            return response.getBody() != null ? response.getBody() : List.of();  // Return empty list if null
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }


    public LibraryWorkerModel createLibrarian(LibraryWorkerModel librarianRequestModel) {
        if (librarianRequestModel == null) {
            throw new InvalidInputException("Librarian request cannot be null");
        }
        try {
            String url = LIBRARIAN_SERVICE_BASE_URL;
            log.debug("Libraryworker-service URL is: " + url);
            return restTemplate.postForObject(url, librarianRequestModel, LibraryWorkerModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }


    public LibraryWorkerModel updateLibrarian(String librarianId, LibraryWorkerModel librarianRequestModel) {
        if (librarianRequestModel == null) {
            throw new InvalidInputException("Librarian request cannot be null");
        }
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
        if (librarianId == null || librarianId.trim().isEmpty()) {
            throw new InvalidInputException("Librarian ID cannot be null or empty.");
        }
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
