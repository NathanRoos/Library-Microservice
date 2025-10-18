package com.nathanroos.library.libraryworkerssubdomain.BussinessLayer;


import com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer.Librarian;
import com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer.LibrarianIdentifier;
import com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer.LibrarianRepository;
import com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer.WorkerAddress;
import com.nathanroos.library.libraryworkerssubdomain.MappingLayer.LibrarianRequestMapper;
import com.nathanroos.library.libraryworkerssubdomain.MappingLayer.LibrarianResponseMapper;
import com.nathanroos.library.libraryworkerssubdomain.PresentationLayer.LibrarianRequestModel;
import com.nathanroos.library.libraryworkerssubdomain.PresentationLayer.LibrarianResponseModel;
import com.nathanroos.library.libraryworkerssubdomain.utils.Exceptions.InvalidInputException;
import com.nathanroos.library.libraryworkerssubdomain.utils.Exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class LibrarianServiceImpl implements LibrarianService{

    private final LibrarianRepository librarianRepository;

//    private final LoanService

    private final LibrarianResponseMapper librarianResponseMapper;

    private final LibrarianRequestMapper librarianRequestMapper;

    public LibrarianServiceImpl(LibrarianRepository librarianRepository, LibrarianResponseMapper librarianResponseMapper, LibrarianRequestMapper librarianRequestMapper) {
        this.librarianRepository = librarianRepository;
        this.librarianResponseMapper = librarianResponseMapper;
        this.librarianRequestMapper = librarianRequestMapper;
    }

    @Override
    public List<LibrarianResponseModel> getWorkers() {
        List<Librarian> workers = librarianRepository.findAll();
        return librarianResponseMapper.entityListToResponseModelList(workers);
    }

    @Override
    public LibrarianResponseModel getLibrarianByLibrarianId(String librarianId) {
        Librarian librarian = getLibrarianObjectById(librarianId);

        return librarianResponseMapper.entityToResponseModel(librarian);
    }

    @Override
    public LibrarianResponseModel addLibrarian(LibrarianRequestModel librarian)
    {



        Librarian librarianNew = librarianRequestMapper.requestModelToEntity(
                librarian, new LibrarianIdentifier()
        );

        validateLibrarianRequestModel(librarian);

        Librarian createdLibrarian = librarianRepository.save(librarianNew);
        return librarianResponseMapper.entityToResponseModel(createdLibrarian);
    }

    @Override
    public LibrarianResponseModel updateLibrarian(LibrarianRequestModel updateLibrarian, String librarianId)
    {
        Librarian librarian = getLibrarianObjectById(librarianId);

        validateLibrarianRequestModel(updateLibrarian);

        WorkerAddress address = new WorkerAddress(updateLibrarian.getLibraryWorkerAddress().getStreetNumber(), updateLibrarian.getLibraryWorkerAddress().getStreetName(), updateLibrarian.getLibraryWorkerAddress().getCity(), updateLibrarian.getLibraryWorkerAddress().getProvince(), updateLibrarian.getLibraryWorkerAddress().getPostalCode());

        librarian.setLibrarianPhoneNumber(updateLibrarian.getLibrarianPhoneNumber());
        librarian.setEmail(updateLibrarian.getEmail());
        librarian.setFirstname(updateLibrarian.getFirstname());
        librarian.setLastname(updateLibrarian.getLastname());
        librarian.setPosition(updateLibrarian.getPosition());
        librarian.setLibraryWorkerAddress(updateLibrarian.getLibraryWorkerAddress());
        librarian.setLibraryWorkerAddress(address);

        librarianRepository.save(librarian);

        return librarianResponseMapper.entityToResponseModel(librarian);
    }

    @Override
    public void removeLibrarian(String librarianId) {

        Librarian existingLibrarian = getLibrarianObjectById(librarianId);

        librarianRepository.delete(existingLibrarian);

    }

    private void validateLibrarianRequestModel(LibrarianRequestModel model) {
        if (model.getFirstname() == null || model.getFirstname().isBlank())
            throw new InvalidInputException("Invalid firstName: " + model.getFirstname());

        if (model.getLastname() == null || model.getLastname().isBlank())
            throw new InvalidInputException("Invalid lastName: " + model.getLastname());

        if (model.getEmail() == null || model.getEmail().isBlank())
            throw new InvalidInputException("Invalid email: " + model.getEmail());

        if (model.getLibrarianPhoneNumber() == null)
            throw new InvalidInputException("Phone number must be provided.");

        if (model.getPosition() == null)
            throw new InvalidInputException("Position must be provided.");
    }

    private Librarian getLibrarianObjectById(String librarianId) {
        try {
            UUID.fromString(librarianId);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid librarianId: " + librarianId);
        }

        Librarian account = this.librarianRepository.findAllByLibrarianIdentifier_LibrarianId(librarianId);

        if (account == null) {
            throw new NotFoundException("Unknown librarianId: " + librarianId);
        }

        return account;
    }
}
