CREATE TABLE IF NOT EXISTS event
(
    id                 VARCHAR(36)  NOT NULL,
    title              VARCHAR(256) NOT NULL,
    start_time         TIMESTAMP    NOT NULL,
    end_time           TIMESTAMP    NOT NULL,
    created_date       TIMESTAMP,
    last_modified_date TIMESTAMP,
    PRIMARY KEY (id)
);