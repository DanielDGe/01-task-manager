alter table tasks
add column created_at timestamp not null default now();

alter table tasks
add column updated_at timestamp not null default now();