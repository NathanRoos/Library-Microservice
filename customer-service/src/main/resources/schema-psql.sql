drop table if exists library_accounts;

CREATE TABLE library_accounts
(
    id SERIAL PRIMARY KEY,
    customer_id VARCHAR(36)  NOT NULL,
    phonenumber VARCHAR(50)  NOT NULL,
    firstname   VARCHAR(100) NOT NULL,
    lastname    VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL
);


