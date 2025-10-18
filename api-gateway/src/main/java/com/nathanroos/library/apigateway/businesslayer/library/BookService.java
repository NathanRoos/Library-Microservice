package com.nathanroos.library.apigateway.businesslayer.library;


import com.nathanroos.library.apigateway.presentationlayer.library.BookRequestModel;
import com.nathanroos.library.apigateway.presentationlayer.library.BookResponseModel;
import org.springframework.stereotype.Service;

import java.util.List;


public interface BookService {

    List<BookResponseModel> getBooks();
    BookResponseModel getBookByBookId(String bookId);
    BookResponseModel addBook(BookRequestModel book);
    BookResponseModel updateBook(BookRequestModel book, String bookId);
    void removeBook(String bookId);
}
