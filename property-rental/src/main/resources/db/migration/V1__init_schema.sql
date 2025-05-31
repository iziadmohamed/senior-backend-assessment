-- Property table
CREATE TABLE properties (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    location VARCHAR(255) NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    created_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

-- Amenity table
CREATE TABLE amenities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

-- Join table for property_amenities
CREATE TABLE property_amenities (
    property_id BIGINT NOT NULL,
    amenity_id BIGINT NOT NULL,
    PRIMARY KEY (property_id, amenity_id),
    CONSTRAINT fk_property FOREIGN KEY (property_id) REFERENCES properties(id),
    CONSTRAINT fk_amenity FOREIGN KEY (amenity_id) REFERENCES amenities(id)
);

-- Booking table
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    property_id BIGINT NOT NULL,
    owner_id VARCHAR(255) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    CONSTRAINT fk_booking_property FOREIGN KEY (property_id) REFERENCES properties(id)
);

-- Index for booking lookup
CREATE INDEX idx_booking_property_id ON bookings(property_id);

-- Index to help find overlapping bookings
CREATE INDEX idx_booking_dates ON bookings(property_id, start_date, end_date) WHERE deleted_at IS NULL;

-- Insert 5 default amenities
INSERT INTO amenities (name) VALUES
    ('WiFi'),
    ('Parking'),
    ('Pool'),
    ('Air Conditioning'),
    ('Breakfast');
