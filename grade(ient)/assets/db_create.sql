CREATE TABLE Semester (
  id INTEGER PRIMARY KEY,
  name TEXT NOT NULL,
  -- In seconds since Unix epoch. (Use noon UTC for time.
  -- Convert to YYYY-MM-DD using date(start_date, 'unixepoch').
  -- The other possible choice was YYYYMMDD, but no built-in conversion
  -- methods exist for that format. This comment applies to the other
  -- dates stored as integers as well.)
  start_date INTEGER CHECK (start_date >= 0),
  end_date INTEGER CHECK (end_date >= 0),
  CHECK (start_date < end_date)
);----

CREATE TABLE Subject (
  id INTEGER PRIMARY KEY,
  semester_id INTEGER,
  name TEXT NOT NULL,
  abbreviation TEXT NOT NULL,
  -- In seconds since Unix epoch. (Use noon UTC for time.)
  start_date INTEGER CHECK (start_date >= 0), 
  end_date INTEGER CHECK (end_date >= 0),
  -- It's legal for a subject to not have a semester, so if a subject's
  -- semester is deleted, set the semester to null.
  FOREIGN KEY (semester_id) REFERENCES Semester (id) ON DELETE SET NULL,
  CHECK (start_date < end_date)
);----

CREATE TABLE Task (
  id INTEGER PRIMARY KEY,
  subject_id INTEGER,
  name TEXT NOT NULL,
  is_done INTEGER NOT NULL DEFAULT 0 CHECK (is_done IN (0, 1)),
  -- In seconds since Unix epoch.
  -- (The reason for not requiring this would be if we decide to have a task
  -- "inbox" for partial information about tasks. If we decide not to have 
  -- that feature, start_instant and end_instant should not be null.)
  start_instant INTEGER CHECK (start_instant >= 0),
  end_instant INTEGER CHECK (end_instant >= 0),
  -- Format: [+-]HH:MM
  origin_time_zone TEXT NOT NULL,
  notes TEXT,
  -- It's legal for a task to not have a subject, so if a task's
  -- subject is deleted, set the subject to null.
  FOREIGN KEY (subject_id) REFERENCES Subject (id) ON DELETE SET NULL,
  CHECK (start_instant < end_instant)
);----

CREATE TABLE Meeting_Time (
  subject_id INTEGER NOT NULL,
  -- Sunday = 0 ... Saturday = 6
  day_of_week INTEGER NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),
  -- Stored in seconds since beginning of day (technically since Unix epoch).
  -- Convert with strftime('%H:%M', start_time, 'unixepoch') to get 24-hour time.
  start_time INTEGER NOT NULL CHECK (start_time >= 0),
  end_time INTEGER NOT NULL CHECK (end_time >= 0),
  PRIMARY KEY (subject_id, day_of_week, start_time, end_time),
  -- Deleting a subject should delete all meeting times.
  FOREIGN KEY (subject_id) REFERENCES Subject (id) ON DELETE CASCADE,
  CHECK (start_time < end_time)
);----

CREATE TABLE Default_Work_Time (
  subject_id INTEGER NOT NULL,
  -- Sunday = 0 ... Saturday = 6
  day_of_week INTEGER NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),
  -- Stored in seconds since beginning of day (technically since Unix epoch).
  -- Convert with strftime('%H:%M', start_time, 'unixepoch') to get 24-hour time.
  start_time INTEGER NOT NULL CHECK (start_time >= 0),
  end_time INTEGER NOT NULL CHECK (end_time >= 0),
  PRIMARY KEY (subject_id, day_of_week, start_time, end_time),
  -- Deleting a subject should delete all default work times.
  FOREIGN KEY (subject_id) REFERENCES Subject (id) ON DELETE CASCADE,
  CHECK (start_time < end_time)
);----

CREATE TABLE Task_Work_Interval (
  task_id INTEGER NOT NULL,
  -- In seconds since Unix epoch
  start_instant INTEGER NOT NULL CHECK (start_instant >= 0),
  end_instant INTEGER NOT NULL CHECK (end_instant >= 0),
  PRIMARY KEY (task_id, start_instant, end_instant),
  -- Deleting a task should delete all work intervals.
  FOREIGN KEY (task_id) REFERENCES Task (id) ON DELETE CASCADE,
  CHECK (start_instant < end_instant)
);----

--TODO: figure out if start and end should have one or separate indexes
--TODO: figure out if index is needed on semester or subject (or meeting times/work intervals)
CREATE INDEX task_start_index ON Task (start_instant);----
CREATE INDEX task_end_index ON Task (end_instant);----
