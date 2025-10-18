package com.nathanroos.library.apigateway.presentationlayer.library;


import com.nathanroos.library.apigateway.businesslayer.librarian.LibrarianService;
import com.nathanroos.library.apigateway.businesslayer.library.BookService;
import com.nathanroos.library.apigateway.presentationlayer.librarian.LibrarianResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/books")
public class BookController {

    private final BookService bookService;


    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping(
            value = "{bookId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<BookResponseModel> getBookByBookId(@PathVariable String bookId) {
        log.debug("1. Request Received in API-Gateway Books Controller: getbook");
        return ResponseEntity.status(HttpStatus.OK).body(bookService.getBookByBookId(bookId));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<BookResponseModel>> getBooks() {
        log.debug("1. Request Received in API-Gateway Books Controller: getbooks");
        return ResponseEntity.status(HttpStatus.OK).body(bookService.getBooks());
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<BookResponseModel> createBook(@RequestBody BookRequestModel book) {
        log.debug("1. Request Received in API-Gateway Books Controller: createbook");
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.addBook(book));
    }

    @PutMapping(
            value = "{bookId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<BookResponseModel> updateBook(@PathVariable String bookId, @RequestBody BookRequestModel book) {
        log.debug("1. Request Received in API-Gateway Books Controller: updatebook");
        return ResponseEntity.status(HttpStatus.OK).body(bookService.updateBook(book, bookId));
    }

    @DeleteMapping(
            value = "{bookId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<BookResponseModel> removeBook(@PathVariable String bookId) {
        log.debug("1. Request Received in API-Gateway Books Controller: removeBook");
        bookService.removeBook(bookId);
        return ResponseEntity.noContent().build();
    }
}
