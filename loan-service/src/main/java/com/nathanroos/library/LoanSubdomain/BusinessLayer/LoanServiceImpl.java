package com.nathanroos.library.LoanSubdomain.BusinessLayer;

import com.nathanroos.library.LoanSubdomain.DataAccessLayer.Loan;
import com.nathanroos.library.LoanSubdomain.DataAccessLayer.LoanIdentifier;
import com.nathanroos.library.LoanSubdomain.DataAccessLayer.LoanRepository;
import com.nathanroos.library.LoanSubdomain.MapingLayer.LoanRequestMapper;
import com.nathanroos.library.LoanSubdomain.MapingLayer.LoanResponseMapper;
import com.nathanroos.library.LoanSubdomain.PresentationLayer.LoanRequestModel;
import com.nathanroos.library.LoanSubdomain.PresentationLayer.LoanResponseModel;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.Customer.CustomerModel;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.Customer.CustomerServiceClient;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.Library.LibraryModel;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.Library.LibraryServiceClient;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.LibraryWorker.LibrarianServiceClient;
import com.nathanroos.library.LoanSubdomain.domainclientlayer.LibraryWorker.LibraryWorkerModel;
import com.nathanroos.library.LoanSubdomain.utils.Exceptions.InvalidInputException;
import com.nathanroos.library.LoanSubdomain.utils.Exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class LoanServiceImpl implements LoanService{

    private final LoanRepository loanRepository;

    private final LoanRequestMapper loanRequestMapper;

    private final LoanResponseMapper loanResponseMapper;

    private final LibraryServiceClient LibraryService;

    private final LibrarianServiceClient librarianService;

    private final CustomerServiceClient customerService;

    private static final int EXPECTED_UUID_LENGTH = 36;


    public LoanServiceImpl(LoanRepository loanRepository, LoanRequestMapper loanRequestMapper, LoanResponseMapper loanResponseMapper, LibraryServiceClient libraryService, LibrarianServiceClient librarianService, CustomerServiceClient customerService) {
        this.loanRepository = loanRepository;
        this.loanRequestMapper = loanRequestMapper;
        this.loanResponseMapper = loanResponseMapper;
        this.LibraryService = libraryService;
        this.librarianService = librarianService;
        this.customerService = customerService;
    }

    public List<LoanResponseModel> getAllLoansByAccountId(String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new InvalidInputException("Account ID cannot be null or empty.");
        }

        CustomerModel account = customerService.getAccountByAccountId(accountId);
        if (account == null) {
            throw new NotFoundException("The Id is not found:" + accountId);
        }

        List<Loan> loan = loanRepository.findAllByLibraryAccountIdentifier_AccountId(accountId);
        if (loan == null) {
            loan = Collections.emptyList();  // Safe fallback
        }

        return loanResponseMapper.entityListToResponseModelList(loan);
    }



