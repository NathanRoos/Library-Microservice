DROP TABLE IF EXISTS books;

CREATE TABLE books
(
    id              INT AUTO_INCREMENT PRIMARY KEY,
    book_id         VARCHAR(100) UNIQUE NOT NULL,
    title           VARCHAR(255)        NOT NULL,
    author          VARCHAR(255)        NOT NULL,
    copies_available INT                 NOT NULL,
    genre           ENUM ('FANTASY',
        'FICTION',

        'SCIENCE_FICTION',

        'ROMANCE',

        'MYSTERY',

        'THRILLER',

        'HORROR',

        'DRAMA',

        'COMEDY',

        'ACTION',

        'ADVENTURE',

        'BIOGRAPHY',

        'HISTORICAL',

        'POETRY', 'NON_FICTION'),
    firstname       VARCHAR(255),
    lastname        VARCHAR(255),
    image_url       VARCHAR(500)
);