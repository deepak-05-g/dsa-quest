CREATE TABLE xp_transactions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount INTEGER NOT NULL CHECK (amount > 0),
    source VARCHAR(80) NOT NULL,
    reference_id VARCHAR(120),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT xp_transactions_reward_key UNIQUE (user_id, source, reference_id)
);

CREATE INDEX xp_transactions_user_created_idx
    ON xp_transactions(user_id, created_at DESC);

CREATE TABLE daily_activity (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    activity_date DATE NOT NULL,
    activity_type VARCHAR(80) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT daily_activity_unique_type UNIQUE (user_id, activity_date, activity_type)
);

CREATE INDEX daily_activity_user_date_idx
    ON daily_activity(user_id, activity_date DESC);

CREATE TABLE lessons (
    id UUID PRIMARY KEY,
    slug VARCHAR(160) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    topic VARCHAR(120) NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE learning_progress (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    last_accessed_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    CONSTRAINT learning_progress_unique_user_lesson UNIQUE (user_id, lesson_id)
);

CREATE INDEX learning_progress_user_accessed_idx
    ON learning_progress(user_id, last_accessed_at DESC);

CREATE TABLE achievements (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(160) NOT NULL,
    description VARCHAR(400) NOT NULL,
    criteria VARCHAR(400) NOT NULL,
    published BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE user_achievements (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    achievement_id UUID NOT NULL REFERENCES achievements(id) ON DELETE CASCADE,
    earned_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT user_achievements_unique UNIQUE (user_id, achievement_id)
);

CREATE INDEX user_achievements_user_earned_idx
    ON user_achievements(user_id, earned_at DESC);
