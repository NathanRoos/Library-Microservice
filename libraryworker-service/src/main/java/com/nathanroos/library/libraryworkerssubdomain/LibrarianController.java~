package com.nathanroos.library.libraryworkerssubdomain.PresentationLayer;

import com.nathanroos.library.libraryworkerssubdomain.BussinessLayer.LibrarianService;
import com.nathanroos.library.libraryworkerssubdomain.utils.Exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final int UUID_LENGTH = 36;

    @Autowired
    public LibrarianController(LibrarianService librarianService) {
        this.librarianService = librarianService;
    }


    @GetMapping(produces = "application/json")
    public ResponseEntity<List<LibrarianResponseModel>> getWorkers() {
        return ResponseEntity.ok().body(librarianService.getWorkers());
    }

    @GetMapping(value = "{librarianId}", produces = "application/json")
    public ResponseEntity<LibrarianResponseModel> getLibrarianByLibrarianId(@PathVariable String librarianId) {
        if (librarianId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid librarianId provided: " + librarianId);
        }
        return ResponseEntity.ok().body(librarianService.getLibrarianByLibrarianId(librarianId));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LibrarianResponseModel> AddLibrarian(@RequestBody LibrarianRequestModel librarianRequestModel) {
        return new ResponseEntity<>(librarianService.addLibrarian(librarianRequestModel), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{librarianId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LibrarianResponseModel> updateLibrarian(@PathVariable String librarianId,
                                                                  @RequestBody LibrarianRequestModel librarianRequestModel)
    {
        return new ResponseEntity<>(librarianService.updateLibrarian(librarianRequestModel, librarianId), HttpStatus.OK);
    }

    @DeleteMapping(value = "/{librarianId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LibrarianResponseModel> removeLibrarian(@PathVariable String librarianId)
    {
        librarianService.removeLibrarian(librarianId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
