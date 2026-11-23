-- Prevent two concurrent requests from selling the same professional's slot.
-- This is enforced by PostgreSQL (not just by a browser-side check).
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE appointments
    ADD CONSTRAINT appointments_employee_no_overlap
    EXCLUDE USING gist (
        company_id WITH =,
        employee_id WITH =,
        tstzrange(starts_at, ends_at, '[)') WITH &&
    )
    WHERE (employee_id IS NOT NULL AND status IN ('SCHEDULED', 'CONFIRMED'));
