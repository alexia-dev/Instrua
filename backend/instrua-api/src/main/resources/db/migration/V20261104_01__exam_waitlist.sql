CREATE TABLE exam_waitlist_entries (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(), company_id UUID NOT NULL REFERENCES companies(id), patient_id UUID NOT NULL REFERENCES patients(id), exam_id UUID NOT NULL,
 medical_request_url VARCHAR(1000), preferred_shifts VARCHAR(100), status VARCHAR(20) NOT NULL DEFAULT 'WAITING', priority_level INTEGER NOT NULL DEFAULT 0 CHECK(priority_level BETWEEN 0 AND 2),
 notified_at TIMESTAMPTZ, response_deadline TIMESTAMPTZ, created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_waitlist_company_exam_status_priority ON exam_waitlist_entries(company_id,exam_id,status,priority_level DESC,created_at ASC);
