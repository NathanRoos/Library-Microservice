package com.nathanroos.library.LoanSubdomain.domainclientlayer.Library;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathanroos.library.LoanSubdomain.utils.Exceptions.HttpErrorInfo;
import com.nathanroos.library.LoanSubdomain.utils.Exceptions.InvalidInputException;
import com.nathanroos.library.LoanSubdomain.utils.Exceptions.NotFoundException;
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
class LibraryServiceClientTest {

    @InjectMocks
    private LibraryServiceClient libraryServiceClient;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        libraryServiceClient = new LibraryServiceClient(restTemplate, objectMapper, "localhost", "8080");
    }

    @Test
    void testGetBookById_Success() {
        String bookId = "book-123";
        LibraryModel book = LibraryModel.builder().bookId(bookId).title("Title").author("Author").build();
        when(restTemplate.getForObject(anyString(), eq(LibraryModel.class))).thenReturn(book);

        LibraryModel result = libraryServiceClient.getBookByBookId(bookId);
        assertNotNull(result);
        assertEquals(bookId, result.getBookId());
    }

    @Test
    void testGetAllBooks_Success() {
        List<LibraryModel> books = List.of(LibraryModel.builder().bookId("book-1").title("Book1").author("A1").build());
        ResponseEntity<List<LibraryModel>> response = new ResponseEntity<>(books, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenReturn(response);

        List<LibraryModel> result = libraryServiceClient.getBooks();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testCreateBook_Success() {
        LibraryModel request = LibraryModel.builder().bookId("book-123").title("Title").author("Author").build();
        when(restTemplate.postForObject(anyString(), eq(request), eq(LibraryModel.class))).thenReturn(request);

        LibraryModel result = libraryServiceClient.addBook(request);
        assertNotNull(result);
        assertEquals("book-123", result.getBookId());
    }

    @Test
    void testUpdateBook_Success() {
        LibraryModel request = LibraryModel.builder().bookId("book-123").title("Updated Title").author("Updated Author").build();
        when(restTemplate.getForObject(anyString(), eq(LibraryModel.class))).thenReturn(request);
        doNothing().when(restTemplate).put(anyString(), eq(request));

        LibraryModel result = libraryServiceClient.updateBook(request, "book-123");
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
    }

    @Test
    void testDeleteBook_Success() {
        doNothing().when(restTemplate).delete(anyString());
        assertDoesNotThrow(() -> libraryServiceClient.deleteBook("book-123"));
    }

    @Test
    void testGetBook_NotFound() throws Exception {
        String bookId = "nonexistent";
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(restTemplate.getForObject(anyString(), eq(LibraryModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "Not found", "/books/" + bookId));

        assertThrows(NotFoundException.class, () -> libraryServiceClient.getBookByBookId(bookId));
    }

    @Test
    void testCreateBook_InvalidInput() throws Exception {
        LibraryModel request = LibraryModel.builder().bookId("book-123").title("Title").author("Author").build();
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid Input", null, null, null);
        when(restTemplate.postForObject(anyString(), eq(request), eq(LibraryModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid input", "/books"));

        assertThrows(InvalidInputException.class, () -> libraryServiceClient.addBook(request));
    }

    @Test
    void testDeleteBook_NotFound() throws Exception {
        String bookId = "nonexistent";
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        doThrow(ex).when(restTemplate).delete(anyString());
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "Not found", "/books/" + bookId));

        assertThrows(NotFoundException.class, () -> libraryServiceClient.deleteBook(bookId));
    }

    @Test
    void testUpdateBook_NotFound() throws Exception {
        String bookId = "nonexistent";
        LibraryModel request = LibraryModel.builder().bookId(bookId).title("Title").author("Author").build();
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(restTemplate.getForObject(anyString(), eq(LibraryModel.class))).thenThrow(ex);
        when(objectMapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "Not found", "/books/" + bookId));

        assertThrows(NotFoundException.class, () -> libraryServiceClient.updateBook(request, bookId));
    }

    @Test
    void testGetBook_EmptyId_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> libraryServiceClient.getBookByBookId(" "));
    }

    @Test
    void testCreateBook_NullInput_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> libraryServiceClient.addBook(null));
    }


    @Test
    void testUpdateBook_NullInput_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> libraryServiceClient.updateBook(null,"book-123"));
    }

    @Test
    void testDeleteBook_EmptyId_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class, () -> libraryServiceClient.deleteBook(" "));
    }

    @Test
    void testGetBooks_HttpError() {
        HttpClientErrorException ex = HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", null, null, null);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenThrow(ex);

        assertThrows(HttpClientErrorException.class, () -> libraryServiceClient.getBooks());
    }

    @Test
    void testGetBooks_EmptyResponse_ShouldReturnEmptyList() {
        ResponseEntity<List<LibraryModel>> response = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class))).thenReturn(response);

        List<LibraryModel> result = libraryServiceClient.getBooks();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdateBook_PutSuccess_ButGetReturnsNull_ShouldThrowNotFound() {
        String bookId = "book-123";
        LibraryModel request = LibraryModel.builder().bookId(bookId).build();
        doNothing().when(restTemplate).put(anyString(), eq(request));
        when(restTemplate.getForObject(contains(bookId), eq(LibraryModel.class)))
                .thenReturn(null); // Simulate getBookByBookId returns null

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> libraryServiceClient.updateBook(request, bookId));
        assertEquals("Book not found for ID: " + bookId, thrown.getMessage());
    }

    @Test
    void testPOJOs() {
        LibraryModel book = LibraryModel.builder()
                .bookId("book-123")
                .title("Sample Title")
                .author("Sample Author")
                .build();
        assertEquals("book-123", book.getBookId());
        assertEquals("Sample Title", book.getTitle());
        assertEquals("Sample Author", book.getAuthor());

        // Example for BookGenreEnum if it exists
        GenreEnum genre = GenreEnum.SCIENCE_FICTION;
        assertEquals(GenreEnum.SCIENCE_FICTION, genre);
    }

}
