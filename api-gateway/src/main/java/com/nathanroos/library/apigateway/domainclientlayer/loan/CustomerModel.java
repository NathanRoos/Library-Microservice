package com.nathanroos.library.apigateway.domainclientlayer.loan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerModel {

    String accountId;
    String customer_firstname;
    String customer_lastname;

}
