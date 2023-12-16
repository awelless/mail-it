CREATE TABLE databasechangelog
(
    id            VARCHAR(255) NOT NULL,
    author        VARCHAR(255) NOT NULL,
    filename      VARCHAR(255) NOT NULL,
    dateexecuted  TIMESTAMP    NOT NULL,
    orderexecuted INT          NOT NULL,
    exectype      VARCHAR(10)  NOT NULL,
    md5sum        VARCHAR(35),
    description   VARCHAR(255),
    comments      VARCHAR(255),
    tag           VARCHAR(255),
    liquibase     VARCHAR(20),
    contexts      VARCHAR(255),
    labels        VARCHAR(255),
    deployment_id VARCHAR(10)
);

CREATE TABLE databasechangeloglock
(
    id          INT PRIMARY KEY,
    locked      BOOLEAN NOT NULL,
    lockgranted TIMESTAMP,
    lockedby    VARCHAR(255)
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
    template             bytea NOT NULL,
    CONSTRAINT fk_mailmessagetemplate_mailmessagetype FOREIGN KEY (mail_message_type_id) REFERENCES mail_message_type (mail_message_type_id)
);

CREATE TABLE mail_message
(
    mail_message_id      BIGINT PRIMARY KEY,
    text                 TEXT,
    data                 bytea,
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

CREATE TABLE server_lease_locks
(
    server_id      INT PRIMARY KEY,
    acquired_until TIMESTAMP   NOT NULL,
    identity_key   VARCHAR(36) NOT NULL
);

CREATE TABLE api_key
(
    api_key_id VARCHAR(32) PRIMARY KEY,
    name       VARCHAR(128) NOT NULL,
    secret     VARCHAR(64)  NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    CONSTRAINT uk_apikey_name UNIQUE (name)
);
