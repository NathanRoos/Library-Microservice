package com.nathanroos.library.LoanSubdomain.DataAccessLayer;


import lombok.Getter;

import java.util.UUID;

@Getter
public class LoanIdentifier {

    private String loanId;

    public LoanIdentifier() {
        this.loanId = UUID.randomUUID().toString();
    }

    public LoanIdentifier(String loanId) {
        this.loanId = loanId;
    }

}
