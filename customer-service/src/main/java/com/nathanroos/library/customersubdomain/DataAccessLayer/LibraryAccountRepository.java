package com.nathanroos.library.customersubdomain.DataAccessLayer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryAccountRepository extends JpaRepository<LibraryAccount, Integer> {

    LibraryAccount findLibraryAccountByLibraryAccountIdentifier_AccountId(String accountId);

}
