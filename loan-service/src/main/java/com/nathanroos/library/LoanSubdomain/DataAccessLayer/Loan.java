package com.nathanroos.library.LoanSubdomain.DataAccessLayer;

import com.nathanroos.library.LoanSubdomain.domainclientlayer.Customer.CustomerModel;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.Library.LibraryModel;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.LibraryWorker.LibraryWorkerModel;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "sales")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Loan {

    @Id
    private String id;

    private LoanIdentifier loanIdentifier;

    private CustomerModel libraryAccountIdentifier;

    private LibraryWorkerModel librarianIdentifier;

    private LibraryModel bookIdentifier;

//    @Column(name = "title")
//    public String title;
//    @Column(name = "author")
//    public String author;
//    @Column(name = "genre")
//    public GenreEnum genre;
    private LoanStatusEnum loanStatus;
    private Date loanDate;
    private Date dueDate;

//    public Loan(@NotNull String id, @NotNull LoanIdentifier loanIdentifier, @NotNull CustomerModel libraryAccountIdentifier,
//                @NotNull LibraryWorkerModel librarianIdentifier, @NotNull LibraryModel bookIdentifier,
////                @NotNull String title, @NotNull String author, @NotNull GenreEnum genre,
//                @NotNull LoanStatusEnum loanStatus, @NotNull Date loanDate, @NotNull Date dueDate)
//    {
//        this.id = id;
//        this.loanIdentifier = loanIdentifier;
//        this.libraryAccountIdentifier = libraryAccountIdentifier;
//        this.librarianIdentifier = librarianIdentifier;
//        this.bookIdentifier = bookIdentifier;
////        this.title = title;
////        this.author = author;
////        this.genre = genre;
//        this.loanStatus = loanStatus;
//        this.loanDate = loanDate;
//        this.dueDate = dueDate;
//    }
}
