package com.nathanroos.library.LoanSubdomain.domainclientlayer.Library;

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
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Slf4j
@Component
public class LibraryServiceClient {

    private final RestTemplate restTemplate;


    private final ObjectMapper mapper;

    private final String BOOK_SERVICE_BASE_URL;

    public LibraryServiceClient(RestTemplate restTemplate,
                                ObjectMapper mapper,
                                @Value("${app.library-service.host}") String bookServiceHost,
                                @Value("${app.library-service.port}") String bookServicePort
    ) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;

        BOOK_SERVICE_BASE_URL = "http://" + bookServiceHost + ":" + bookServicePort + "/api/v1/books";
    }


    public LibraryModel getBookByBookId(String bookId) {
        if (bookId == null || bookId.trim().isEmpty()) {
            throw new InvalidInputException("Book ID cannot be null or empty.");
        }

        try {
            String url = BOOK_SERVICE_BASE_URL + "/" + bookId;
            log.debug("library-service URL is: " + url);
            LibraryModel bookResponseModel = restTemplate.getForObject(url, LibraryModel.class);
            return bookResponseModel;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }


    public void deleteBook(String bookId) {
        if (bookId == null || bookId.trim().isEmpty()) {
            throw new InvalidInputException("Book ID cannot be null or empty.");
        }
        try {
            String url = BOOK_SERVICE_BASE_URL + "/" + bookId;
            log.debug("library-service URL is: " + url);
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }


    public LibraryModel addBook(LibraryModel book) {
        if (book == null) {
            throw new InvalidInputException("Book request cannot be null");
        }
        try {
            String url = BOOK_SERVICE_BASE_URL;
            log.debug("library-service URL is: " + url);
            return restTemplate.postForObject(url, book, LibraryModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }


    public LibraryModel updateBook(LibraryModel book, String bookId) {
        if (book == null) {
            throw new InvalidInputException("Book request cannot be null");
        }
        if (bookId == null || bookId.trim().isEmpty()) {
            throw new InvalidInputException("Book ID cannot be null or empty");
        }

        try {
            String url = BOOK_SERVICE_BASE_URL + "/" + bookId;
            log.debug("library-service URL is: " + url);
            restTemplate.put(url, book);
            LibraryModel updatedBook = getBookByBookId(bookId);  // <- Save to variable
            if (updatedBook == null) {                           // <- Null check
                throw new NotFoundException("Book not found for ID: " + bookId);
            }
            return updatedBook;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }



    public List<LibraryModel> getBooks() {
        try {
            log.debug("library-service URL is {}", BOOK_SERVICE_BASE_URL);

            ResponseEntity<List<LibraryModel>> response = restTemplate.exchange(
                    BOOK_SERVICE_BASE_URL, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<LibraryModel>>() {});

            return (response != null && response.getBody() != null) ? response.getBody() : List.of();
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
