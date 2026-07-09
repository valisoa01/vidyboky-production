create table if not exists invoice
(
    id              uuid not null
        constraint invoice_pk primary key,
    order_id        uuid not null
        constraint invoice_order_fk references orders (id),
    bucket_key      varchar(255) not null,
    generation_date timestamp not null,
    constraint invoice_order_uq unique (order_id)
);