CREATE SCHEMA IF NOT EXISTS datafirewall;

CREATE TABLE IF NOT EXISTS datafirewall.sql_expressions (
	id int8 NOT NULL,
	sql text NOT NULL,
	source_name text NULL,
	CONSTRAINT actions_rep_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS datafirewall.jwt_token_registry (
    jti uuid primary key,
    service varchar(255) not null,
    subject varchar(255) not null,
    issued_at timestamptz not null,
    expires_at timestamptz not null,
    revoked_at timestamptz
);

create index idx_jwt_token_registry_expires_at
    on datafirewall.jwt_token_registry (expires_at);

create index idx_jwt_token_registry_service_active
    on jwt_token_registry (service)
    where revoked_at is null;
