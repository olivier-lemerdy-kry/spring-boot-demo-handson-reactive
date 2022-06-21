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

CREATE TABLE IF NOT EXISTS person
(
    id                 VARCHAR(36)  NOT NULL,
    name               VARCHAR(256) NOT NULL,
    created_date       TIMESTAMP,
    last_modified_date TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS participants
(
    event_id  VARCHAR(36) NOT NULL,
    person_id VARCHAR(36) NOT NULL,
    FOREIGN KEY (event_id) REFERENCES event (id),
    FOREIGN KEY (person_id) REFERENCES person (id),
    UNIQUE (event_id, person_id)
);