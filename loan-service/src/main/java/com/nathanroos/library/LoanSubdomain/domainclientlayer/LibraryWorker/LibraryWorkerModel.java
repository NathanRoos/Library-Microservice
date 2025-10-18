package com.nathanroos.library.LoanSubdomain.domainclientlayer.LibraryWorker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LibraryWorkerModel {

    private String librarianId;
    private String firstname;
    private String lastname;



}
