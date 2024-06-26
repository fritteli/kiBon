/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;

/**
 * Service zum Verwalten von Kindern
 */
public interface DokumentGrundService {

	/**
	 * Speichert den DokumentGrund neu in der DB falls der Key noch nicht existiert. Sonst wird das existierende DokumentGrund aktualisiert
	 *
	 * @param dokumentGrund Das DokumentGrund als DTO
	 */
	@Nonnull
	DokumentGrund saveDokumentGrund(@Nonnull DokumentGrund dokumentGrund);

	/**
	 * @param key PK (id) des Kindes
	 * @return DokumentGrund mit dem gegebenen key oder null falls nicht vorhanden
	 */
	@Nonnull
	Optional<DokumentGrund> findDokumentGrund(@Nonnull String key);

	/**
	 * findet in der DB alle Dokumentgruende eines bestimmten Gesuchs (wenn der user dafuer berechtigt ist)
	 * @param gesuch gesuch dessen Dokumentgruende geladen werden sollen
	 * @return Liste der Dokumentgruende
	 */
	@Nonnull
	Collection<DokumentGrund> findAllDokumentGrundByGesuch(@Nonnull Gesuch gesuch);

	/**
	 * findet in der DB alle Dokumentgreunde eines bestimmten Gesuches
	 * @param gesuch gesuch dessen Dokumentgruende geladen werden sollen
	 * @param doAuthCheck flag zum disabeln des authorization checks fuer interne methoden
	 * @return	 * @return Liste der Dokumentgruende
	 */
	@Nonnull
	Collection<DokumentGrund> findAllDokumentGrundByGesuch(@Nonnull Gesuch gesuch,  boolean doAuthCheck);

	@Nonnull
	Collection<DokumentGrund> findAllDokumentGrundByGesuchAndDokumentType(@Nonnull Gesuch gesuch, @Nonnull DokumentGrundTyp dokumentGrundTyp);

	/**
	 * Aktualisiert die DokumentGrund in der DB
	 *
	 * @param dokumentGrund Die DokumentGrund als DTO
	 */
	@Nullable
	DokumentGrund updateDokumentGrund(@Nonnull DokumentGrund dokumentGrund);

	void removeAllDokumentGrundeFromGesuch(Gesuch gesuch);

	/**
	 * Removes the given dokumentGrund if its list of Dokument is empty
	 */
	void removeIfEmpty(@Nonnull DokumentGrund dokumentGrund);
}
