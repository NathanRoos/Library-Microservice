drop table if exists librarians;
CREATE TABLE librarians
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    librarian_id  VARCHAR(100) UNIQUE NOT NULL,
    firstname     VARCHAR(255)        NOT NULL,
    lastname      VARCHAR(255)        NOT NULL,
    email         VARCHAR(255)        NOT NULL,
    phonenumber   VARCHAR(20)         NOT NULL,
    phonetype     ENUM ('MOBILE','WORK','HOME'),
    position_name ENUM ('LIBRARY_CLERK','ASSISTANT','ARCHIVIST'),
    streetnumber  VARCHAR(255),
    streetname    VARCHAR(255),
    city          VARCHAR(255),
    province      ENUM ('QUEBEC','ONTARIO','BRITISH_COLUMBIA',
        'ALBERTA','SASKATCHEWAN','MANITOBA','NEW_BRUNSWICK',
        'NEWFOUNDLAND','NOVA_SCOTIA', 'PRINCE_EDWARD_ISLAND'),
    postal_code   VARCHAR(20)         NOT NULL
);