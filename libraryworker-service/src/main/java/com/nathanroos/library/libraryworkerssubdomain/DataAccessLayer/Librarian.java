package com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

@Entity
@Table(name="librarians")
@Data
@NoArgsConstructor
public class Librarian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private LibrarianIdentifier librarianIdentifier;

    private String firstname;
    private String lastname;
    @Column(name = "email")
    private String email;
//    @ElementCollection(fetch = FetchType.LAZY)
//    @CollectionTable(name = "booksLoaned",
//            joinColumns = @JoinColumn(name = "loanIdentifier", referencedColumnName = "loanIdentifier"))
//    public List<Loan> booksLoaned;

    @Embedded
    private WorkerAddress libraryWorkerAddress;

    @Embedded
    private Position position;

    @Embedded
//    @Column(name = "librarianPhoneNumber")
    private LibrarianPhoneNumber librarianPhoneNumber;

    public Librarian(@NotNull String firstName, @NotNull String lastName, @NotNull String email,
                     @NotNull WorkerAddress employeeAddress,
                     @NotNull Position position, @NotNull LibrarianPhoneNumber librarianPhoneNumber,
                     @NotNull LibrarianIdentifier librarianIdentifier) {
        this.firstname = firstName;
        this.lastname = lastName;
        this.email = email;
//        this.booksLoaned = booksLoaned;
        this.libraryWorkerAddress = employeeAddress;
        this.position = position;
        this.librarianPhoneNumber = librarianPhoneNumber;
        this.librarianIdentifier = librarianIdentifier;
    }
}
