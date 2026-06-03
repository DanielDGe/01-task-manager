create table if not exists tasks (
    id bigserial primary key,
    completed boolean not null,
    description varchar(255),
    title varchar(100)
);