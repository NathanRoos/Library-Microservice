package com.nathanroos.library.LoanSubdomain.domainclientlayer.Customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerModel {

    String accountId;
    String firstname;
    String lastname;


}
