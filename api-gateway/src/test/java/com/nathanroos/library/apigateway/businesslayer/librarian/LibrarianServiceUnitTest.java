package com.nathanroos.library.apigateway.businesslayer.librarian;

import com.nathanroos.library.apigateway.ExceptionsHandling.NotFoundException;
import com.nathanroos.library.apigateway.domainclientlayer.librarian.*;
import com.nathanroos.library.apigateway.presentationlayer.librarian.LibrarianRequestModel;
import com.nathanroos.library.apigateway.presentationlayer.librarian.LibrarianResponseModel;
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
class LibrarianServiceUnitTest {

    @Mock
    private LibrarianServiceClient librarianServiceClient;

    @InjectMocks
    private LibrarianServiceImpl librarianService;

    private LibrarianRequestModel requestModel;
    private LibrarianResponseModel responseModel;

    @BeforeEach
    void setUp() {
        String librarianId = UUID.randomUUID().toString();

        requestModel = LibrarianRequestModel.builder()
                .firstname("Alice")
                .lastname("Smith")
                .email("alice.smith@example.com")
                .libraryWorkerAddress(WorkerAddress.builder()
                        .streetName("123 Main St")
                        .city("City")
                        .province(ProvinceEnum.QUEBEC)
                        .postalCode("12345")
                        .build())
                .position(Position.builder().positionTitle(PositionEnum.LIBRARY_CLERK).build())
                .librarianPhoneNumber(LibrarianPhoneNumber.builder().phoneNumber("555-555-5555").build())
                .build();

        responseModel = LibrarianResponseModel.builder()
                .librarianId(librarianId)
                .firstname("Alice")
                .lastname("Smith")
                .email("alice.smith@example.com")
                .libraryWorkerAddress(requestModel.getLibraryWorkerAddress())
                .position(requestModel.getPosition())
                .librarianPhoneNumber(requestModel.getLibrarianPhoneNumber())
                .build();
    }

    @Test
    void whenGetLibrarianById_thenReturnLibrarian() {
        when(librarianServiceClient.getLibrarian(responseModel.getLibrarianId())).thenReturn(responseModel);

        var result = librarianService.getLibrarian(responseModel.getLibrarianId());

        assertNotNull(result);
        assertEquals(responseModel.getLibrarianId(), result.getLibrarianId());
        assertEquals(responseModel.getFirstname(), result.getFirstname());
    }

    @Test
    void whenGetAllLibrarians_thenReturnList() {
        when(librarianServiceClient.getWorkers()).thenReturn(List.of(responseModel));

        var result = librarianService.getworkers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(responseModel.getLibrarianId(), result.get(0).getLibrarianId());
    }

    @Test
    void whenGetAllLibrariansIsEmpty_thenReturnEmptyList() {
        when(librarianServiceClient.getWorkers()).thenReturn(Collections.emptyList());

        var result = librarianService.getworkers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void whenCreateLibrarian_thenReturnCreatedLibrarian() {
        when(librarianServiceClient.createLibrarian(requestModel)).thenReturn(responseModel);

        var result = librarianService.createLibrarian(requestModel);

        assertNotNull(result);
        assertEquals(responseModel.getLibrarianId(), result.getLibrarianId());
        assertEquals(responseModel.getFirstname(), result.getFirstname());
    }

    @Test
    void whenUpdateLibrarian_thenReturnUpdatedLibrarian() {
        when(librarianServiceClient.updateLibrarian(responseModel.getLibrarianId(), requestModel)).thenReturn(responseModel);

        var result = librarianService.updateLibrarian(responseModel.getLibrarianId(), requestModel);

        assertNotNull(result);
        assertEquals(responseModel.getLibrarianId(), result.getLibrarianId());
    }

    @Test
    void whenDeleteLibrarian_thenVerifyClientCall() {
        doNothing().when(librarianServiceClient).deleteLibrarian(responseModel.getLibrarianId());

        librarianService.deleteLibrarian(responseModel.getLibrarianId());

        verify(librarianServiceClient, times(1)).deleteLibrarian(responseModel.getLibrarianId());
    }

    @Test
    void whenGetLibrarianByIdNotFound_thenThrowNotFoundException() {
        when(librarianServiceClient.getLibrarian(anyString())).thenReturn(null);

        assertThrows(NotFoundException.class, () -> {
            librarianService.getLibrarian("nonexistent-id");
        });
    }

    @Test
    void whenLibrarianServiceClientThrowsException_thenPropagate() {
        when(librarianServiceClient.getLibrarian(anyString())).thenThrow(new RuntimeException("Service failure"));

        assertThrows(RuntimeException.class, () -> {
            librarianService.getLibrarian("some-id");
        });
    }
}
