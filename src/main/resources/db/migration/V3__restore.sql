
INSERT INTO roles (name)
VALUES
    ('ROLE_ADMIN'),
    ('ROLE_ORGANIZER'),
    ('ROLE_PARTICIPANT')
ON CONFLICT (name) DO NOTHING;


-- ============================================================
-- 2. USUARIOS SEMILLA
-- Hash BCrypt válido para: password123
-- ============================================================

INSERT INTO users (
    name,
    email,
    password,
    enabled,
    account_locked,
    created_at,
    updated_at,
    deleted
)
VALUES
(
    'Administrador General',
    'admin@ups.edu.ec',
    '$2a$10$gLbE70LdT6FIwVydaOwFGO7nVOVW/SZfSaRliXOq80xENSsdVon/K',
    TRUE,
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    FALSE
),
(
    'Dr. Juan Pérez (Organizador)',
    'organizer@ups.edu.ec',
    '$2a$10$gLbE70LdT6FIwVydaOwFGO7nVOVW/SZfSaRliXOq80xENSsdVon/K',
    TRUE,
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    FALSE
),
(
    'Estudiante Carlos López',
    'student@ups.edu.ec',
    '$2a$10$gLbE70LdT6FIwVydaOwFGO7nVOVW/SZfSaRliXOq80xENSsdVon/K',
    TRUE,
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    FALSE
)
ON CONFLICT (email) DO UPDATE SET
    name = EXCLUDED.name,
    password = EXCLUDED.password,
    enabled = TRUE,
    account_locked = FALSE,
    updated_at = CURRENT_TIMESTAMP,
    deleted = FALSE;


-- ============================================================
-- 3. ROLES CORRECTOS PARA LOS USUARIOS SEMILLA
-- ============================================================

DELETE FROM user_roles ur
USING users u
WHERE ur.user_id = u.id
  AND u.email IN (
      'admin@ups.edu.ec',
      'organizer@ups.edu.ec',
      'student@ups.edu.ec'
  );

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM (
    VALUES
        ('admin@ups.edu.ec', 'ROLE_ADMIN'),
        ('organizer@ups.edu.ec', 'ROLE_ORGANIZER'),
        ('student@ups.edu.ec', 'ROLE_PARTICIPANT')
) AS seed(email, role_name)
JOIN users u ON u.email = seed.email
JOIN roles r ON r.name = seed.role_name
ON CONFLICT (user_id, role_id) DO NOTHING;


-- ============================================================
-- 4. CATEGORÍAS
-- ============================================================

INSERT INTO categories (
    name,
    description,
    created_at,
    updated_at,
    deleted
)
VALUES
(
    'Inteligencia Artificial y Ciencia de Datos',
    'Conferencias, talleres y seminarios sobre IA, ML y análisis masivo de datos.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    FALSE
),
(
    'Ciberseguridad y Redes',
    'Eventos centrados en seguridad de la información, hacking ético y redes avanzadas.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    FALSE
),
(
    'Desarrollo de Software y Arquitectura Web',
    'Seminarios de buenas prácticas, microservicios y frameworks modernos.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    FALSE
)
ON CONFLICT (name) DO UPDATE SET
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP,
    deleted = FALSE;


-- ============================================================
-- 5. EVENTO SEMILLA
-- ============================================================

INSERT INTO events (
    id,
    title,
    description,
    modality,
    location,
    capacity,
    available_seats,
    start_date,
    end_date,
    status,
    organizer_id,
    category_id,
    created_at,
    updated_at,
    deleted
)
SELECT
    1,
    'Congreso Internacional de IA 2026',
    'Magno evento sobre los últimos avances en Inteligencia Artificial y Deep Learning.',
    'HYBRID',
    'Auditorio Leónidas Proaño / Zoom',
    100,
    100,
    '2026-09-10 09:00:00+00',
    '2026-09-12 18:00:00+00',
    'PUBLISHED',
    organizer.id,
    category.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    FALSE
FROM users organizer
JOIN categories category
    ON category.name = 'Inteligencia Artificial y Ciencia de Datos'
WHERE organizer.email = 'organizer@ups.edu.ec'
ON CONFLICT (id) DO UPDATE SET
    title = EXCLUDED.title,
    description = EXCLUDED.description,
    modality = EXCLUDED.modality,
    location = EXCLUDED.location,
    capacity = EXCLUDED.capacity,
    start_date = EXCLUDED.start_date,
    end_date = EXCLUDED.end_date,
    status = EXCLUDED.status,
    organizer_id = EXCLUDED.organizer_id,
    category_id = EXCLUDED.category_id,
    updated_at = CURRENT_TIMESTAMP,
    deleted = FALSE;


-- ============================================================
-- 6. SESIÓN SEMILLA
-- ============================================================

INSERT INTO sessions (
    id,
    title,
    description,
    start_time,
    end_time,
    room,
    event_id,
    created_at,
    updated_at,
    deleted
)
VALUES
(
    1,
    'Keynote: Redes Neuronales Transformer',
    'Presentación magistral sobre transformers en visión y PNL.',
    '2026-09-10 10:00:00+00',
    '2026-09-10 12:00:00+00',
    'Aula Magna 1',
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    FALSE
)
ON CONFLICT (id) DO UPDATE SET
    title = EXCLUDED.title,
    description = EXCLUDED.description,
    start_time = EXCLUDED.start_time,
    end_time = EXCLUDED.end_time,
    room = EXCLUDED.room,
    event_id = EXCLUDED.event_id,
    updated_at = CURRENT_TIMESTAMP,
    deleted = FALSE;


-- ============================================================
-- 7. INSCRIPCIÓN SEMILLA
-- ============================================================

INSERT INTO registrations (
    user_id,
    event_id,
    registration_date,
    status,
    created_at,
    updated_at,
    deleted
)
SELECT
    student.id,
    1,
    CURRENT_TIMESTAMP,
    'CONFIRMED',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    FALSE
FROM users student
WHERE student.email = 'student@ups.edu.ec'
ON CONFLICT (user_id, event_id) DO UPDATE SET
    status = 'CONFIRMED',
    updated_at = CURRENT_TIMESTAMP,
    deleted = FALSE;


-- ============================================================
-- 8. RECALCULAR CUPOS DISPONIBLES
-- ============================================================

UPDATE events e
SET
    available_seats = GREATEST(
        e.capacity - (
            SELECT COUNT(*)
            FROM registrations r
            WHERE r.event_id = e.id
              AND r.status = 'CONFIRMED'
              AND r.deleted = FALSE
        ),
        0
    ),
    updated_at = CURRENT_TIMESTAMP
WHERE e.id = 1;


-- ============================================================
-- 9. CORREGIR SECUENCIAS
-- ============================================================

SELECT setval(
    pg_get_serial_sequence('roles', 'id'),
    GREATEST(COALESCE((SELECT MAX(id) FROM roles), 1), 1),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('users', 'id'),
    GREATEST(COALESCE((SELECT MAX(id) FROM users), 1), 1),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('categories', 'id'),
    GREATEST(COALESCE((SELECT MAX(id) FROM categories), 1), 1),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('events', 'id'),
    GREATEST(COALESCE((SELECT MAX(id) FROM events), 1), 1),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('sessions', 'id'),
    GREATEST(COALESCE((SELECT MAX(id) FROM sessions), 1), 1),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('registrations', 'id'),
    GREATEST(COALESCE((SELECT MAX(id) FROM registrations), 1), 1),
    TRUE
);