CREATE TABLE event_stream (
  id bigserial,
  aggregate_root_type_name varchar(256),
  aggregate_root_id varchar(36),
  version integer,
  command_id varchar(36),
  gmt_create date,
  events text,
  PRIMARY KEY (id),
  CONSTRAINT uk_aggregate_root_id_version UNIQUE (aggregate_root_id, version),
  CONSTRAINT uk_aggregate_root_id_command_id UNIQUE (aggregate_root_id, command_id)
);