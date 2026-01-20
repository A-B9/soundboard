INSERT INTO sounds (
    name,
    description,
    created_at,
    size,
    content_type,
    stored_name
) VALUES
(
    'Airhorn',
    'Classic loud airhorn sound',
    CURRENT_TIMESTAMP,
    8000000,
    'audio/mp3',
    'sounds/airhorn'
),
(
    'Applause',
    'Crowd applause sound',
    CURRENT_TIMESTAMP,
    3000000,
    'audio/mp3',
    'sounds/applause'
),
(
    'Error Buzz',
    'Short error buzzer',
    CURRENT_TIMESTAMP,
    1000000,
    'audio/mp3',
    'sounds/error buzz'
);
