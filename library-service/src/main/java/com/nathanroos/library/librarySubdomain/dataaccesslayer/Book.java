package com.nathanroos.library.librarySubdomain.dataaccesslayer;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;


@Entity
@Table(name="books")
@Data
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; //private identifier

    @Embedded
    private BookIdentifier bookIdentifier; //public identifier

    private String title;
    private String author;

    @Column(name = "copies_available")
    private Integer copiesAvailable;

    @Column(name = "genre")
    @Enumerated(EnumType.STRING)
    private GenreEnum genre;

    private String firstname;
    private String lastname;

    public Book(@NotNull String title, @NotNull String author, @NotNull Integer copiesAvailable, @NotNull GenreEnum genre) {
        this.bookIdentifier = new BookIdentifier();
        this.title = title;
        this.author = author;
        this.copiesAvailable = copiesAvailable;
        this.genre = genre;
    }

}
