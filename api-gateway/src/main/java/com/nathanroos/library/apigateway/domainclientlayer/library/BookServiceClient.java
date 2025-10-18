package com.nathanroos.library.apigateway.domainclientlayer.library;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathanroos.library.apigateway.ExceptionsHandling.HttpErrorInfo;
import com.nathanroos.library.apigateway.ExceptionsHandling.InvalidInputException;
import com.nathanroos.library.apigateway.ExceptionsHandling.NotFoundException;
import com.nathanroos.library.apigateway.presentationlayer.customer.LibraryAccountResponseModel;
import com.nathanroos.library.apigateway.presentationlayer.library.BookRequestModel;
import com.nathanroos.library.apigateway.presentationlayer.library.BookResponseModel;
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
public class BookServiceClient {

    private final RestTemplate restTemplate;


    private final ObjectMapper mapper;

    private final String BOOK_SERVICE_BASE_URL;

    public BookServiceClient(  RestTemplate restTemplate,
                                   ObjectMapper mapper,
                                   @Value("${app.library-service.host}") String bookServiceHost,
                                   @Value("${app.library-service.port}") String bookServicePort
    ) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;

        BOOK_SERVICE_BASE_URL = "http://" + bookServiceHost + ":" + bookServicePort + "/api/v1/books";
    }


    public BookResponseModel getBookByBookId(String bookId) {

        try {
            String url = BOOK_SERVICE_BASE_URL + "/" + bookId;
            log.debug("library-service URL is: " + url);
            BookResponseModel bookResponseModel = restTemplate.getForObject(url, BookResponseModel.class);
            return bookResponseModel;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public void deleteBook(String bookId) {
        try {
            String url = BOOK_SERVICE_BASE_URL + "/" + bookId;
            log.debug("library-service URL is: " + url);
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public BookResponseModel addBook(BookRequestModel book) {
        try {
            String url = BOOK_SERVICE_BASE_URL;
            log.debug("library-service URL is: " + url);
            return restTemplate.postForObject(url, book, BookResponseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public BookResponseModel updateBook(BookRequestModel book, String bookId) {
        try {
            String url = BOOK_SERVICE_BASE_URL + "/" + bookId;
            log.debug("library-service URL is: " + url);
            restTemplate.put(url, book);
            return getBookByBookId(bookId);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public List<BookResponseModel> getBooks() {
        try {
            log.debug("library-service URL is {}", BOOK_SERVICE_BASE_URL);

            ResponseEntity<List<BookResponseModel>> response = restTemplate.exchange(BOOK_SERVICE_BASE_URL, HttpMethod.GET, null, new ParameterizedTypeReference<List<BookResponseModel>>() {});

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
