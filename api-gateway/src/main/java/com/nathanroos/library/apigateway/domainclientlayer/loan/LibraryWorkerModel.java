package com.nathanroos.library.apigateway.domainclientlayer.loan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LibraryWorkerModel {

    private String librarianId;
    private String librarian_firstname;
    private String librarian_lastname;

}
