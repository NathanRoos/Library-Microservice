package com.nathanroos.library.librarySubdomain.utils.Exceptions;
public class BookAlreadyExistsException extends RuntimeException {
    public BookAlreadyExistsException(String bookTitle) {
        super("A book with the title '" + bookTitle + "' already exists.");
    }
}
