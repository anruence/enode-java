CREATE TABLE IF NOT EXISTS `conference` (
    id BIGINT(20) unsigned NOT NULL AUTO_INCREMENT,
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
	version int(10) NOT NULL,
	event_sequence INT(10) NOT NULL,
	PRIMARY KEY (id),
	UNIQUE KEY uk_conference_id (conference_id)
) ENGINE = InnoDB CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `conference_slug_index` (
    id BIGINT(20) unsigned NOT NULL AUTO_INCREMENT,
	index_id VARCHAR(38) NOT NULL,
	conference_id VARCHAR(38) NOT NULL,
	slug VARCHAR(1024) NOT NULL,
	PRIMARY KEY (id),
	UNIQUE KEY uk_index_id (index_id)
) ENGINE = InnoDB CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `conference_seat_type` (
    id BIGINT(20) unsigned NOT NULL AUTO_INCREMENT,
	seat_type_id VARCHAR(38) NOT NULL,
	conference_id VARCHAR(38) NOT NULL,
	name VARCHAR(70) NOT NULL,
	description VARCHAR(250) NOT NULL,
	price DECIMAL(18, 2) NOT NULL,
	quantity INT(10) NOT NULL,
	available_quantity INT(10) NOT NULL,
	PRIMARY KEY (id),
	UNIQUE KEY uk_seat_type_id (seat_type_id)
) ENGINE = InnoDB CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `reservation_item` (
    id BIGINT(20) unsigned NOT NULL AUTO_INCREMENT,
	conference_id VARCHAR(38) NOT NULL,
	reservation_id VARCHAR(38) NOT NULL,
	seat_type_id VARCHAR(38) NOT NULL,
	quantity INT(10) NOT NULL,
	PRIMARY KEY (id),
	UNIQUE KEY uk_conference_id_reservation_id_seat_type_id (conference_id, reservation_id, seat_type_id)
) ENGINE = InnoDB CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `order` (
    id BIGINT(20) unsigned NOT NULL AUTO_INCREMENT,
	order_id VARCHAR(38) NOT NULL,
	conference_id VARCHAR(38) NOT NULL,
	status INT(10) NOT NULL,
	access_code VARCHAR(1024) NULL,
	registrant_first_name VARCHAR(1024) NULL,
	registrant_last_name VARCHAR(1024) NULL,
	registrant_email VARCHAR(1024) NULL,
	total_amount DECIMAL(18, 2) NOT NULL,
	reservation_expiration_date DATETIME NULL,
	version INT(10) NOT NULL,
	PRIMARY KEY (id),
	UNIQUE KEY uk_order_id (order_id)
) ENGINE = InnoDB CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `order_line` (
    id BIGINT(20) unsigned NOT NULL AUTO_INCREMENT,
	order_id VARCHAR(38) NOT NULL,
	seat_type_id VARCHAR(38) NOT NULL,
	seat_type_name VARCHAR(1024) NULL,
	unit_price DECIMAL(18, 2) NOT NULL,
	quantity INT(10) NOT NULL,
	line_total DECIMAL(18, 2) NOT NULL,
	PRIMARY KEY (id),
	UNIQUE KEY uk_order_id_seat_type_id (order_id, seat_type_id)
) ENGINE = InnoDB CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `order_seat_assignment` (
    id BIGINT(20) unsigned NOT NULL AUTO_INCREMENT,
	assignments_id VARCHAR(38) NOT NULL,
	order_id VARCHAR(38) NOT NULL,
	position INT(10) NOT NULL,
	seat_type_id VARCHAR(38) NOT NULL,
	seat_type_name VARCHAR(1024) NULL,
	attendee_first_name VARCHAR(1024) NULL,
	attendee_last_name VARCHAR(1024) NULL,
	attendee_email VARCHAR(1024) NULL,
	PRIMARY KEY (id),
	UNIQUE KEY uk_assignments_id_position (assignments_id, position)
) ENGINE = InnoDB CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `payment` (
    id BIGINT(20) unsigned NOT NULL AUTO_INCREMENT,
	payment_id VARCHAR(38) NOT NULL,
	state INT(10) NOT NULL,
	order_id VARCHAR(38) NOT NULL,
	description VARCHAR(1024) NULL,
	total_amount DECIMAL(18, 2) NOT NULL,
	version BIGINT NOT NULL,
	PRIMARY KEY (id),
	UNIQUE KEY uk_payment_id (payment_id)
) ENGINE = InnoDB CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `payment_item` (
    id BIGINT(20) unsigned NOT NULL AUTO_INCREMENT,
	payment_item_id VARCHAR(38) NOT NULL,
	payment_id VARCHAR(38) NULL,
	description VARCHAR(1024) NULL,
	amount DECIMAL(18, 2) NOT NULL,
	PRIMARY KEY (id),
	UNIQUE KEY uk_payment_item_id (payment_item_id)
) ENGINE = InnoDB CHARSET = utf8mb4;