package com.nathanroos.library.customersubdomain.DataAccessLayer;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

@Entity
@Table(name="libraryAccounts")
@Data
@NoArgsConstructor
public class LibraryAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private LibraryAccountIdentifier libraryAccountIdentifier;

//    @Column(name = "booksBorrowed")
//    private List<Book> booksBorrowed;
//    @Column(name = "dueDates")
//    private List<Date> dueDates;
//    @Column(name = "loanStatus")
//    private List<LoanStatusEnum> loanStatus;
    @Column(name = "phonenumber")
    private String phoneNumber;
    private String firstname;
    private String lastname;
//    @Column(name = "contactPreference")
//    private ContactPreferenceEnum contactPreference;
    @Column(name = "email")
    private String email;


    public LibraryAccount(@NotNull String phoneNumber, @NotNull String firstName, @NotNull String lastName, @NotNull String email) {
        this.phoneNumber = phoneNumber;
        this.firstname = firstName;
        this.lastname = lastName;
//        this.contactPreference = contactPreference;
        this.email = email;
    }




}
