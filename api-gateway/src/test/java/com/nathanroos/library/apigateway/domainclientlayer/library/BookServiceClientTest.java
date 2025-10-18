package com.nathanroos.library.apigateway.domainclientlayer.library;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathanroos.library.apigateway.ExceptionsHandling.HttpErrorInfo;
import com.nathanroos.library.apigateway.ExceptionsHandling.InvalidInputException;
import com.nathanroos.library.apigateway.ExceptionsHandling.NotFoundException;
import com.nathanroos.library.apigateway.presentationlayer.library.BookRequestModel;
import com.nathanroos.library.apigateway.presentationlayer.library.BookResponseModel;
import com.nathanroos.library.apigateway.domainclientlayer.library.GenreEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceClientTest {

    @InjectMocks
    private BookServiceClient bookServiceClient;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private String host = "localhost";
    private String port = "8080";

    @BeforeEach
    void setup() {
        bookServiceClient = new BookServiceClient(restTemplate, objectMapper, host, port);
    }

    @Test
    void testGetBookByBookId_Success() {
        String bookId = "book123";
        BookResponseModel mockBook = new BookResponseModel(bookId, "John", "Doe", GenreEnum.FICTION, "Title", "Author", 5);
        when(restTemplate.getForObject(anyString(), eq(BookResponseModel.class))).thenReturn(mockBook);

        BookResponseModel result = bookServiceClient.getBookByBookId(bookId);

        assertNotNull(result);
        assertEquals(bookId, result.getBookId());
    }

    @Test
    void testGetAllBooks_Success() {
        List<BookResponseModel> books = List.of(new BookResponseModel("book123", "John", "Doe", GenreEnum.FICTION, "Title", "Author", 5));
        ResponseEntity<List<BookResponseModel>> response = new ResponseEntity<>(books, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenReturn(response);

        List<BookResponseModel> result = bookServiceClient.getBooks();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testAddBook_Success() {
        BookRequestModel request = BookRequestModel.builder().firstname("John").lastname("Doe").genre(GenreEnum.FICTION).title("Title").author("Author").copiesAvailable(5).build();
        BookResponseModel response = new BookResponseModel("book123", "John", "Doe", GenreEnum.FICTION, "Title", "Author", 5);
        when(restTemplate.postForObject(anyString(), eq(request), eq(BookResponseModel.class))).thenReturn(response);

        BookResponseModel result = bookServiceClient.addBook(request);

        assertNotNull(result);
    }

    @Test
    void testUpdateBook_Success() {
        BookRequestModel request = BookRequestModel.builder().firstname("John").lastname("Doe").genre(GenreEnum.FICTION).title("Updated Title").author("Author").copiesAvailable(5).build();
        BookResponseModel updated = new BookResponseModel("book123", "John", "Doe", GenreEnum.FICTION, "Updated Title", "Author", 5);
        String bookId = "book123";

        when(restTemplate.getForObject(anyString(), eq(BookResponseModel.class))).thenReturn(updated);

        BookResponseModel result = bookServiceClient.updateBook(request, bookId);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
    }

    @Test
    void testDeleteBook_Success() {
        doNothing().when(restTemplate).delete(anyString());

        assertDoesNotThrow(() -> bookServiceClient.deleteBook("book123"));
    }

    @Test
    void testGetBookByBookId_NotFound() throws Exception {
        String bookId = "book123";
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(restTemplate.getForObject(anyString(), eq(BookResponseModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "Not Found", "/books/" + bookId));

        assertThrows(NotFoundException.class, () -> bookServiceClient.getBookByBookId(bookId));
    }

    @Test
    void testAddBook_InvalidInput() throws Exception {
        BookRequestModel request = BookRequestModel.builder().firstname("John").lastname("Doe").build();
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid Input", null, null, null);
        when(restTemplate.postForObject(anyString(), eq(request), eq(BookResponseModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid input", "/books"));

        assertThrows(InvalidInputException.class, () -> bookServiceClient.addBook(request));
    }

    @Test
    void testDeleteBook_NotFound() throws Exception {
        String bookId = "book123";
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        doThrow(ex).when(restTemplate).delete(anyString());
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "Not Found", "/books/" + bookId));

        assertThrows(NotFoundException.class, () -> bookServiceClient.deleteBook(bookId));
    }

    @Test
    void testGetBooks_HttpClientError() throws Exception {
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", null, null, null);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(ex);

        HttpClientErrorException thrown = assertThrows(HttpClientErrorException.class, () -> bookServiceClient.getBooks());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, thrown.getStatusCode());
    }

    @Test
    void testGetAllBooks_EmptyList() {
        ResponseEntity<List<BookResponseModel>> response = new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        List<BookResponseModel> result = bookServiceClient.getBooks();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
