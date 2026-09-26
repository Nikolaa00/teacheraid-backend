-- Phase 1.1 — school tenant, staff/student users, classes, enrollments (ADR 0003).
-- Headmaster is school.headmaster_email, never app_user (BR-5).
-- Unique student email is per school. Soft-delete enrollments via left_at.

CREATE TABLE school (
    id                uuid PRIMARY KEY,
    name              text NOT NULL,
    headmaster_email  text NOT NULL,
    created_at        timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE app_user (
    id             uuid PRIMARY KEY,
    school_id      uuid REFERENCES school (id),
    email          text NOT NULL,
    password_hash  text,
    role           text NOT NULL,
    created_at     timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT app_user_role_chk CHECK (role IN ('TEACHER', 'SCHOOL_ADMIN', 'STUDENT'))
);

CREATE UNIQUE INDEX app_user_school_email_uidx
    ON app_user (school_id, lower(email))
    WHERE school_id IS NOT NULL;

CREATE TABLE class (
    id          uuid PRIMARY KEY,
    school_id   uuid NOT NULL REFERENCES school (id),
    teacher_id  uuid NOT NULL REFERENCES app_user (id),
    name        text NOT NULL,
    created_at  timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX class_school_id_idx ON class (school_id);
CREATE INDEX class_teacher_id_idx ON class (teacher_id);

CREATE TABLE enrollment (
    id          uuid PRIMARY KEY,
    school_id   uuid NOT NULL REFERENCES school (id),
    class_id    uuid NOT NULL REFERENCES class (id),
    student_id  uuid NOT NULL REFERENCES app_user (id),
    left_at     timestamptz,
    created_at  timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX enrollment_active_uidx
    ON enrollment (class_id, student_id)
    WHERE left_at IS NULL;

CREATE INDEX enrollment_school_id_idx ON enrollment (school_id);
