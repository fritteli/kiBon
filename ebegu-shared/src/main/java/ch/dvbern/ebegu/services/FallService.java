package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.entities.Fall;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;

/**
 * Service zum Verwalten von Fallen
 */
public interface FallService {

	/**
	 * Erstellt einen neuen Fall in der DB, falls der key noch nicht existiert. Sollte es existieren, aktualisiert es den Inhalt
	 *
	 * @param fall der Fall als DTO
	 * @return den gespeicherten Fall
	 */
	@Nonnull
	Fall saveFall(@Nonnull Fall fall);

	/**
	 * @param key PK (id) des Falles
	 * @return Fall mit dem gegebenen key oder null falls nicht vorhanden
	 */
	@Nonnull
 	Optional<Fall> findFall(@Nonnull String key);

	@Nonnull
	Optional<Fall> findFallByNumber(@Nonnull Long fallnummer);

	/**
	 * Gibt alle existierenden Faelle zurueck.
	 *
	 * @return Liste aller Faelle aus der DB
	 */
	@Nonnull
	Collection<Fall> getAllFalle();

	/**
	 * entfernt einen Fall aus der Database
	 *
	 * @param fall der Fall als DTO
	 */
	void removeFall(@Nonnull Fall fall);

}
