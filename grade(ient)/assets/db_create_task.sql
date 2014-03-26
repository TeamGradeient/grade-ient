CREATE TABLE Task (
  _id INTEGER PRIMARY KEY AUTOINCREMENT,
  subject_name TEXT,
  name TEXT NOT NULL,
  is_done INTEGER NOT NULL DEFAULT 0 CHECK (is_done IN (0, 1)),
  -- In milliseconds since Unix epoch.
  start_instant INTEGER NOT NULL CHECK (start_instant >= 0),
  end_instant INTEGER NOT NULL CHECK (end_instant >= 0),
  notes TEXT,
  CHECK (start_instant <= end_instant)
);----

CREATE TABLE Task_Work_Interval (
  task_id INTEGER NOT NULL,
  -- In milliseconds since Unix epoch
  start_instant INTEGER NOT NULL CHECK (start_instant >= 0),
  end_instant INTEGER NOT NULL CHECK (end_instant >= 0),
  PRIMARY KEY (task_id, start_instant, end_instant),
  -- Deleting a task should delete all work intervals.
  FOREIGN KEY (task_id) REFERENCES Task (_id) ON DELETE CASCADE,
  CHECK (start_instant <= end_instant)
);----

--TODO: figure out if start and end should have one or separate indexes
--TODO: figure out if index is needed on semester or subject (or meeting times/work intervals)
CREATE INDEX task_start_index ON Task (start_instant);----
CREATE INDEX task_end_index ON Task (end_instant);----
