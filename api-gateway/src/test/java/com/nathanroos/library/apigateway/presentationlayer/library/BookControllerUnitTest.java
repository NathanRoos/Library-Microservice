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
class BookControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetBookById_Success() throws Exception {
        BookResponseModel response = new BookResponseModel("book-1", "John", "Doe", null, "Title", "Author", 3);

        when(bookService.getBookByBookId("book-1")).thenReturn(response);

        mockMvc.perform(get("/api/v1/books/book-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bookId").value("book-1"));
    }

    @Test
    void testGetBookById_NotFound() throws Exception {
        when(bookService.getBookByBookId(anyString()))
                .thenThrow(new NotFoundException("Book not found"));

        mockMvc.perform(get("/api/v1/books/notfound"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllBooks() throws Exception {
        BookResponseModel response = new BookResponseModel("book-1", "John", "Doe", null, "Title", "Author", 3);

        when(bookService.getBooks()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value("book-1"));
    }

    @Test
    void testAddBook_Success() throws Exception {
        BookRequestModel request = BookRequestModel.builder()
                .firstname("Jane")
                .lastname("Smith")
                .title("New Book")
                .author("Jane Doe")
                .copiesAvailable(5)
                .build();

        BookResponseModel response = new BookResponseModel("book-2", "Jane", "Smith", null, "New Book", "Jane Doe", 5);

        when(bookService.addBook(any(BookRequestModel.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookId").value("book-2"));
    }

    @Test
    void testAddBook_InvalidInput() throws Exception {
        when(bookService.addBook(any(BookRequestModel.class)))
                .thenThrow(new InvalidInputException("Invalid input"));

        BookRequestModel request = BookRequestModel.builder()
                .firstname("Jane")
                .lastname("Smith")
                .build(); // Missing required fields

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void testUpdateBook_Success() throws Exception {
        BookRequestModel request = BookRequestModel.builder()
                .firstname("UpdatedFirst")
                .lastname("UpdatedLast")
                .title("Updated Title")
                .author("Updated Author")
                .copiesAvailable(10)
                .build();

        BookResponseModel response = new BookResponseModel("book-1", "UpdatedFirst", "UpdatedLast", null, "Updated Title", "Updated Author", 10);

        when(bookService.updateBook(any(BookRequestModel.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/books/book-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value("book-1"));
    }

    @Test
    void testDeleteBook() throws Exception {
        mockMvc.perform(delete("/api/v1/books/book-1"))
                .andExpect(status().isNoContent());
    }
}
