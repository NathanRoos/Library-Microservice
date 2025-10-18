package com.nathanroos.library.apigateway.presentationlayer.librarian;

//import com.nathanroos.library.Exceptions.InvalidInputException;
import com.nathanroos.library.apigateway.businesslayer.librarian.LibrarianService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/workers")
public class LibrarianController {

    private final LibrarianService librarianService;


    public LibrarianController(LibrarianService librarianService) {
        this.librarianService = librarianService;
    }

    @GetMapping(
            value = "{librarianId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LibrarianResponseModel> getLibrarian(@PathVariable String librarianId) {
        log.debug("1. Request Received in API-Gateway Librarians Controller: getLibrarian");
        return ResponseEntity.status(HttpStatus.OK).body(librarianService.getLibrarian(librarianId));
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<LibrarianResponseModel>> getWorkers() {
        log.debug("1. Request Received in API-Gateway Librarians Controller: getWorkers");
        return ResponseEntity.status(HttpStatus.OK).body(librarianService.getworkers());
    }

    @PutMapping(
            value = "{librarianId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LibrarianResponseModel> updateLibrarian(@PathVariable String librarianId, @RequestBody LibrarianRequestModel librarianRequestModel) {
        log.debug("1. Request Received in API-Gateway Librarians Controller: updateLibrarian");
        return ResponseEntity.status(HttpStatus.OK).body(librarianService.updateLibrarian(librarianId, librarianRequestModel));
    }

    @DeleteMapping(
            value = "/{librarianId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LibrarianResponseModel> deleteLibrarian(@PathVariable String librarianId) {
        log.debug("1. Request Received in API-Gateway Librarians Controller: deleteLibrarian");
        librarianService.deleteLibrarian(librarianId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LibrarianResponseModel> createLibrarian(@RequestBody LibrarianRequestModel librarianRequestModel) {
        log.debug("1. Request Received in API-Gateway Librarians Controller: createLibrarian");
        LibrarianResponseModel librarianResponseModel1 = librarianService.createLibrarian(librarianRequestModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(librarianService.createLibrarian(librarianRequestModel));
    }

}
