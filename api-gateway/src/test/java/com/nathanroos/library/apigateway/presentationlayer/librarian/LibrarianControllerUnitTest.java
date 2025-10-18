package com.nathanroos.library.apigateway.presentationlayer.librarian;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathanroos.library.apigateway.ExceptionsHandling.InvalidInputException;
import com.nathanroos.library.apigateway.ExceptionsHandling.NotFoundException;
import com.nathanroos.library.apigateway.businesslayer.librarian.LibrarianService;
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

@WebMvcTest(LibrarianController.class)
class LibrarianControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LibrarianService librarianService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetLibrarianById_Success() throws Exception {
        LibrarianResponseModel response = LibrarianResponseModel.builder()
                .librarianId("librarian-1")
                .firstname("John")
                .lastname("Doe")
                .build();

        when(librarianService.getLibrarian("librarian-1")).thenReturn(response);

        mockMvc.perform(get("/api/v1/workers/librarian-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.librarianId").value("librarian-1"));
    }

    @Test
    void testGetLibrarianById_NotFound() throws Exception {
        when(librarianService.getLibrarian(anyString()))
                .thenThrow(new NotFoundException("Librarian not found"));

        mockMvc.perform(get("/api/v1/workers/notfound"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllLibrarians() throws Exception {
        LibrarianResponseModel response = LibrarianResponseModel.builder()
                .librarianId("librarian-1")
                .firstname("John")
                .lastname("Doe")
                .build();

        when(librarianService.getworkers()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/workers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].librarianId").value("librarian-1"));
    }

    @Test
    void testAddLibrarian_Success() throws Exception {
        LibrarianRequestModel request = LibrarianRequestModel.builder()
                .firstname("Jane")
                .lastname("Smith")
                .email("jane.smith@example.com")
                .build();

        LibrarianResponseModel response = LibrarianResponseModel.builder()
                .librarianId("librarian-2")
                .firstname("Jane")
                .lastname("Smith")
                .build();

        when(librarianService.createLibrarian(any(LibrarianRequestModel.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/workers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.librarianId").value("librarian-2"));
    }

    @Test
    void testAddLibrarian_InvalidInput() throws Exception {
        when(librarianService.createLibrarian(any(LibrarianRequestModel.class)))
                .thenThrow(new InvalidInputException("Invalid input"));

        LibrarianRequestModel request = LibrarianRequestModel.builder()
                .firstname("Jane")
                .lastname("Smith")
                .build(); // Missing required fields like email, address, etc.

        mockMvc.perform(post("/api/v1/workers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void testUpdateLibrarian_Success() throws Exception {
        LibrarianRequestModel request = LibrarianRequestModel.builder()
                .firstname("UpdatedFirst")
                .lastname("UpdatedLast")
                .email("updated@example.com")
                .build();

        LibrarianResponseModel response = LibrarianResponseModel.builder()
                .librarianId("librarian-1")
                .firstname("UpdatedFirst")
                .lastname("UpdatedLast")
                .build();

        when(librarianService.updateLibrarian(anyString(), any(LibrarianRequestModel.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/workers/librarian-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.librarianId").value("librarian-1"));
    }

    @Test
    void testDeleteLibrarian() throws Exception {
        mockMvc.perform(delete("/api/v1/workers/librarian-1"))
                .andExpect(status().isNoContent());
    }
}
