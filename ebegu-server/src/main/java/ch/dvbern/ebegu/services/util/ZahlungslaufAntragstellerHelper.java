/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.entities.Zahlungsposition;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.VerfuegungsZeitabschnittZahlungsstatus;
import ch.dvbern.ebegu.enums.ZahlungStatus;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Helper fuer die Auszahlung der Mahlzeitenverguenstigung der Gemeinde an die Antragsteller
 */
public class ZahlungslaufAntragstellerHelper implements ZahlungslaufHelper {

	@Nonnull
	@Override
	public ZahlungslaufTyp getZahlungslaufTyp() {
		return ZahlungslaufTyp.GEMEINDE_ANTRAGSTELLER;
	}

	@Nonnull
	@Override
	public VerfuegungsZeitabschnittZahlungsstatus getZahlungsstatus(@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		return zeitabschnitt.getZahlungsstatusMahlzeitenverguenstigung();
	}

	@Nonnull
	@Override
	public Zahlung findZahlungForEmpfaenger(
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		@Nonnull Zahlungsauftrag zahlungsauftrag,
		@Nonnull Map<String, Zahlung> zahlungProInstitution
	) {
		final Betreuung betreuung = zeitabschnitt.getVerfuegung().getBetreuung();
		Objects.requireNonNull(betreuung);
		Gesuch gesuch = betreuung.extractGesuch();
		// Wir setzen als "Empfaenger-ID" die ID des Falles: In selben Zahlungslauf kann es zu Auszahlungen
		// von mehreren Mutation derselben Familie kommen, daher waere die Gesuch-ID oder die Gesuchsteller-ID
		// nicht geeignet. Da auch Korrekturzahlungen ueber die Periode hinaus moeglich sind, faellt auch
		// die Dossier-ID weg.
		String fallId = gesuch.getDossier().getFall().getId();
		if (zahlungProInstitution.containsKey(fallId)) {
			return zahlungProInstitution.get(fallId);
		}
		// Es gibt noch keine Zahlung fuer diesen Empfaenger, wir erstellen eine Neue
		Zahlung zahlung = createZahlung(gesuch, betreuung.getBetreuungsangebotTyp(), fallId, zahlungsauftrag);
		zahlungProInstitution.put(fallId, zahlung);
		return zahlung;
	}

	@Nonnull
	private Zahlung createZahlung(
		@Nonnull Gesuch gesuch,
		@Nonnull BetreuungsangebotTyp betreuungsangebotTyp,
		@Nonnull String fallId,
		@Nonnull Zahlungsauftrag zahlungsauftrag
	) {
		final Familiensituation familiensituation = gesuch.extractFamiliensituation();
		Objects.requireNonNull(familiensituation, "Die Familiensituation muessen zu diesem Zeitpunkt definiert sein");

		final Auszahlungsdaten auszahlungsdaten = familiensituation.getAuszahlungsdaten();
		Objects.requireNonNull(auszahlungsdaten, "Die Auszahlungsdaten muessen zu diesem Zeitpunkt definiert sein");

		final Gesuchsteller gesuchsteller1 = gesuch.extractGesuchsteller1()
			.orElseThrow(() -> new EbeguRuntimeException("createZahlung", "GS1 not found for Gesuch " + gesuch.getId()));

		Zahlung zahlung = new Zahlung();
		zahlung.setStatus(ZahlungStatus.ENTWURF);
		zahlung.setAuszahlungsdaten(auszahlungsdaten);
		zahlung.setEmpfaengerId(fallId);
		zahlung.setEmpfaengerName(gesuchsteller1.getFullName());
		zahlung.setBetreuungsangebotTyp(betreuungsangebotTyp);
		zahlung.setZahlungsauftrag(zahlungsauftrag);
		zahlungsauftrag.getZahlungen().add(zahlung);
		return zahlung;
	}

	@Nonnull
	@Override
	public BigDecimal getAuszahlungsbetrag(@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		BigDecimal auszahlungsbetrag = BigDecimal.ZERO;
		auszahlungsbetrag = MathUtil.DEFAULT.addNullSafe(
			auszahlungsbetrag,
			zeitabschnitt.getRelevantBgCalculationResult().getVerguenstigungHauptmahlzeitenTotal(),
			zeitabschnitt.getRelevantBgCalculationResult().getVerguenstigungNebenmahlzeitenTotal());
		return auszahlungsbetrag;
	}

	@Nonnull
	@Override
	public Adresse getAuszahlungsadresseOrDefaultadresse(@Nonnull Zahlung zahlung) {
		// In erster Prio nehmen wir die speziell definierte Zahlungsadresse
		Adresse auszahlungsadresse = zahlung.getAuszahlungsdaten().getAdresseKontoinhaber();
		if (auszahlungsadresse == null) {
			// Falls keine spezifische Adresse definiert ist, nehmen wir die Wohnadresse des Gesuchstellers
			final Optional<Zahlungsposition> firstZahlungsposition = zahlung.getZahlungspositionen().stream().findFirst();
			if (firstZahlungsposition.isPresent()) {
				final GesuchstellerContainer gesuchsteller1 =
					firstZahlungsposition.get().getVerfuegungZeitabschnitt().getVerfuegung().getPlatz().extractGesuch().getGesuchsteller1();
				Objects.requireNonNull(gesuchsteller1);
				auszahlungsadresse =
					gesuchsteller1.getWohnadresseAm(LocalDate.now());
			}
		}
		// Jetzt muss zwingend eine Adresse vorhanden sein
		Objects.requireNonNull(auszahlungsadresse);
		return auszahlungsadresse;
	}
}
