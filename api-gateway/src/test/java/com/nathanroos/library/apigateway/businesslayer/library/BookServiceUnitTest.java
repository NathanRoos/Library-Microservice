package com.nathanroos.library.apigateway.businesslayer.library;

import com.nathanroos.library.apigateway.ExceptionsHandling.NotFoundException;
import com.nathanroos.library.apigateway.domainclientlayer.library.BookServiceClient;
import com.nathanroos.library.apigateway.presentationlayer.library.BookRequestModel;
import com.nathanroos.library.apigateway.presentationlayer.library.BookResponseModel;
import com.nathanroos.library.apigateway.domainclientlayer.library.GenreEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceUnitTest {

    @Mock
    private BookServiceClient bookServiceClient;

    @InjectMocks
    private BookServiceImpl bookService;

    private BookRequestModel requestModel;
    private BookResponseModel responseModel;
    private String bookId;

    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID().toString();

        requestModel = BookRequestModel.builder()
                .firstname("John")
                .lastname("Doe")
                .title("Test Title")
                .author("Test Author")
                .genre(GenreEnum.FICTION)
                .copiesAvailable(5)
                .build();

        responseModel = new BookResponseModel(
                bookId, "John", "Doe", GenreEnum.FICTION, "Test Title", "Test Author", 5
        );
    }

    @Test
    void whenGetBookByBookId_thenReturnBook() {
        when(bookServiceClient.getBookByBookId(bookId)).thenReturn(responseModel);

        var result = bookService.getBookByBookId(bookId);

        assertNotNull(result);
        assertEquals(responseModel.getBookId(), result.getBookId());
        assertEquals(responseModel.getTitle(), result.getTitle());
    }

    @Test
    void whenGetAllBooks_thenReturnList() {
        when(bookServiceClient.getBooks()).thenReturn(List.of(responseModel));

        var result = bookService.getBooks();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(responseModel.getBookId(), result.get(0).getBookId());
    }

    @Test
    void whenGetAllBooksIsEmpty_thenReturnEmptyList() {
        when(bookServiceClient.getBooks()).thenReturn(Collections.emptyList());

        var result = bookService.getBooks();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void whenAddBook_thenReturnCreatedBook() {
        when(bookServiceClient.addBook(requestModel)).thenReturn(responseModel);

        var result = bookService.addBook(requestModel);

        assertNotNull(result);
        assertEquals(responseModel.getBookId(), result.getBookId());
        assertEquals(responseModel.getTitle(), result.getTitle());
    }

    @Test
    void whenUpdateBook_thenReturnUpdatedBook() {
        when(bookServiceClient.updateBook(requestModel, bookId)).thenReturn(responseModel);

        var result = bookService.updateBook(requestModel, bookId);

        assertNotNull(result);
        assertEquals(responseModel.getBookId(), result.getBookId());
    }

    @Test
    void whenRemoveBook_thenVerifyClientCall() {
        doNothing().when(bookServiceClient).deleteBook(bookId);

        bookService.removeBook(bookId);

        verify(bookServiceClient, times(1)).deleteBook(bookId);
    }

    @Test
    void whenGetBookByBookIdNotFound_thenThrowNotFoundException() {
        when(bookServiceClient.getBookByBookId(anyString())).thenReturn(null);

        assertThrows(NotFoundException.class, () -> {
            bookService.getBookByBookId("nonexistent-id");
        });
    }

    @Test
    void whenBookServiceClientThrowsException_thenPropagate() {
        when(bookServiceClient.getBookByBookId(anyString())).thenThrow(new RuntimeException("Service failure"));

        assertThrows(RuntimeException.class, () -> {
            bookService.getBookByBookId("some-id");
        });
    }
}
