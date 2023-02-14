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

package ch.dvbern.ebegu.rules;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.enterprise.inject.spi.CDI;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.services.DossierService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;

/**
 * Regel für Wohnsitz in Bern (Zuzug und Wegzug):
 * - Durch Adresse definiert
 * - Anspruch vom ersten Tag des Zuzugs
 * - Anspruch bis 2 Monate nach Wegzug, auf Ende Monat
 * - Kein Doppel-Anspruch bei Umzug mit Betreuung in derselben Institution (KIBON-1843)
 * Verweis 16.8 Der zivilrechtliche Wohnsitz
 */
public class WohnsitzCalcRule extends AbstractCalcRule {

	public WohnsitzCalcRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.WOHNSITZ, RuleType.REDUKTIONSREGEL, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN);
	}

	@SuppressWarnings("PMD.CollapsibleIfStatements")
	@Override
	protected void executeRule(@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData) {
		if (inputData.isWohnsitzNichtInGemeindeGS1()) {
			inputData.setAnspruchZeroAndSaveRestanspruch();
			inputData.addBemerkung(
				MsgKey.WOHNSITZ_MSG,
				getLocale(),
				platz.extractGesuch().getDossier().getGemeinde().getName());
		} else {
			// KIBON_1843 2 Ative gesuche in unterschiedlichen gemeinden möglich
			if (!inputData.getPotentielleDoppelBetreuung()) {
				return;
			}
			DossierService dossierService = CDI.current().select(DossierService.class).get();

			List<Dossier> allDossiersForFallNummer = dossierService.getAllDossiersForFallNummer(
				platz.getKind()
					.getGesuch()
					.getDossier()
					.getFall()
					.getFallNummer());

			List<Gesuch> allGesucheForFallNummer = getAllGesucheForDossiers(allDossiersForFallNummer);
			// Pro Kind im Kindcontainer(dossier)
			List<Betreuung> alleBetreuungen = getAlleBetreuungen(allGesucheForFallNummer);
			Map<String, List<Betreuung>> allBetreuungenProKind = getAllBetreuungenProKind(alleBetreuungen);
			Map<String, List<Betreuung>> relevanteBetreuungen =
				keepRelevantEntries(allBetreuungenProKind, inputData.getParent());
			if (relevanteBetreuungen.size() > 0) {
				if (inputData.getPotentielleDoppelBetreuung()) {
					inputData.setAnspruchZeroAndSaveRestanspruch();
				}
			}
		}
	}

	private Map<String, List<Betreuung>> keepRelevantEntries(
		Map<String, List<Betreuung>> allBetreuungenProKind,
		VerfuegungZeitabschnitt aktuellBerechneterAbschnitt) {
		Map<String, List<Betreuung>> relevantEntries = new HashMap<>();
		for (String key : allBetreuungenProKind.keySet()) {
			List<Betreuung> betreuungList = allBetreuungenProKind.get(key);
			if (betreuungList.size() > 1 &&
				istEineBetreuungVerfuegt(betreuungList) &&
				betrifftGleichePeriode(betreuungList, aktuellBerechneterAbschnitt)) {
				relevantEntries.put(key, betreuungList);
			}
		}
		return relevantEntries;
	}

	private boolean betrifftGleichePeriode(
		final List<Betreuung> betreuungList,
		VerfuegungZeitabschnitt aktuellBerechneterAbschnitt) {
		// eine Betreuung ist verfügt ... und der abschnitt muss in einem Zeitabschnitt der vorhanden sein.
		for (Betreuung b : betreuungList) {
			Verfuegung verfuegung = b.getVerfuegung();
			if (null != verfuegung) {
				List<VerfuegungZeitabschnitt> zeitabschnitte = verfuegung.getZeitabschnitte();
				for (VerfuegungZeitabschnitt abschnitt : zeitabschnitte) {
					if (abschnitt.getGueltigkeit()
						.contains(aktuellBerechneterAbschnitt.getGueltigkeit().getGueltigAb())) {
						return true;
					}

				}
			}
		}
		return false;
	}

	private Map<String, List<Betreuung>> getAllBetreuungenProKind(List<Betreuung> alleBetreuungen) {
		Map<String, List<Betreuung>> betreuungProKindListe = new HashMap<>();
		for (Betreuung betreuung : alleBetreuungen) {
			KindContainer kc = betreuung.getKind();
			String identifier = constructSortIdentifier(kc, betreuung);
			if (null == betreuungProKindListe.get(identifier)) {
				List<Betreuung> betreuungsList = new LinkedList<>();
				betreuungsList.add(betreuung);
				betreuungProKindListe.put(identifier, betreuungsList);
			} else {
				betreuungProKindListe.get(identifier).add(betreuung);
			}

		}
		return betreuungProKindListe;
	}

	private List<Betreuung> getAlleBetreuungen(List<Gesuch> alleGesuche) {
		List<Betreuung> alleBetreuungen = new ArrayList<>();
		for (Gesuch gesuch : alleGesuche) {
			for (KindContainer kc : gesuch.getKindContainers()) {
				alleBetreuungen.addAll(kc.getBetreuungen());
			}
		}
		return alleBetreuungen;
	}

	private boolean istEineBetreuungVerfuegt(List<Betreuung> bList) {
		for (Betreuung b : bList) {
			if (b.getVerfuegung() != null) {
				return true;
			}
		}
		return false;
	}
	private String constructSortIdentifier(KindContainer kc, Betreuung betreuung) {
		final char SPACER = '-';
		//für die Lesbarkeit :-) nur ein String mit
		// <JAHR><Monat><Tag>-<Nachnmae>-<Vorname>-<InstitutionsID>
		return kc.getKindJA().getGeburtsdatum().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + SPACER
			+ kc.getKindJA().getNachname() + SPACER
			+ kc.getKindJA().getVorname() + SPACER
			+ betreuung.getInstitutionStammdaten().getInstitution().getId();
	}

	private List<Gesuch> getAllGesucheForDossiers(List<Dossier> allDossiersForFallNummer) {
		List<Gesuch> alleGesuche = new LinkedList<>();
		GesuchService gesuchService = CDI.current().select(GesuchService.class).get();
		for (Dossier dossier : allDossiersForFallNummer) {
			alleGesuche.addAll(gesuchService.getAllGesuchForDossier(dossier.getId()));
		}
		return alleGesuche;
	}
}
