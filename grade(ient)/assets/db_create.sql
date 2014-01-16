CREATE TABLE Semester (
  id INTEGER PRIMARY KEY,
  name TEXT NOT NULL,
  start_date INTEGER NOT NULL CHECK (start_date >= 0),
  end_date INTEGER NOT NULL CHECK (end_date >= 0),
  CHECK (start_date < end_date)
);----

CREATE TABLE Subject (
  id INTEGER PRIMARY KEY,
  semester_id INTEGER,
  name TEXT NOT NULL,
  abbreviation TEXT NOT NULL,
  start_date INTEGER NOT NULL CHECK (start_date >= 0), 
  end_date INTEGER NOT NULL CHECK (end_date >= 0),
  FOREIGN KEY (semester_id) REFERENCES Semester (id),
  CHECK (start_date < end_date)
);----

CREATE TABLE Task (
  id INTEGER PRIMARY KEY,
  subject_id INTEGER,
  name TEXT NOT NULL,
  is_done INTEGER NOT NULL DEFAULT 0 CHECK (is_done IN (0, 1)),
  start_instant INTEGER CHECK (start_instant >= 0),  --TODO: required? (not null)
  end_instant INTEGER CHECK (end_instant >= 0), --TODO: required? (not null)
  origin_time_zone TEXT NOT NULL,
  notes TEXT,
  FOREIGN KEY (subject_id) REFERENCES Subject (id),
  CHECK (start_instant < end_instant)
);----

CREATE TABLE Meeting_Time (
  subject_id INTEGER NOT NULL,
  day_of_week INTEGER NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),
  start_time INTEGER NOT NULL CHECK (start_time >= 0),
  end_time INTEGER NOT NULL CHECK (end_time >= 0),
  PRIMARY KEY (subject_id, day_of_week, start_time, end_time),
  FOREIGN KEY (subject_id) REFERENCES Subject (id) ON DELETE CASCADE,
  CHECK (start_time < end_time)
);----

CREATE TABLE Default_Work_Time (
  subject_id INTEGER NOT NULL,
  day_of_week INTEGER NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),
  start_time INTEGER NOT NULL CHECK (start_time >= 0),
  end_time INTEGER NOT NULL CHECK (end_time >= 0),
  PRIMARY KEY (subject_id, day_of_week, start_time, end_time),
  FOREIGN KEY (subject_id) REFERENCES Subject (id) ON DELETE CASCADE,
  CHECK (start_time < end_time)
);----

CREATE TABLE Task_Work_Interval (
  task_id INTEGER NOT NULL,
  start_instant INTEGER NOT NULL CHECK (start_instant >= 0),
  end_instant INTEGER NOT NULL CHECK (end_instant >= 0),
  PRIMARY KEY (task_id, start_instant, end_instant),
  FOREIGN KEY (task_id) REFERENCES Task (id) ON DELETE CASCADE,
  CHECK (start_instant < end_instant)
);----

CREATE TRIGGER update_subject_on_semester_delete 
  AFTER DELETE ON Semester FOR EACH ROW 
  BEGIN
    UPDATE Subject SET semester_id = NULL
    WHERE semester_id = OLD.id;
  END;----

CREATE TRIGGER update_task_on_subject_delete
  AFTER DELETE ON Subject FOR EACH ROW
  BEGIN
    UPDATE Task SET subject_id = NULL
    WHERE subject_id = OLD.id;
  END;----

--TODO: figure out if start and end should have one or separate indexes
--TODO: figure out if index is needed on semester or subject (or meeting times/work intervals)
CREATE INDEX task_start_index ON Task (start_instant);----
CREATE INDEX task_end_index ON Task (end_instant);----
