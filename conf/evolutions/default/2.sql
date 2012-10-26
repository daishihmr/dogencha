# Default Schema

# --- !Ups

alter table model add column if not exists publication int default 0;

# --- !Downs

alter table model drop column if exists publication;
