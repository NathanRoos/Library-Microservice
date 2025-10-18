package com.nathanroos.library.apigateway.presentationlayer.library;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathanroos.library.apigateway.ExceptionsHandling.InvalidInputException;
import com.nathanroos.library.apigateway.ExceptionsHandling.NotFoundException;
import com.nathanroos.library.apigateway.businesslayer.library.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetBookById_Success() throws Exception {
        BookResponseModel mockResponse = new BookResponseModel(
                "book-123", "John", "Doe", null, "The Great Book", "AuthorX", 5);

        when(bookService.getBookByBookId("book-123")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/books/book-123")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bookId").value("book-123"));
    }

    @Test
    void testGetBookById_NotFound() throws Exception {
        when(bookService.getBookByBookId("book-999"))
                .thenThrow(new NotFoundException("Book not found"));

        mockMvc.perform(get("/api/v1/books/book-999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllBooks() throws Exception {
        when(bookService.getBooks()).thenReturn(
                List.of(new BookResponseModel("book-123", "John", "Doe", null, "The Great Book", "AuthorX", 5))
        );

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value("book-123"));
    }

    @Test
    void testAddBook() throws Exception {
        BookRequestModel request = BookRequestModel.builder()
                .firstname("Jane")
                .lastname("Smith")
                .title("New Book")
                .author("Jane Doe")
                .copiesAvailable(10)
                .build();

        BookResponseModel response = new BookResponseModel(
                "book-456", "Jane", "Smith", null, "New Book", "Jane Doe", 10);

        when(bookService.addBook(any(BookRequestModel.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookId").value("book-456"));
    }

    @Test
    void testAddBook_InvalidInput() throws Exception {
        when(bookService.addBook(any(BookRequestModel.class)))
                .thenThrow(new InvalidInputException("Invalid input"));

        BookRequestModel request = BookRequestModel.builder()
                .firstname("Jane")
                .lastname("Smith")
                .build(); // Missing required fields like title, author, copiesAvailable

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void testUpdateBook() throws Exception {
        BookRequestModel request = BookRequestModel.builder()
                .firstname("UpdatedFirst")
                .lastname("UpdatedLast")
                .title("Updated Title")
                .author("Updated Author")
                .copiesAvailable(15)
                .build();

        BookResponseModel response = new BookResponseModel(
                "book-123", "UpdatedFirst", "UpdatedLast", null, "Updated Title", "Updated Author", 15);

        when(bookService.updateBook(any(BookRequestModel.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/books/book-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value("book-123"));
    }

    @Test
    void testDeleteBook() throws Exception {
        mockMvc.perform(delete("/api/v1/books/book-123"))
                .andExpect(status().isNoContent());
    }
}
