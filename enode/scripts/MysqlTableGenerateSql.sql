CREATE TABLE `EventStream`
(
    `id`                      BIGINT AUTO_INCREMENT NOT NULL,
    `aggregateroot_type_name` VARCHAR(256)          NOT NULL,
    `aggregateroot_id`        VARCHAR(36)           NOT NULL,
    `version`                 INT                   NOT NULL,
    `command_id`              VARCHAR(36)           NOT NULL,
    `created_on`              DATETIME              NOT NULL,
    `events`                  MEDIUMTEXT            NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `IX_EventStream_AggId_Version` (`aggregateroot_id`, `Version`),
    UNIQUE KEY `IX_EventStream_AggId_CommandId` (`aggregateroot_id`, `command_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
CREATE TABLE `PublishedVersion`
(
    `id`                      BIGINT AUTO_INCREMENT NOT NULL,
    `processor_name`          VARCHAR(128)          NOT NULL,
    `aggregateroot_type_name` VARCHAR(256)          NOT NULL,
    `aggregateroot_id`        VARCHAR(36)           NOT NULL,
    `version`                 INT                   NOT NULL,
    `created_on`              DATETIME              NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `IX_PublishedVersion_AggId_Version` (`processor_name`, `aggregateroot_id`, `version`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;