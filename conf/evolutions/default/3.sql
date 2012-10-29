# Default Schema

# --- !Ups

alter table model add column if not exists download_count int default 0;

# --- !Downs

alter table model drop column if exists download_count;
