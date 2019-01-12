CREATE TABLE `Command` (
  `Sequence`        BIGINT AUTO_INCREMENT      NOT NULL,
  `CommandId`       VARCHAR(36)                NOT NULL,
  `AggregateRootId` VARCHAR(36)                NULL,
  `MessagePayload`  MEDIUMTEXT
                    CHARACTER SET utf8mb4
                    COLLATE utf8mb4_unicode_ci NULL,
  `MessageTypeName` VARCHAR(256)               NOT NULL,
  `CreatedOn`       DATETIME                   NOT NULL,
  PRIMARY KEY (`Sequence`),
  UNIQUE KEY `IX_Command_CommandId` (`CommandId`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8;

CREATE TABLE `EventStream` (
  `Sequence`              BIGINT AUTO_INCREMENT      NOT NULL,
  `AggregateRootTypeName` VARCHAR(256)               NOT NULL,
  `AggregateRootId`       VARCHAR(36)                NOT NULL,
  `Version`               INT                        NOT NULL,
  `CommandId`             VARCHAR(36)                NOT NULL,
  `CreatedOn`             DATETIME                   NOT NULL,
  `Events`                MEDIUMTEXT
                          CHARACTER SET utf8mb4
                          COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`Sequence`),
  UNIQUE KEY `IX_EventStream_AggId_Version` (`AggregateRootId`, `Version`),
  UNIQUE KEY `IX_EventStream_AggId_CommandId` (`AggregateRootId`, `CommandId`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8;

CREATE TABLE `PublishedVersion` (
  `Sequence`              BIGINT AUTO_INCREMENT NOT NULL,
  `ProcessorName`         VARCHAR(128)          NOT NULL,
  `AggregateRootTypeName` VARCHAR(256)          NOT NULL,
  `AggregateRootId`       VARCHAR(36)           NOT NULL,
  `Version`               INT                   NOT NULL,
  `CreatedOn`             DATETIME              NOT NULL,
  PRIMARY KEY (`Sequence`),
  UNIQUE KEY `IX_PublishedVersion_AggId_Version`(`ProcessorName`, `AggregateRootId`, `Version`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8;

CREATE TABLE `LockKey` (
  `Name` VARCHAR(128) NOT NULL,
  PRIMARY KEY (`Name`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8;