USE projectit211;

INSERT INTO users (id, username, password_hash, role, is_active, created_at, updated_at)
VALUES
    (1, 'patient1', '$2a$12$j5LfTKqdA8q.DA.lkfIr9uu2CM2YwKYmZJFk8REgnVmdp98dTo7HG', 'PATIENT', true, NOW(), NULL),
    (2, 'doctor1', '$2a$12$.8ZWcseWfTFaIT9JmUTz.OgOEeKn0PjXcSHrRrO1uW./rFLNExrxq', 'DOCTOR', true, NOW(), NULL),
    (3, 'admin1', '$2a$12$CWYoYjBD6YNk9qijFyH.weU7OMuSL8nPTq8m3Af.huoFxApGNeEQ6', 'ADMIN', true, NOW(), NULL)
ON DUPLICATE KEY UPDATE
    username = VALUES(username),
    password_hash = VALUES(password_hash),
    role = VALUES(role),
    is_active = VALUES(is_active),
    updated_at = NOW();
