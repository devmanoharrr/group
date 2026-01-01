-- Observation core table (minimal; we can evolve with V2.. later)
CREATE TABLE IF NOT EXISTS observation (
  id TEXT PRIMARY KEY,                -- UUID string
  citizen_id TEXT NOT NULL,
  postcode TEXT NOT NULL,
  temperature_c REAL,                 -- nullable
  ph REAL,                            -- nullable
  alkalinity_mg_l REAL,               -- nullable
  turbidity_ntu REAL,                 -- nullable
  observations TEXT,                  -- CSV of enum values (e.g., "CLEAR,MURKY")
  image_paths TEXT,                   -- CSV of relative paths (max 3 when enforced at API layer)
  authority TEXT,                     -- for dashboard filtering
  created_at TEXT NOT NULL            -- ISO-8601 string timestamp
);

CREATE INDEX IF NOT EXISTS idx_observation_authority_created
  ON observation(authority, created_at);
