ALTER TABLE betreuungsmitteilung_pensum ADD COLUMN monatliche_hauptmahlzeiten INTEGER NOT NULL DEFAULT 0;
ALTER TABLE betreuungsmitteilung_pensum_aud ADD COLUMN monatliche_hauptmahlzeiten INTEGER;

ALTER TABLE betreuungspensum ADD COLUMN monatliche_hauptmahlzeiten INTEGER NOT NULL DEFAULT 0;
ALTER TABLE betreuungspensum_aud ADD COLUMN monatliche_hauptmahlzeiten INTEGER;

ALTER TABLE betreuungspensum_abweichung ADD COLUMN monatliche_hauptmahlzeiten INTEGER NOT NULL DEFAULT 0;
ALTER TABLE betreuungspensum_abweichung_aud ADD COLUMN monatliche_hauptmahlzeiten INTEGER;

ALTER TABLE betreuungsmitteilung_pensum ADD COLUMN monatliche_nebenmahlzeiten INTEGER NOT NULL DEFAULT 0;
ALTER TABLE betreuungsmitteilung_pensum_aud ADD COLUMN monatliche_nebenmahlzeiten INTEGER;

ALTER TABLE betreuungspensum ADD COLUMN monatliche_nebenmahlzeiten INTEGER NOT NULL DEFAULT 0;
ALTER TABLE betreuungspensum_aud ADD COLUMN monatliche_nebenmahlzeiten INTEGER;

ALTER TABLE betreuungspensum_abweichung ADD COLUMN monatliche_nebenmahlzeiten INTEGER NOT NULL DEFAULT 0;
ALTER TABLE betreuungspensum_abweichung_aud ADD COLUMN monatliche_nebenmahlzeiten INTEGER;
