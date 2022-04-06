CREATE TABLE event
(
    id    VARCHAR(36)  NOT NULL,
    title VARCHAR(128) NOT NULL,
    start TIMESTAMP    NOT NULL,
    end   TIMESTAMP    NOT NULL,
    PRIMARY KEY (id)
);