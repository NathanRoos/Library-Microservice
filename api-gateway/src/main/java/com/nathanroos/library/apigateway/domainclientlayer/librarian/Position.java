package com.nathanroos.library.apigateway.domainclientlayer.librarian;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;

@Builder
public class Position {

    @Column(name = "position_name")
    @Enumerated(EnumType.STRING)
    public PositionEnum positionTitle;

}
