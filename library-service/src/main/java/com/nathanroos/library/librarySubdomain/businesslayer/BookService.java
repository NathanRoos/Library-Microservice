package com.nathanroos.library.librarySubdomain.businesslayer;



import com.nathanroos.library.librarySubdomain.presentationlayer.BookRequestModel;
import com.nathanroos.library.librarySubdomain.presentationlayer.BookResponseModel;

import java.util.List;

public interface BookService {

    List<BookResponseModel> getBooks();
    BookResponseModel getBookByBookId(String bookId);
    BookResponseModel addBook(BookRequestModel book);
    BookResponseModel updateBook(BookRequestModel book, String bookId);
    void removeBook(String bookId);
}
