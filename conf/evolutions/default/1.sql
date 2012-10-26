# Default Schema

# --- !Ups

create table model (
    id identity primary key,
    root_file varchar(512) unique not null,
    name varchar(100),
    author varchar(100) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    last_accessed_at timestamp not null,
    comment varchar(1000)
);

# --- !Downs

drop table model;
