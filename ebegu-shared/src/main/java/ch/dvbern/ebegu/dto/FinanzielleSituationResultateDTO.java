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

package ch.dvbern.ebegu.dto;

import java.math.BigDecimal;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.util.MathUtil;

/**
 * DTO für die Resultate der Berechnungen der Finanziellen Situation
 */
public class FinanzielleSituationResultateDTO {

	@Nullable
	private BigDecimal geschaeftsgewinnDurchschnittGesuchsteller1 = BigDecimal.ZERO;
	@Nullable
	private BigDecimal geschaeftsgewinnDurchschnittGesuchsteller2 = BigDecimal.ZERO;
	@Nullable
	private BigDecimal einkommenBeiderGesuchsteller = BigDecimal.ZERO;
	private BigDecimal nettovermoegenXProzent = BigDecimal.ZERO;
	private BigDecimal anrechenbaresEinkommen = BigDecimal.ZERO;
	private BigDecimal abzuegeBeiderGesuchsteller = BigDecimal.ZERO;
	private BigDecimal massgebendesEinkVorAbzFamGr = BigDecimal.ZERO;

	private BigDecimal massgebendesEinkVorAbzFamGrGS1 = BigDecimal.ZERO;
	private BigDecimal massgebendesEinkVorAbzFamGrGS2 = BigDecimal.ZERO;

	private BigDecimal bruttolohnJahrGS1 = BigDecimal.ZERO;
	private BigDecimal bruttolohnJahrGS2 = BigDecimal.ZERO;

	public FinanzielleSituationResultateDTO() {
		initToZero();
	}

	private void initToZero() {
		// Alle Werte auf 0 initialisieren, falls Null
		// Wenn negativ -> 0
		geschaeftsgewinnDurchschnittGesuchsteller1 = MathUtil.positiveNonNullAndRound(geschaeftsgewinnDurchschnittGesuchsteller1);
		geschaeftsgewinnDurchschnittGesuchsteller2 = MathUtil.positiveNonNullAndRound(geschaeftsgewinnDurchschnittGesuchsteller2);
		einkommenBeiderGesuchsteller = MathUtil.positiveNonNullAndRound(einkommenBeiderGesuchsteller);
		nettovermoegenXProzent = MathUtil.positiveNonNullAndRound(nettovermoegenXProzent);
		anrechenbaresEinkommen = MathUtil.positiveNonNullAndRound(anrechenbaresEinkommen);
		abzuegeBeiderGesuchsteller = MathUtil.positiveNonNullAndRound(abzuegeBeiderGesuchsteller);
		massgebendesEinkVorAbzFamGr = MathUtil.positiveNonNullAndRound(massgebendesEinkVorAbzFamGr);
	}

	@Nullable
	public BigDecimal getGeschaeftsgewinnDurchschnittGesuchsteller1() {
		return geschaeftsgewinnDurchschnittGesuchsteller1;
	}

	public void setGeschaeftsgewinnDurchschnittGesuchsteller1(@Nullable BigDecimal geschaeftsgewinnDurchschnittGesuchsteller1) {
		this.geschaeftsgewinnDurchschnittGesuchsteller1 = geschaeftsgewinnDurchschnittGesuchsteller1;
	}

	@Nullable
	public BigDecimal getGeschaeftsgewinnDurchschnittGesuchsteller2() {
		return geschaeftsgewinnDurchschnittGesuchsteller2;
	}

	public void setGeschaeftsgewinnDurchschnittGesuchsteller2(@Nullable BigDecimal geschaeftsgewinnDurchschnittGesuchsteller2) {
		this.geschaeftsgewinnDurchschnittGesuchsteller2 = geschaeftsgewinnDurchschnittGesuchsteller2;
	}

	@Nullable
	public BigDecimal getEinkommenBeiderGesuchsteller() {
		return einkommenBeiderGesuchsteller;
	}

	public void setEinkommenBeiderGesuchsteller(@Nullable BigDecimal einkommenBeiderGesuchsteller) {
		this.einkommenBeiderGesuchsteller = einkommenBeiderGesuchsteller;
	}

	public BigDecimal getNettovermoegenXProzent() {
		return nettovermoegenXProzent;
	}

	public void setNettovermoegenXProzent(BigDecimal nettovermoegenXProzent) {
		this.nettovermoegenXProzent = nettovermoegenXProzent;
	}

	public BigDecimal getAnrechenbaresEinkommen() {
		return anrechenbaresEinkommen;
	}

	public void setAnrechenbaresEinkommen(BigDecimal anrechenbaresEinkommen) {
		this.anrechenbaresEinkommen = anrechenbaresEinkommen;
	}

	public BigDecimal getAbzuegeBeiderGesuchsteller() {
		return abzuegeBeiderGesuchsteller;
	}

	public void setAbzuegeBeiderGesuchsteller(BigDecimal abzuegeBeiderGesuchsteller) {
		this.abzuegeBeiderGesuchsteller = abzuegeBeiderGesuchsteller;
	}

	public BigDecimal getMassgebendesEinkVorAbzFamGr() {
		return massgebendesEinkVorAbzFamGr;
	}

	public void setMassgebendesEinkVorAbzFamGr(BigDecimal massgebendesEinkVorAbzFamGr) {
		this.massgebendesEinkVorAbzFamGr = massgebendesEinkVorAbzFamGr;
	}

	public BigDecimal getMassgebendesEinkVorAbzFamGrGS1() {
		return massgebendesEinkVorAbzFamGrGS1;
	}

	public void setMassgebendesEinkVorAbzFamGrGS1(BigDecimal massgebendesEinkVorAbzFamGrGS1) {
		this.massgebendesEinkVorAbzFamGrGS1 = massgebendesEinkVorAbzFamGrGS1;
	}

	public BigDecimal getMassgebendesEinkVorAbzFamGrGS2() {
		return massgebendesEinkVorAbzFamGrGS2;
	}

	public void setMassgebendesEinkVorAbzFamGrGS2(BigDecimal massgebendesEinkVorAbzFamGrGS2) {
		this.massgebendesEinkVorAbzFamGrGS2 = massgebendesEinkVorAbzFamGrGS2;
	}

	public BigDecimal getBruttolohnJahrGS1() {
		return bruttolohnJahrGS1;
	}

	public void setBruttolohnJahrGS1(BigDecimal bruttolohnJahrGS1) {
		this.bruttolohnJahrGS1 = bruttolohnJahrGS1;
	}

	public BigDecimal getBruttolohnJahrGS2() {
		return bruttolohnJahrGS2;
	}

	public void setBruttolohnJahrGS2(BigDecimal bruttolohnJahrGS2) {
		this.bruttolohnJahrGS2 = bruttolohnJahrGS2;
	}
}
