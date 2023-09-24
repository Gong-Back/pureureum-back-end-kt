create table project_apply
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT,
    `created_date` date        NOT NULL,
    `apply_status` varchar(20) NOT NULL,
    `user_id`      bigint(20) NOT NULL,
    `project_id`   bigint(20) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_project_id_user_id` (`project_id`,`user_id`),
    KEY            `fk_project_apply_project_id_ref_project_id` (`project_id`),
    CONSTRAINT `fk_project_apply_project_id_ref_project_id` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`)
);

create table project_like
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT,
    `created_date` date NOT NULL,
    `user_id`      bigint(20) NOT NULL,
    `project_id`   bigint(20) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_project_id_user_id` (`project_id`,`user_id`),
    KEY            `fk_project_like_project_id_ref_project_id` (`project_id`),
    CONSTRAINT `fk_project_like_project_id_ref_project_id` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`)
);
