ALTER TABLE kind
	CHANGE kinderabzug kinderabzug_erstes_halbjahr VARCHAR(36) NOT NULL;
ALTER TABLE kind_aud
	CHANGE kinderabzug kinderabzug_erstes_halbjahr VARCHAR(36);

ALTER TABLE kind
	ADD kinderabzug_zweites_halbjahr VARCHAR(36);
ALTER TABLE kind_aud
	ADD kinderabzug_zweites_halbjahr VARCHAR(36);

# copy existing value into new field.
UPDATE kind SET kinderabzug_zweites_halbjahr = kinderabzug_erstes_halbjahr;

# make field not null
ALTER TABLE kind MODIFY kinderabzug_zweites_halbjahr VARCHAR(36) NOT NULL;