//    @Override
//    public List<LoanResponseModel> getLoans() {
//        List<Loan> loans = loanRepository.findAll();
//
////        List<LoanResponseModel> loanResponseModels = new ArrayList<>();
//
////        for(Loan loan : loans){
////            CustomerModel account = customerService.getAccountByAccountId(loan.getLibraryAccountIdentifier().getAccountId());
////            LibraryModel book = LibraryService.getBookByBookId(loan.getBookIdentifier().getBookId());
////            LibraryWorkerModel librarian = librarianService.getLibrarian(loan.getLibrarianIdentifier().getLibrarianId());
//
////            LoanResponseModel responseModel = loanResponseMapper.entityToResponseModel(loan);
////            responseModel.setAuthor(book.getAuthor());
////            responseModel.setBookId(book.getBookId());
////
////            responseModel.setTitle(book.getTitle());
////            responseModel.setCustomer_firstname(account.getCustomer_firstname());
////            responseModel.setCustomer_lastname(account.getCustomer_lastname());
////            responseModel.setLibrarian_firstname(librarian.getLibrarian_firstname());
////            responseModel.setLibrarian_lastname(librarian.getLibrarian_lastname());
//
////            loanResponseModels.add(responseModel);
////        }
//
//        return loanResponseMapper.entityListToResponseModelList(loans);
//    }

    @Override
    public LoanResponseModel getLoanByLoanId(String accountId, String loanId) {
        Loan loan = loanRepository.findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId(accountId, loanId);

        if(loan == null){
            throw new NotFoundException("The Id is not found:" + loanId);
        }




//        LibraryModel book = LibraryService.getBookByBookId(loan.getBookIdentifier().getBookId());
//
//        LibraryWorkerModel librarian = librarianService.getLibrarian(loan.getLibrarianIdentifier().getLibrarianId());
//
//        CustomerModel account = customerService.getAccountByAccountId(loan.getLibraryAccountIdentifier().getAccountId());




        return loanResponseMapper.entityToResponseModel(loan);
    }

    public List<LoanResponseModel> getAllLoanByLoanId(String loanId) {
        List<Loan> loan = loanRepository.findAllByLoanIdentifier_LoanId(loanId);

        if(loan == null){
            throw new NotFoundException("The Id is not found:" + loanId);
        }



        for(Loan loan5 : loan){
            loan5.setLibraryAccountIdentifier(customerService.getAccountByAccountId(loan5.getLibraryAccountIdentifier().getAccountId()));
            loan5.setBookIdentifier(LibraryService.getBookByBookId(loan5.getBookIdentifier().getBookId()));
            loan5.setLibrarianIdentifier(librarianService.getLibrarian(loan5.getLibrarianIdentifier().getLibrarianId()));
        }




//        LibraryModel book = LibraryService.getBookByBookId(loan.getBookIdentifier().getBookId());
//
//        LibraryWorkerModel librarian = librarianService.getLibrarian(loan.getLibrarianIdentifier().getLibrarianId());
//
//        CustomerModel account = customerService.getAccountByAccountId(loan.getLibraryAccountIdentifier().getAccountId());


        return loanResponseMapper.entityListToResponseModelList(loan);
    }

    @Override
    public LoanResponseModel addLoan(LoanRequestModel loan, String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new InvalidInputException("Account ID cannot be null or empty.");
        }

        if (loan == null) {
            throw new InvalidInputException("Loan request cannot be null.");
        }

        if (loan.getAccountId() == null || loan.getAccountId().trim().isEmpty()) {
            throw new InvalidInputException("Loan request accountId cannot be null or empty.");
        }

        if (!accountId.equals(loan.getAccountId())) {
            throw new InvalidInputException("Account ID in path and body do not match.");
        }

        if (loan.getBookId() == null || loan.getBookId().trim().isEmpty() ||
                loan.getLibrarianId() == null || loan.getLibrarianId().trim().isEmpty()) {
            throw new InvalidInputException("Missing required fields: bookId and/or librarianId.");
        }

        // Now fetch the external dependencies safely
        CustomerModel customerResponseModel = customerService.getAccountByAccountId(accountId);
        if (customerResponseModel == null) {
            throw new NotFoundException("Customer not found for accountId: " + accountId);
        }

        LibraryModel libraryResponseModel = LibraryService.getBookByBookId(loan.getBookId());
        if (libraryResponseModel == null) {
            throw new NotFoundException("Book not found for bookId: " + loan.getBookId());
        }

        LibraryWorkerModel libraryWorkerResponseModel = librarianService.getLibrarian(loan.getLibrarianId());
        if (libraryWorkerResponseModel == null) {
            throw new NotFoundException("Librarian not found for librarianId: " + loan.getLibrarianId());
        }

        log.debug("customer-name is " + customerResponseModel.getFirstname() + " " + customerResponseModel.getLastname());
        log.debug("Librarian-name is " + libraryWorkerResponseModel.getFirstname() + " " + libraryWorkerResponseModel.getLastname());

        LoanIdentifier loanIdentifier = new LoanIdentifier();
        Loan loanToCreate = loanRequestMapper.requestModelToEntity(
                loan, loanIdentifier, customerResponseModel, libraryWorkerResponseModel, libraryResponseModel
        );

        Loan loanSaved = loanRepository.save(loanToCreate);
        return loanResponseMapper.entityToResponseModel(loanSaved);
    }


    @Override
    public LoanResponseModel updateLoan(String accountId, LoanRequestModel loanRequestModel, String loanId) {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new InvalidInputException("Account ID cannot be null or empty.");
        }

        if (loanRequestModel == null) {
            throw new InvalidInputException("Loan request cannot be null.");
        }

        if (loanRequestModel.getAccountId() == null || loanRequestModel.getAccountId().trim().isEmpty()) {
            throw new InvalidInputException("Loan request accountId cannot be null or empty.");
        }

        if (loanRequestModel.getBookId() == null || loanRequestModel.getBookId().trim().isEmpty()) {
            throw new InvalidInputException("Loan request bookId cannot be null or empty.");
        }

        if (loanRequestModel.getLibrarianId() == null || loanRequestModel.getLibrarianId().trim().isEmpty()) {
            throw new InvalidInputException("Loan request librarianId cannot be null or empty.");
        }

        // 🔥 Validate loanId format (UUID check)
        try {
            UUID.fromString(loanId);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("The following Id is not the correct length: " + loanId);
        }

        // Validate external dependencies
        CustomerModel customerModel = customerService.getAccountByAccountId(accountId);
        if (customerModel == null) {
            throw new NotFoundException("Customer not found: " + accountId);
        }

        LibraryWorkerModel libraryWorkerModel = librarianService.getLibrarian(loanRequestModel.getLibrarianId());
        if (libraryWorkerModel == null) {
            throw new NotFoundException("Librarian not found: " + loanRequestModel.getLibrarianId());
        }

        LibraryModel libraryModel = LibraryService.getBookByBookId(loanRequestModel.getBookId());
        if (libraryModel == null) {
            throw new NotFoundException("Book not found: " + loanRequestModel.getBookId());
        }

        // Find existing loan
        Loan loan = loanRepository.findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId(accountId, loanId);
        if (loan == null) {
            throw new NotFoundException("The loan ID is not found: " + loanId);
        }

        // Update fields
        loan.setLoanIdentifier(new LoanIdentifier(loanId));
        loan.setLoanStatus(loanRequestModel.getLoanStatus());
        loan.setLoanDate(loanRequestModel.getLoanDate());
        loan.setDueDate(loanRequestModel.getDueDate());
        loan.setLibraryAccountIdentifier(customerModel);
        loan.setLibrarianIdentifier(libraryWorkerModel);
        loan.setBookIdentifier(libraryModel);

        Loan loanUpdated = loanRepository.save(loan);
        return loanResponseMapper.entityToResponseModel(loanUpdated);
    }




    @Override
    public void removeLoan(String accountId, String loanId) {
        if (loanId == null || loanId.trim().isEmpty() || loanId.length() != EXPECTED_UUID_LENGTH) {
            throw new InvalidInputException("The following Id is not the correct length: " + loanId);
        }

        Loan existingLoan = loanRepository.findLoanByLibraryAccountIdentifier_AccountIdAndLoanIdentifier_LoanId(accountId, loanId);

        if (existingLoan == null) {
            throw new NotFoundException("Provided loanId not found: " + loanId);
        }

        loanRepository.delete(existingLoan);
    }

}
