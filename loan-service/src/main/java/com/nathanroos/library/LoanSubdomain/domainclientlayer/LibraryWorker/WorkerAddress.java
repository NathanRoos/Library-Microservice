package com.nathanroos.library.LoanSubdomain.domainclientlayer.LibraryWorker;

import lombok.Getter;


@Getter
public class WorkerAddress {
    private String streetNumber;

    private String streetName;

    private String city;

    private ProvinceEnum province;

    private String postalCode;

    public WorkerAddress(String streetNumber, String streetName, String city, ProvinceEnum province, String postalCode) {
        this.streetNumber = streetNumber;
        this.streetName = streetName;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
    }

    public WorkerAddress() {}

}
