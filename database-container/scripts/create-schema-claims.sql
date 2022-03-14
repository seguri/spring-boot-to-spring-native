drop table if exists claim_drug;
drop table if exists claim_inpatient;
drop table if exists claim_outpatient;
drop table if exists drug;
drop table if exists facility;
drop table if exists procedure;
drop table if exists claim;
drop table if exists provider;
drop table if exists eligibility;
drop table if exists plan;
drop table if exists member;
drop table if exists diagnosis;
drop table if exists address;
drop sequence if exists serial;


create table if not exists address
(
    id       integer generated always as identity primary key,
    street_1 varchar not null,
    street_2 varchar null,
    city     varchar not null,
    state    varchar not null,
    zip      varchar not null
);

create table if not exists member
(
    id                integer generated always as identity primary key,
    subscriber_number varchar not null,
    first_name        varchar not null,
    last_name         varchar not null,
    gender            varchar not null,
    dob               date    not null,
    address_id        integer not null,
    constraint fk_address
        foreign key (address_id)
            references address (id)
);

create table if not exists plan
(
    id          integer generated always as identity primary key,
    name        varchar not null,
    description varchar not null
);

create table if not exists eligibility
(
    id             integer generated always as identity primary key,
    plan_id        integer not null,
    member_id      integer not null,
    effective_date date    not null,
    end_date       date    not null,
    constraint fk_plan
        foreign key (plan_id)
            references plan (id),
    constraint fk_member
        foreign key (member_id)
            references member (id)
);

create table if not exists provider
(
    id         integer generated always as identity primary key,
    npi        varchar not null,
    first_name varchar not null,
    last_name  varchar not null,
    gender     varchar not null,
    address_id integer not null,
    constraint fk_address
        foreign key (address_id)
            references address (id)
);

create table if not exists facility
(
    id         integer generated always as identity primary key,
    name       varchar not null,
    address_id integer not null,
    constraint fk_address
        foreign key (address_id)
            references address (id)
);

create table if not exists diagnosis
(
    id          integer generated always as identity primary key,
    code        varchar not null,
    description varchar not null
);

create table if not exists drug
(
    id          integer generated always as identity primary key,
    ndc         varchar not null,
    name        varchar not null,
    description varchar not null
);

create table if not exists procedure
(
    id          integer generated always as identity primary key,
    code        varchar not null,
    description varchar not null
);

create table if not exists claim
(
    id                     integer generated always as identity primary key,
    claim_number           varchar not null,
    precert_number         varchar null,
    claim_date             date    not null,
    submitted_amount       decimal not null,
    approved_amount        decimal null,
    status                 integer not null,
    status_date            date    not null,
    payment_date           date    null,
    payment_number         varchar null,
    member_id              integer not null,
    submitting_provider_id integer not null,
    diagnosis_id           integer not null,
    constraint fk_member
        foreign key (member_id)
            references member (id),
    constraint fk_provider
        foreign key (submitting_provider_id)
            references provider (id),
    constraint fk_diagnosis
        foreign key (diagnosis_id)
            references diagnosis (id)
);

create table if not exists claim_drug
(
    id       integer generated always as identity primary key,
    claim_id integer not null,
    drug_id  integer not null,
    units    integer not null,
    constraint fk_claim
        foreign key (claim_id)
            references claim (id),
    constraint fk_drug
        foreign key (drug_id)
            references drug (id)
);

create table if not exists claim_outpatient
(
    id                integer generated always as identity primary key,
    claim_id          integer not null,
    procedure_code_id integer not null,
    procedure_date    date    not null,
    constraint fk_claim
        foreign key (claim_id)
            references claim (id),
    constraint fk_procedure
        foreign key (procedure_code_id)
            references procedure (id)
);

create table if not exists claim_inpatient
(
    id             integer generated always as identity primary key,
    claim_id       integer not null,
    facility_id    integer not null,
    admit_date     date    not null,
    discharge_date date    not null,
    constraint fk_claim
        foreign key (claim_id)
            references claim (id),
    constraint fk_facility
        foreign key (facility_id)
            references facility (id)
);

create sequence if not exists claims_serial start 1;