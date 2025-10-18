package com.nathanroos.library.apigateway.presentationlayer.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LibraryAccountResponseModel extends RepresentationModel<LibraryAccountResponseModel> {

    String firstname;
    String lastname;
    String phoneNumber;
    String email;
    String accountId;


}
