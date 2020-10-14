CREATE TABLE conference (
	id VARCHAR(38) NOT NULL,
	conference_id VARCHAR(38) NOT NULL,
	access_code VARCHAR(128) DEFAULT NULL,
	owner_name VARCHAR(1024) NOT NULL,
	owner_email VARCHAR(1024) NOT NULL,
	slug VARCHAR(1024) NOT NULL,
	name VARCHAR(1024) NOT NULL,
	description VARCHAR(1024) NOT NULL,
	location VARCHAR(1024) NOT NULL,
	tagline VARCHAR(1024) DEFAULT NULL,
	twitter_search VARCHAR(1024) DEFAULT NULL,
	start_date DATETIME NOT NULL,
	end_date DATETIME NOT NULL,
	is_published BIT NOT NULL,
	version BIGINT NOT NULL,
	event_sequence INT NOT NULL,
	PRIMARY KEY (id),
	UNIQUE uk_conference_id (conference_id)
) ENGINE = InnoDB CHARSET = utf8mb4;

CREATE TABLE conference_slug_index (
	id VARCHAR(38) NOT NULL,
	index_id VARCHAR(38) NOT NULL,
	conference_id VARCHAR(38) NOT NULL,
	slug VARCHAR(1024) NOT NULL,
	PRIMARY KEY (id),
	UNIQUE uk_index_id (index_id)
) ENGINE = InnoDB CHARSET = utf8mb4;

CREATE TABLE conference_seat_type (
	id VARCHAR(38) NOT NULL,
	seat_type_id VARCHAR(38) NOT NULL,
	conference_id VARCHAR(38) NOT NULL,
	name VARCHAR(70) NOT NULL,
	description VARCHAR(250) NOT NULL,
	price DECIMAL(18, 2) NOT NULL,
	quantity INT NOT NULL,
	available_quantity INT NOT NULL,
	PRIMARY KEY (id),
	UNIQUE uk_seat_type_id (seat_type_id)
) ENGINE = InnoDB CHARSET = utf8mb4;

CREATE TABLE reservation_item (
	id VARCHAR(38) NOT NULL,
	conference_id VARCHAR(38) NOT NULL,
	reservation_id VARCHAR(38) NOT NULL,
	seat_type_id VARCHAR(38) NOT NULL,
	quantity INT NOT NULL,
	PRIMARY KEY (id),
	UNIQUE uk_conference_id_reservation_id_seat_type_id (conference_id, reservation_id, seat_type_id)
) ENGINE = InnoDB CHARSET = utf8mb4;

CREATE TABLE `order` (
	id VARCHAR(38) NOT NULL,
	order_id VARCHAR(38) NOT NULL,
	conference_id VARCHAR(38) NOT NULL,
	status INT NOT NULL,
	access_code VARCHAR(1024) NULL,
	registrant_first_name VARCHAR(1024) NULL,
	registrant_last_name VARCHAR(1024) NULL,
	registrant_email VARCHAR(1024) NULL,
	total_amount DECIMAL(18, 2) NOT NULL,
	reservation_expiration_date DATETIME NULL,
	version BIGINT NOT NULL,
	PRIMARY KEY (order_id)
) ENGINE = InnoDB CHARSET = utf8mb4;

CREATE TABLE order_line (
	id VARCHAR(38) NOT NULL,
	order_id VARCHAR(38) NOT NULL,
	seat_type_id VARCHAR(38) NOT NULL,
	seat_type_name VARCHAR(1024) NULL,
	unit_price DECIMAL(18, 2) NOT NULL,
	quantity INT NOT NULL,
	line_total DECIMAL(18, 2) NOT NULL,
	PRIMARY KEY (id),
	UNIQUE uk_order (order_id, seat_type_id)
) ENGINE = InnoDB CHARSET = utf8mb4;

CREATE TABLE order_seat_assignment (
	id VARCHAR(38) NOT NULL,
	assignments_id VARCHAR(38) NOT NULL,
	order_id VARCHAR(38) NOT NULL,
	position INT NOT NULL,
	seat_type_id VARCHAR(38) NOT NULL,
	seat_type_name VARCHAR(1024) NULL,
	attendee_first_name VARCHAR(1024) NULL,
	attendee_last_name VARCHAR(1024) NULL,
	attendee_email VARCHAR(1024) NULL,
	PRIMARY KEY (id),
	UNIQUE uk_assignments_id_position (assignments_id, position)
) ENGINE = InnoDB CHARSET = utf8mb4;

CREATE TABLE payment (
	id VARCHAR(38) NOT NULL,
	payment_id VARCHAR(38) NOT NULL,
	state INT NOT NULL,
	order_id VARCHAR(38) NOT NULL,
	description VARCHAR(1024) NULL,
	total_amount DECIMAL(18, 2) NOT NULL,
	version BIGINT NOT NULL,
	PRIMARY KEY (id)
) ENGINE = InnoDB CHARSET = utf8mb4;

CREATE TABLE payment_item (
	id VARCHAR(38) NOT NULL,
	payment_item_id VARCHAR(38) NOT NULL,
	payment_id VARCHAR(38) NULL,
	description VARCHAR(1024) NULL,
	amount DECIMAL(18, 2) NOT NULL,
	PRIMARY KEY (id)
) ENGINE = InnoDB CHARSET = utf8mb4;