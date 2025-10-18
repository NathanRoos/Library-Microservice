package com.nathanroos.library.librarySubdomain.businesslayer;

import com.nathanroos.library.librarySubdomain.dataaccesslayer.Book;
import com.nathanroos.library.librarySubdomain.dataaccesslayer.BookIdentifier;
import com.nathanroos.library.librarySubdomain.dataaccesslayer.BookRepository;
import com.nathanroos.library.librarySubdomain.mappinglayer.BookRequestMapper;
import com.nathanroos.library.librarySubdomain.mappinglayer.BookResponseMapper;
import com.nathanroos.library.librarySubdomain.presentationlayer.BookRequestModel;
import com.nathanroos.library.librarySubdomain.presentationlayer.BookResponseModel;
import com.nathanroos.library.librarySubdomain.utils.Exceptions.InvalidInputException;
import com.nathanroos.library.librarySubdomain.utils.Exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookResponseMapper bookResponseMapper;

    private final BookRequestMapper bookRequestMapper;


    public BookServiceImpl(BookRepository bookRepository, BookResponseMapper bookResponseMapper, BookRequestMapper bookRequestMapper) {
        this.bookRepository = bookRepository;
        this.bookResponseMapper = bookResponseMapper;
        this.bookRequestMapper = bookRequestMapper;
    }

    @Override
    public List<BookResponseModel> getBooks() {
        List<Book> books = bookRepository.findAll();
        return bookResponseMapper.entityListToResponseModelList(books);
    }


    @Override
    public BookResponseModel getBookByBookId(String bookId) {
        Book book = getBookObjectById(bookId);

        return bookResponseMapper.entityToResponseModel(book);
    }

    @Override
    public BookResponseModel addBook(BookRequestModel book) {

        Book book1 = bookRequestMapper.requestModelToEntity(
                book, new BookIdentifier()
        );

        validateLibrarianRequestModel(book);

        Book createdBook = bookRepository.save(book1);
        return bookResponseMapper.entityToResponseModel(createdBook);
    }

    @Override
    public BookResponseModel updateBook(BookRequestModel UpdateBook, String bookId) {

        Book book = getBookObjectById(bookId);

        validateLibrarianRequestModel(UpdateBook);

        book.setAuthor(UpdateBook.getAuthor());
        book.setTitle(UpdateBook.getTitle());
        book.setGenre(UpdateBook.getGenre());
        book.setCopiesAvailable(UpdateBook.getCopiesAvailable());

        bookRepository.save(book);

        return bookResponseMapper.entityToResponseModel(book);
    }

    @Override
    public void removeBook(String bookId) {
        Book existingBook = getBookObjectById(bookId);

        bookRepository.delete(existingBook);
    }

    private void validateLibrarianRequestModel(BookRequestModel model) {
        if (model.getFirstname() == null || model.getFirstname().isBlank()) {
            throw new InvalidInputException("Invalid firstName: " + model.getFirstname());
        }
        if (model.getLastname() == null || model.getLastname().isBlank()) {
            throw new InvalidInputException("Invalid lastName: " + model.getLastname());
        }
        if (model.getAuthor() == null || model.getAuthor().isBlank()) {
            throw new InvalidInputException("Invalid author: " + model.getAuthor());
        }
        if (model.getTitle() == null || model.getTitle().isBlank()) {
            throw new InvalidInputException("Invalid title: " + model.getTitle());
        }
        if (model.getGenre() == null) {
            throw new InvalidInputException("Genre must not be null");
        }
        if (model.getCopiesAvailable() == null || model.getCopiesAvailable() < 0) {
            throw new InvalidInputException("CopiesAvailable must be non-negative");
        }
    }


    private Book getBookObjectById(String bookId) {
        try {
            UUID.fromString(bookId);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid bookId: " + bookId);
        }

        Book account = this.bookRepository.findAllByBookIdentifier_BookId(bookId);

        if (account == null) {
            throw new NotFoundException("Unknown bookId: " + bookId);
        }

        return account;
    }

}
