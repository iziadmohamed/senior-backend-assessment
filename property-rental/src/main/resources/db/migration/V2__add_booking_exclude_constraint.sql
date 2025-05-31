CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE bookings
ADD COLUMN booking_range tstzrange NOT NULL;

CREATE OR REPLACE FUNCTION update_booking_range() RETURNS trigger AS $$
BEGIN
  NEW.booking_range := tstzrange(NEW.start_date, NEW.end_date);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_booking_range
BEFORE INSERT OR UPDATE ON bookings
FOR EACH ROW EXECUTE FUNCTION update_booking_range();

ALTER TABLE bookings
ADD CONSTRAINT no_overlapping_bookings
EXCLUDE USING gist (
    property_id WITH =,
    booking_range WITH &&
)
WHERE (deleted_at IS NULL);
