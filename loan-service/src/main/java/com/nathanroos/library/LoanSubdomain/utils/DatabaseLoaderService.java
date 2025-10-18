package com.nathanroos.library.LoanSubdomain.utils;

import com.nathanroos.library.LoanSubdomain.DataAccessLayer.Loan;
import com.nathanroos.library.LoanSubdomain.DataAccessLayer.LoanIdentifier;
import com.nathanroos.library.LoanSubdomain.DataAccessLayer.LoanRepository;
import com.nathanroos.library.LoanSubdomain.DataAccessLayer.LoanStatusEnum;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.Customer.CustomerModel;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.Library.LibraryModel;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.LibraryWorker.LibraryWorkerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseLoaderService implements CommandLineRunner {

    @Autowired
    LoanRepository loanRepository;

    @Override
    public void run(String... args) throws Exception {

        var loanIdentifier = new LoanIdentifier();


        var libraryModel = LibraryModel.builder()
                .bookId("550e8400-e29b-41d4-a716-446655440000")
                .title("1984")
                .author("George Orwell")
                .build();
        var libraryWorkerModel = LibraryWorkerModel.builder()
                .librarianId("1a2b3c4d-e29b-41d4-a716-446655440000")
                .firstname("Alice")
                .lastname("Johnson")
                .build();

        var customerModel = CustomerModel.builder()
                .accountId("84a8ec6e-2fdc-4c6d-940f-9b2274d44420")
                .firstname("John")
                .lastname("Doe")
                .build();

        var loan1 = Loan.builder()
                .id("1")
                .loanIdentifier(loanIdentifier)
                .libraryAccountIdentifier(customerModel)
                .librarianIdentifier(libraryWorkerModel)
                .bookIdentifier(libraryModel)
                .dueDate(new java.util.Date())
                .loanDate(new java.util.Date())
                .loanStatus(LoanStatusEnum.ACTIVE)
                .build();

        loanRepository.save(loan1);

    }
}