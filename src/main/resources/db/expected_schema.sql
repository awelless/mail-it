CREATE TABLE mail_message_type
(
    mail_message_type_id BIGINT PRIMARY KEY,
    name                 VARCHAR(128) NOT NULL,
    description          VARCHAR(1024),
    max_retries_count    INT,
    state                VARCHAR(32)  NOT NULL,
    content_type         VARCHAR(32)  NOT NULL,
    template_engine      VARCHAR(64),
    template             TEXT,
    CONSTRAINT uk_mail_message_type_name UNIQUE (name)
);

CREATE TABLE mail_message
(
    mail_message_id      BIGINT PRIMARY KEY,
    text                 TEXT,
    data                 BLOB,
    subject              VARCHAR(256),
    email_from           VARCHAR(256),
    email_to             VARCHAR(256)  NOT NULL,
    mail_message_type_id BIGINT        NOT NULL,
    created_at           TIMESTAMP     NOT NULL,
    sending_started_at   TIMESTAMP,
    sent_at              TIMESTAMP,
    status               VARCHAR(32)   NOT NULL,
    failed_count         INT           NOT NULL,
    CONSTRAINT fk_mailmessage_mailmessagetype FOREIGN KEY (mail_message_type_id) REFERENCES mail_message_type (mail_message_type_id)
);
