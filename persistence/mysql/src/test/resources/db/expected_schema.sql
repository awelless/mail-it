CREATE TABLE DATABASECHANGELOG
(
    ID            VARCHAR(255) NOT NULL,
    AUTHOR        VARCHAR(255) NOT NULL,
    FILENAME      VARCHAR(255) NOT NULL,
    DATEEXECUTED  DATETIME     NOT NULL,
    ORDEREXECUTED INT          NOT NULL,
    EXECTYPE      VARCHAR(10)  NOT NULL,
    MD5SUM        VARCHAR(35),
    DESCRIPTION   VARCHAR(255),
    COMMENTS      VARCHAR(255),
    TAG           VARCHAR(255),
    LIQUIBASE     VARCHAR(20),
    CONTEXTS      VARCHAR(255),
    LABELS        VARCHAR(255),
    DEPLOYMENT_ID VARCHAR(10)
);

CREATE TABLE DATABASECHANGELOGLOCK
(
    ID          INT PRIMARY KEY,
    LOCKED      BOOLEAN NOT NULL,
    LOCKGRANTED DATETIME,
    LOCKEDBY    VARCHAR(255)
);

CREATE TABLE mail_message_type
(
    mail_message_type_id BIGINT PRIMARY KEY,
    name                 VARCHAR(128) NOT NULL,
    description          VARCHAR(1024),
    max_retries_count    INT,
    state                VARCHAR(32)  NOT NULL,
    created_at           TIMESTAMP    NOT NULL,
    updated_at           TIMESTAMP    NOT NULL,
    content_type         VARCHAR(32)  NOT NULL,
    template_engine      VARCHAR(64),
    CONSTRAINT uk_mail_message_type_name UNIQUE (name)
);

CREATE TABLE mail_message_template
(
    mail_message_type_id BIGINT PRIMARY KEY,
    template             BLOB NOT NULL,
    CONSTRAINT fk_mailmessagetemplate_mailmessagetype FOREIGN KEY (mail_message_type_id) REFERENCES mail_message_type (mail_message_type_id)
);

CREATE TABLE mail_message
(
    mail_message_id      BIGINT PRIMARY KEY,
    text                 TEXT,
    data                 BLOB,
    subject              VARCHAR(256),
    email_from           VARCHAR(256),
    email_to             VARCHAR(256) NOT NULL,
    mail_message_type_id BIGINT       NOT NULL,
    created_at           TIMESTAMP    NOT NULL,
    sending_started_at   TIMESTAMP,
    sent_at              TIMESTAMP,
    status               VARCHAR(32)  NOT NULL,
    failed_count         INT          NOT NULL,
    deduplication_id     VARCHAR(128),
    CONSTRAINT fk_mailmessage_mailmessagetype FOREIGN KEY (mail_message_type_id) REFERENCES mail_message_type (mail_message_type_id),
    CONSTRAINT uk_mailmessage_deduplicationid UNIQUE (deduplication_id)
);

CREATE TABLE instance_id_locks
(
    instance_id    INT PRIMARY KEY,
    acquired_until TIMESTAMP   NOT NULL,
    identity_key   VARCHAR(36) NOT NULL
);

CREATE TABLE application
(
    application_id BIGINT PRIMARY KEY,
    name           VARCHAR(128) NOT NULL,
    state          VARCHAR(32)  NOT NULL,
    CONSTRAINT uk_application_name UNIQUE (name)
);

CREATE TABLE api_key
(
    api_key_id     VARCHAR(32) PRIMARY KEY,
    name           VARCHAR(128) NOT NULL,
    secret         VARCHAR(64)  NOT NULL,
    application_id BIGINT       NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    expires_at     TIMESTAMP    NOT NULL,
    CONSTRAINT fk_apikey_application FOREIGN KEY (application_id) REFERENCES application (application_id)
);
