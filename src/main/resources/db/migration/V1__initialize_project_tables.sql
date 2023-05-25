create table facility
(
    id                bigint auto_increment
        primary key,
    created_date      date         not null,
    city              varchar(20)  null,
    county            varchar(20)  null,
    detail            varchar(100) null,
    district          varchar(20)  null,
    jibun             varchar(100) null,
    latitude          varchar(20)  null,
    longitude         varchar(20)  null,
    facility_category varchar(20)  not null,
    name              varchar(255) null,
    progress          varchar(15)  not null,
    user_id           bigint       not null
);

create table facility_certification_doc
(
    id                 bigint auto_increment
        primary key,
    created_date       date         not null,
    content_type       varchar(255) not null,
    file_key           varchar(255) not null,
    original_file_name varchar(255) not null,
    facility_id        bigint       null
);

alter table facility_certification_doc
    add constraint fk_facility_certification_document_id_ref_facility_id
        foreign key (facility_id)
            references facility (id);

create table profile
(
    id                 bigint auto_increment
        primary key,
    created_date       date         not null,
    content_type       varchar(255) not null,
    file_key           varchar(255) not null,
    original_file_name varchar(255) not null
);

create table project
(
    id                 bigint auto_increment
        primary key,
    created_date       date         not null,
    facility_id        bigint       not null,
    like_count         int          not null,
    payment_type       varchar(20)  not null,
    project_category   varchar(20)  not null,
    content            varchar(255) not null,
    guide              varchar(500) null,
    introduction       varchar(200) not null,
    max_age            int          not null,
    min_age            int          not null,
    notice             varchar(500) null,
    project_end_date   date         not null,
    project_start_date date         not null,
    recruits           int          not null,
    title              varchar(20)  not null,
    total_recruits     int          not null,
    project_status     varchar(20)  not null,
    user_id            bigint       not null
);

alter table project
    add index idx_project_facility_Id (facility_id),
    add index idx_project_user_Id (user_id);

create table project_file
(
    id                 bigint auto_increment
        primary key,
    created_date       date         not null,
    content_type       varchar(255) not null,
    file_key           varchar(255) not null,
    original_file_name varchar(255) not null,
    project_file_type  varchar(255) not null,
    project_id         bigint       not null
);

alter table project_file
    add constraint fk_project_file_project_id_ref_project_id
        foreign key (project_id)
            references project (id);


create table project_payment
(
    id                  bigint auto_increment
        primary key,
    created_date        date         not null,
    amount              int          not null,
    deposit_information varchar(500) null,
    refund_instruction  varchar(500) null,
    project_id          bigint       not null
);

alter table project_payment
    add constraint fk_project_payment_project_id_ref_project_id
        foreign key (project_id)
            references project (id),
    add unique uk_project_id (project_id);

create table sms_log
(
    id           bigint auto_increment
        primary key,
    created_date date         not null,
    is_success   bit          not null,
    receiver     varchar(255) null
);

create table temp_social_auth
(
    id           bigint auto_increment
        primary key,
    created_date date         not null,
    birthday     varchar(255) null,
    email        varchar(255) not null,
    gender       smallint     null,
    name         varchar(255) null,
    phone_number varchar(255) null,
    social_type  varchar(255) null,
    constraint UK_llr78woo32d1c5kr2p88f5olu
        unique (email)
);

alter table temp_social_auth
    add index idx_email (email),
    add unique UK_llr78woo32d1c5kr2p88f5olu (email);

create table user
(
    id           bigint auto_increment
        primary key,
    created_date date         not null,
    birthday     date         not null,
    email        varchar(30)  not null,
    gender       varchar(6)   not null,
    name         varchar(100) not null,
    nickname     varchar(30)  not null,
    phone_number varchar(13)  not null,
    user_role    varchar(10)  not null,
    password     varchar(255) not null,
    social_type  varchar(10)  not null,
    profile_id   bigint       not null
);

alter table user
    add constraint FKof44u64o1d7scaukghm9veo23
        foreign key (profile_id)
            references profile (id),
    add unique UK_1mcjtpxmwom9h9bf2q0k412e0 (profile_id),
    add unique UK_4bgmpi98dylab6qdvf9xyaxu4 (phone_number),
    add unique UK_n4swgcf30j6bmtb4l4cjryuym (nickname),
    add unique UK_ob8kqyqqgmefl0aco34akdtpe (email);
