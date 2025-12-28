CREATE SCHEMA IF NOT EXISTS datafirewall;

CREATE TABLE IF NOT EXISTS datafirewall.sql_expressions (
	id int8 NOT NULL,
	sql text NOT NULL,
	source_name text NULL,
	CONSTRAINT actions_rep_pkey PRIMARY KEY (id)
);
