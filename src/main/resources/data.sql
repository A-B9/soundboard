INSERT INTO sounds (
    name,
    description,
    created_at,
    key_binding,
    duration_seconds,
    active,
    categories
) VALUES
(
    'Airhorn',
    'Classic loud airhorn sound',
    CURRENT_TIMESTAMP,
    'A',
    3,
    TRUE,
    'BATTLE,CITY'
),
(
    'Applause',
    'Crowd applause sound',
    CURRENT_TIMESTAMP,
    'P',
    8,
    TRUE,
    'TENSE,EPIC'
),
(
    'Error Buzz',
    'Short error buzzer',
    CURRENT_TIMESTAMP,
    'E',
    2,
    FALSE,
    'BATTLE,TRAVEL'
);
