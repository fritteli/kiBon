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

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.rechner.rules.RechnerRule;

/**
 * Superklasse für BG-Rechner der Gemeinde: Berechnet sowohl nach ASIV wie auch nach Gemeinde-spezifischen Regeln
 */
public abstract class AbstractGemeindeRechner extends AbstractAsivRechner {

	private final List<RechnerRule> rechnerRulesForGemeinde;
	private final RechnerRuleParameterDTO rechnerParameter = new RechnerRuleParameterDTO();


	protected AbstractGemeindeRechner(List<RechnerRule> rechnerRulesForGemeinde) {
		this.rechnerRulesForGemeinde = rechnerRulesForGemeinde;
	}

	/**
	 * Diese Methode fuehrt die Berechnung fuer die uebergebenen Verfuegungsabschnitte durch.
	 */
	@Override
	public void calculateAsivAndGemeinde(
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull List<RechnerRule> rechnerRules
	) {
		// Wir berechnen ASIV und Gemeinde separat:
		doCalculateAsiv(verfuegungZeitabschnitt, parameterDTO);
		doCalculateGemeinde(verfuegungZeitabschnitt, parameterDTO);
	}

	private void prepareRechnerParameterForAsiv() {
		rechnerParameter.reset();
	}

	private void prepareRechnerParameterForGemeinde(
		@Nonnull BGCalculationInput inputGemeinde,
		@Nonnull BGRechnerParameterDTO parameterDTO
	) {
		for (RechnerRule rechnerRule : rechnerRulesForGemeinde) {
			// Diese Pruefung erfolgt eigentlich schon aussen... die Rules die reinkommen sind schon konfiguriert fuer Gemeinde
			if (rechnerRule.isConfigueredForGemeinde(parameterDTO)) {
				rechnerParameter.setHasGemeindeRules(true);
				if (rechnerRule.isRelevantForVerfuegung(inputGemeinde, parameterDTO)) {
					rechnerRule.prepareParameter(inputGemeinde, parameterDTO, rechnerParameter);
				}
			}
		}
	}

	private void doCalculateAsiv(
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		@Nonnull BGRechnerParameterDTO parameterDTO
	) {
		// Fuer ASIV alles zuruecksetzen
		prepareRechnerParameterForAsiv();

		BGCalculationResult resultAsiv = calculateAsiv(verfuegungZeitabschnitt.getBgCalculationInputAsiv(), parameterDTO);
		resultAsiv.roundAllValues();
		verfuegungZeitabschnitt.setBgCalculationResultAsiv(resultAsiv);
	}

	private void doCalculateGemeinde(
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		@Nonnull BGRechnerParameterDTO parameterDTO
	) {
		// Fuer Gemeinde die richtigen Werte setzen
		prepareRechnerParameterForGemeinde(verfuegungZeitabschnitt.getBgCalculationInputGemeinde(), parameterDTO);

		// Jetzt die Berechnung mit den Input-Werten der Gemeinde durchfuehren
		if (rechnerParameter.isHasGemeindeRules()) {
			BGCalculationResult resultGemeinde = calculateAsiv(verfuegungZeitabschnitt.getBgCalculationInputGemeinde(), parameterDTO);
			resultGemeinde.roundAllValues();
			verfuegungZeitabschnitt.setBgCalculationResultGemeinde(resultGemeinde);
		}
	}

	@Nonnull
	@Override
	BigDecimal getVerguenstigungProZeiteinheit(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull Boolean unter12Monate,
		@Nonnull Boolean eingeschult,
		@Nonnull Boolean besonderebeduerfnisse,
		@Nonnull BigDecimal massgebendesEinkommen,
		boolean bezahltVollkosten
	) {
		// "Normale" Verguentigung pro Zeiteinheit
		BigDecimal verguenstigungProZeiteinheit = super.getVerguenstigungProZeiteinheit(parameterDTO, unter12Monate, eingeschult, besonderebeduerfnisse,
			massgebendesEinkommen,
			bezahltVollkosten);
		// Zusaetzlicher Gutschein Gemeinde
		verguenstigungProZeiteinheit = EXACT.addNullSafe(verguenstigungProZeiteinheit, rechnerParameter.getZusaetzlicherGutscheinGemeindeBetrag());
		return verguenstigungProZeiteinheit;
	}
}
