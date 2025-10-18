package com.nathanroos.library.libraryworkerssubdomain.DataAccessLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Embeddable
public class Position {

    @Column(name = "position_name")
    @Enumerated(EnumType.STRING)
    public PositionEnum positionTitle;

    public Position(PositionEnum positionEnum) {
        this.positionTitle = positionEnum;
    }
}
