-- Create tables for SRS DB

CREATE TABLE Deck(
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    creationTimestamp INTEGER NOT NULL, -- In milliseconds since Unix epoch
    intervalModifier INTEGER NOT NULL DEFAULT 100
);

CREATE TABLE Card(
    id INTEGER PRIMARY KEY,
    deckId INTEGER NOT NULL REFERENCES Deck(id) ON DELETE CASCADE,
    front TEXT NOT NULL,
    back TEXT NOT NULL,
    creationTimestamp INTEGER NOT NULL -- In milliseconds since Unix epoch
);

CREATE INDEX cardCreationTimestamp ON Card(creationTimestamp);
CREATE INDEX cardDeckId ON Card(deckId);

CREATE TABLE Answer(
    cardId INTEGER NOT NULL REFERENCES Card(id) ON DELETE CASCADE,
    isCorrect INTEGER NOT NULL,
    timestamp INTEGER NOT NULL
);

CREATE INDEX answerCardId ON Answer(cardId);
CREATE INDEX answerTimestamp ON Answer(timestamp);

CREATE TABLE Schedule(
    cardId INTEGER PRIMARY KEY REFERENCES Card(id) ON DELETE CASCADE,
    scheduledForTimestamp INTEGER, -- In milliseconds since Unix epoch, NULL when suspended
    intervalDays INTEGER, -- 0 for new cards, NULL when suspended
    isLeech INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX scheduleScheduledForTimestamp ON Schedule(scheduledForTimestamp);
CREATE INDEX scheduleIsLeech ON Schedule(isLeech);

-- Create empty decks

-- TODO: Add one of these INSERT statements for each deck that needs to be
-- imported. This creates an empty deck in the new DB.
INSERT INTO Deck (id, name, intervalModifier, creationTimestamp) VALUES
  (
    1, -- ID of the deck in the new DB
    'deck name',
    100, -- Interval modifier. 100 is 100%
    strftime('%s','now')
  );

-- https://github.com/ankidroid/Anki-Android/wiki/Database-Structure
ATTACH DATABASE 'collection.anki2' AS anki;

-- Migrate cards into the decks

-- TODO: Add one of these INSERT statements for each deck that needs to be
-- imported. This imports the cards from the Anki DB into the new DB.
INSERT INTO Card
SELECT
  anki.cards.id,
  1, -- The ID of the deck in the new DB
  REPLACE(SUBSTR(flds, 1, INSTR(flds, x'1f') - 1), '<br>', x'0a'),
  REPLACE(SUBSTR(flds, INSTR(flds, x'1f') + 1), '<br>', x'0a'),
  anki.cards.id
FROM anki.notes
  JOIN anki.cards ON anki.notes.id = anki.cards.nid
WHERE anki.cards.did = 1625145651098; -- The ID of the deck in the Anki DB

-- Migrate answers

INSERT INTO Answer
SELECT
  anki.revlog.cid,
  CASE ease
  WHEN 1 THEN 0
  ELSE 1
  END,
  anki.revlog.id
FROM revlog;

-- Migrate schedule (assume no leeches to migrate)

-- Suspended cards
INSERT INTO Schedule
SELECT
  id,
  NULL,
  NULL,
  0
FROM anki.cards
WHERE queue = -1;

-- Active cards
INSERT INTO Schedule
SELECT
  cid,
  -- Assuming no new cards. Note: ivl is in days
  anki.revlog.id + (anki.revlog.ivl * 24 * 60 * 60 * 1000),
  anki.revlog.ivl,
  0
FROM anki.revlog JOIN anki.cards ON anki.revlog.cid = anki.cards.id
-- Assuming no cards scheduled seconds into the future
WHERE anki.revlog.ivl > 0 AND queue <> -1
GROUP BY cid
HAVING anki.revlog.id = MAX(anki.revlog.id);

PRAGMA user_version = 1;

VACUUM;
