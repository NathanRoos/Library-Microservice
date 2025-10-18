package com.nathanroos.library.apigateway.businesslayer.library;

import com.nathanroos.library.apigateway.ExceptionsHandling.NotFoundException;
import com.nathanroos.library.apigateway.domainclientlayer.customer.CustomerServiceClient;
import com.nathanroos.library.apigateway.domainclientlayer.library.BookServiceClient;
import com.nathanroos.library.apigateway.presentationlayer.librarian.LibrarianController;
import com.nathanroos.library.apigateway.presentationlayer.librarian.LibrarianResponseModel;
import com.nathanroos.library.apigateway.presentationlayer.library.BookController;
import com.nathanroos.library.apigateway.presentationlayer.library.BookRequestModel;
import com.nathanroos.library.apigateway.presentationlayer.library.BookResponseModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class BookServiceImpl implements BookService {

    private final BookServiceClient bookServiceClient;


    public BookServiceImpl(BookServiceClient bookServiceClient) {
        this.bookServiceClient = bookServiceClient;
    }

    @Override
    public List<BookResponseModel> getBooks() {
        return bookServiceClient.getBooks().stream().map(this::addLinks).toList();

    }

    @Override
    public BookResponseModel getBookByBookId(String bookId) {
        BookResponseModel book = bookServiceClient.getBookByBookId(bookId);
        if (book == null) {
            throw new NotFoundException("Book with ID: " + bookId + " not found");
        }
        return addLinks(book);
    }


    @Override
    public BookResponseModel addBook(BookRequestModel book) {
        return addLinks(bookServiceClient.addBook(book));
    }

    @Override
    public BookResponseModel updateBook(BookRequestModel book, String bookId) {
        return addLinks(bookServiceClient.updateBook(book, bookId));
    }

    @Override
    public void removeBook(String bookId) {
        bookServiceClient.deleteBook(bookId);
    }

    private BookResponseModel addLinks(BookResponseModel book) {
        Link selfLink = linkTo(methodOn(BookController.class)
                .getBookByBookId(book.getBookId()))
                .withSelfRel();
        book.add(selfLink);

        Link allBooksLink = linkTo(methodOn(BookController.class)
                .getBooks())
                .withRel("books");
        book.add(allBooksLink);

        return book;
    }
}
