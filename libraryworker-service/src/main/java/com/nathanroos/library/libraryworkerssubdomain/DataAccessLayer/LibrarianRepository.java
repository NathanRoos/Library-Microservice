package com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface LibrarianRepository extends JpaRepository<Librarian, Integer> {

    Librarian findAllByLibrarianIdentifier_LibrarianId(String librarianId);


}
