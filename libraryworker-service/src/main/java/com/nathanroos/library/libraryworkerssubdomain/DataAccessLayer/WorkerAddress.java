package com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer;

import jakarta.persistence.*;
import lombok.Getter;

@Embeddable
@Table(name = "worker_addresses")
@Getter
public class WorkerAddress {
    @Column(name = "streetnumber")
    private String streetNumber;

    @Column(name = "streetname")
    private String streetName;

    @Column(name = "city")
    private String city;

    @Enumerated(EnumType.STRING)
    private ProvinceEnum province;

    @Column(name = "postal_code")
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
