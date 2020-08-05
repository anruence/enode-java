CREATE TABLE conference (
	id varchar(38)  NOT NULL ,
	access_code varchar(128) DEFAULT NULL,
	owner_name varchar(1024) NOT NULL,
	owner_email varchar(1024) NOT NULL,
	slug varchar(1024) NOT NULL,
	name varchar(1024) NOT NULL,
	description varchar(1024) NOT NULL,
	location varchar(1024) NOT NULL,
	tagline varchar(1024) DEFAULT NULL,
	twitter_search varchar(1024) DEFAULT NULL,
	start_date DATETIME NOT NULL,
	end_date DATETIME NOT NULL,
	is_published BIT NOT NULL,
	version BIGINT NOT NULL,
	event_sequence INT NOT NULL,
	PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE conference_slug (
    index_id      varchar (38)    NOT NULL,
    conference_id VARCHAR (38) NOT NULL,
    slug         varchar(1024)   NOT NULL,
    PRIMARY KEY (index_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE conference_seat_type (
    id                varchar(38) NOT NULL,
    conference_id      varchar(38) NOT NULL,
    name              varchar (70)    NOT NULL,
    description       varchar (250)   NOT NULL,
    price             DECIMAL (18, 2)  NOT NULL,
    quantity          INT              NOT NULL,
    available_quantity INT              NOT NULL,
    PRIMARY KEY  (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE reservation_item (
    id  varchar(38) NOT NULL,
    conference_id  varchar(38) NOT NULL,
    reservation_id varchar(38) NOT NULL,
    seat_type_id    varchar(38) NOT NULL,
    quantity      INT              NOT NULL,
    PRIMARY KEY  (id),
    UNIQUE  KEY uk_conference_id_reservation_id_seat_type_id (conference_id, reservation_id, seat_type_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `order` (
    order_id                   varchar(38) NOT NULL,
    conference_id              varchar(38) NOT NULL,
    status                    INT              NOT NULL,
    access_code                varchar(1024)   NULL,
    registrant_first_name       varchar(1024)   NULL,
    registrant_last_name        varchar(1024)   NULL,
    registrant_email           varchar(1024)   NULL,
    total_amount               DECIMAL (18, 2)  NOT NULL,
    reservation_expiration_date DATETIME         NULL,
    version                   BIGINT           NOT NULL,
    PRIMARY KEY  (order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE order_line (
    id      varchar(38) NOT NULL,
    order_id      varchar(38) NOT NULL,
    seat_type_id   varchar(38) NOT NULL,
    seat_type_name varchar(1024)   NULL,
    unit_price    DECIMAL (18, 2)  NOT NULL,
    quantity     INT              NOT NULL,
    line_total    DECIMAL (18, 2)  NOT NULL,
    PRIMARY KEY  (id),
    UNIQUE KEY uk_order (order_id, seat_type_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE order_seat_assignment (
    id     varchar(38) NOT NULL,
    assignments_id     varchar(38) NOT NULL,
    order_id           varchar(38) NOT NULL,
    position          INT              NOT NULL,
    seat_type_id        varchar(38) NOT NULL,
    seat_type_name      varchar(1024)   NULL,
    attendee_first_name varchar(1024)   NULL,
    attendee_last_name  varchar(1024)   NULL,
    attendee_email     varchar(1024)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_assignments_id_position (assignments_id, position)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE payment (
    id          varchar(38) NOT NULL,
    state       INT              NOT NULL,
    order_id     varchar(38) NOT NULL,
    description varchar(1024)   NULL,
    total_amount DECIMAL (18, 2)  NOT NULL,
    version     BIGINT           NOT NULL,
    PRIMARY KEY  (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE payment_item (
    id          varchar(38) NOT NULL,
    description varchar(1024)   NULL,
    amount      DECIMAL (18, 2)  NOT NULL,
    payment_id   varchar(38) NULL,
    PRIMARY KEY  (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
