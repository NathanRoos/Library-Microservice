package com.nathanroos.library.LoanSubdomain.domainclientlayer.Library;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LibraryModel {

    String bookId;
    String title;
    String author;


}
