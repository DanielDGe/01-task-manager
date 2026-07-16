alter table tasks
add column owner_username varchar(100);

update tasks
set owner_username = 'daniel'
where owner_username is null;

alter table tasks
alter column owner_username set not null;

create index idx_tasks_owner_username
on tasks(owner_username);