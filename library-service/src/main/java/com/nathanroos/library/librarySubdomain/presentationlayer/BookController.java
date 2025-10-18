package com.nathanroos.library.librarySubdomain.presentationlayer;

import com.nathanroos.library.librarySubdomain.businesslayer.BookService;
import com.nathanroos.library.librarySubdomain.utils.Exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final int UUID_LENGTH = 36;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<BookResponseModel>> getBooks() {
        return ResponseEntity.ok().body(bookService.getBooks());
    }

    @GetMapping(value = "/{bookId}", produces = "application/json")
    public ResponseEntity<BookResponseModel> getBookByBookId(@PathVariable String bookId) {
        if (bookId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid bookId provided: " + bookId);
        }
        return ResponseEntity.ok().body(bookService.getBookByBookId(bookId));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookResponseModel> AddBook(@RequestBody BookRequestModel bookRequestModel) {
        return new ResponseEntity<>(bookService.addBook(bookRequestModel), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{bookId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookResponseModel> updateBook(@PathVariable String bookId, @RequestBody BookRequestModel bookRequestModel)
    {
        return new ResponseEntity<>(bookService.updateBook(bookRequestModel, bookId), HttpStatus.OK);
    }

    @DeleteMapping(value = "/{bookId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookResponseModel> deleteBook(@PathVariable String bookId)
    {
        bookService.removeBook(bookId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
