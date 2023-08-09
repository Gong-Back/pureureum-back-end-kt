create table dashboard
(
    id           bigint auto_increment primary key,
    created_date date   not null,
    is_deleted   bit    not null,
    project_id   bigint not null
);

alter table dashboard
    add index idx_project_dashboard_project_id (project_id);

create table dashboard_bulletin_board
(
    id           bigint auto_increment primary key,
    created_date date        not null,
    content      tinytext    not null,
    title        varchar(50) not null,
    user_id      bigint      not null,
    dashboard_id bigint      not null
);

alter table dashboard_bulletin_board
    add constraint fk_dashboard_bulletin_board_dashboard_id_ref_dashboard_id
        foreign key (dashboard_id)
            references dashboard (id),
    add index idx_dashboard_bulletin_board_title (title),
    add index idx_dashboard_bulletin_board_user_id (user_id);

create table dashboard_bulletin_board_comment
(
    id                          bigint auto_increment
        primary key,
    created_date                date         not null,
    content                     varchar(200) not null,
    depth                       int          not null,
    parent_comment_id           bigint       not null,
    user_id                     bigint       not null,
    dashboard_bulletin_board_id bigint       not null
);

alter table dashboard_bulletin_board_comment
    add constraint fk_dashboard_bulletin_board_comment_ref_bulletin_board_id
        foreign key (dashboard_bulletin_board_id)
            references dashboard_bulletin_board (id),
    add index idx_dashboard_bulletin_board_user_id (user_id);

create table dashboard_bulletin_board_file
(
    id                          bigint auto_increment
        primary key,
    created_date                date         not null,
    content_type                varchar(255) not null,
    file_key                    varchar(255) not null,
    original_file_name          varchar(255) not null,
    title                       varchar(30)  not null,
    dashboard_bulletin_board_id bigint       not null
);

alter table dashboard_bulletin_board_comment
    add constraint fk_dashboard_bulletin_board_file_ref_bulletin_board_id
        foreign key (dashboard_bulletin_board_id)
            references dashboard_bulletin_board (id);

create table dashboard_calendar
(
    id           bigint auto_increment
        primary key,
    created_date date        not null,
    content      varchar(30) not null,
    end_date     datetime(6) not null,
    start_date   datetime(6) not null,
    user_id      bigint      not null,
    dashboard_id bigint      not null
);

alter table dashboard_calendar
    add constraint fk_dashboard_calendar_dashboard_id_ref_dashboard_id
        foreign key (dashboard_id)
            references dashboard (id),
    add index idx_dashboard_calendar_end_date (end_date),
    add index idx_dashboard_calendar_start_date (start_date);

create table dashboard_gallery
(
    id                 bigint auto_increment
        primary key,
    created_date       date         not null,
    content_type       varchar(255) not null,
    file_key           varchar(255) not null,
    original_file_name varchar(255) not null,
    title              varchar(30)  not null,
    user_id            bigint       not null,
    dashboard_id       bigint       not null
);

alter table dashboard_gallery
    add constraint fk_dashboard_gallery_dashboard_id_ref_dashboard_id
        foreign key (dashboard_id)
            references dashboard (id);

create table dashboard_member
(
    id                bigint auto_increment
        primary key,
    created_date      date        not null,
    user_id           bigint      not null,
    user_project_role varchar(20) not null,
    dashboard_id      bigint      not null
);

alter table dashboard_member
    add constraint fk_dashboard_member_dashboard_id_ref_dashboard_id
        foreign key (dashboard_id)
            references dashboard (id),
    add index idx_dashboard_member_user_id (user_id);

create table dashboard_notice
(
    id           bigint auto_increment
        primary key,
    created_date date        not null,
    content      tinytext    not null,
    title        varchar(50) not null,
    user_id      bigint      not null,
    dashboard_id bigint      not null
);

alter table dashboard_notice
    add constraint fk_dashboard_notice_dashboard_id_ref_dashboard_id
        foreign key (dashboard_id)
            references dashboard (id);

create table dashboard_qna_board
(
    id           bigint auto_increment
        primary key,
    created_date date        not null,
    content      tinytext    not null,
    title        varchar(50) not null,
    user_id      bigint      not null
);

alter table dashboard_qna_board
    add index idx_dashboard_qna_board_title (title),
    add index idx_dashboard_qna_board_user_id (user_id);

create table dashboard_qna_board_comment
(
    id                     bigint auto_increment
        primary key,
    created_date           date         not null,
    content                varchar(200) not null,
    depth                  int          not null,
    parent_comment_id      bigint       not null,
    user_id                bigint       not null,
    dashboard_qna_board_id bigint       not null
);

alter table dashboard_qna_board_comment
    add constraint fk_dashboard_qna_board_comment_ref_dashboard_qna_board_id
        foreign key (dashboard_qna_board_id)
            references dashboard_qna_board (id),
    add index idx_dashboard_qna_board_user_id (user_id);

create table dashboard_qna_board_file
(
    id                     bigint auto_increment
        primary key,
    created_date           date         not null,
    content_type           varchar(255) not null,
    file_key               varchar(255) not null,
    original_file_name     varchar(255) not null,
    title                  varchar(30)  not null,
    dashboard_qna_board_id bigint       not null
);

alter table dashboard_qna_board_file
    add constraint fk_dashboard_qna_board_file_ref_dashboard_qna_board_id
        foreign key (dashboard_qna_board_id)
            references dashboard_qna_board (id);
