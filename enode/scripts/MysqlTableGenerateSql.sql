CREATE TABLE `EventStream`
(
    `Sequence`              BIGINT AUTO_INCREMENT NOT NULL,
    `AggregateRootTypeName` VARCHAR(256)          NOT NULL,
    `AggregateRootId`       VARCHAR(36)           NOT NULL,
    `Version`               INT                   NOT NULL,
    `CommandId`             VARCHAR(36)           NOT NULL,
    `CreatedOn`             DATETIME              NOT NULL,
    `Events`                MEDIUMTEXT            NOT NULL,
    PRIMARY KEY (`Sequence`),
    UNIQUE KEY `IX_EventStream_AggId_Version` (`AggregateRootId`, `Version`),
    UNIQUE KEY `IX_EventStream_AggId_CommandId` (`AggregateRootId`, `CommandId`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `PublishedVersion`
(
    `Sequence`              BIGINT AUTO_INCREMENT NOT NULL,
    `ProcessorName`         VARCHAR(128)          NOT NULL,
    `AggregateRootTypeName` VARCHAR(256)          NOT NULL,
    `AggregateRootId`       VARCHAR(36)           NOT NULL,
    `Version`               INT                   NOT NULL,
    `CreatedOn`             DATETIME              NOT NULL,
    PRIMARY KEY (`Sequence`),
    UNIQUE KEY `IX_PublishedVersion_AggId_Version` (`ProcessorName`, `AggregateRootId`, `Version`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;