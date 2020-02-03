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

package ch.dvbern.ebegu.entities;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import com.google.common.base.MoreObjects;
import org.apache.commons.lang.Validate;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.MathUtil.roundToFrankenRappen;

@Entity
@Audited
public class BGCalculationResult extends AbstractEntity {

	private static final long serialVersionUID = 6727717920099112569L;

	// Dies wird benötigt für die Migration der Daten und kann im nächsten Release wieder entfernt werden
	@SuppressWarnings("PMD.UnusedPrivateField")
	@Column(nullable = true)
	private String tempIdZeitabschnitt;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal vollkosten = BigDecimal.ZERO; // Punkt IV auf der Verfuegung

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal verguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.ZERO; // Punkt V auf der Verfuegung

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal verguenstigungOhneBeruecksichtigungMinimalbeitrag = BigDecimal.ZERO; // Punkt VI auf der Verfuegung

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal minimalerElternbeitrag = BigDecimal.ZERO; //TODO (hefr) brauchts mich???

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal elternbeitrag = BigDecimal.ZERO; //TODO (hefr) brauchts mich?

	@Nullable // Aktuell noch nullable, wegen bereits verfuegten Gesuchen!
	@Column(nullable = true)
	private BigDecimal minimalerElternbeitragGekuerzt; // Punkt VII auf der Verfuegung

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal verguenstigung = BigDecimal.ZERO; // Punkt VIII auf der Verfuegung

	@NotNull @Nonnull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = Constants.DB_DEFAULT_SHORT_LENGTH)
	private PensumUnits zeiteinheit = PensumUnits.DAYS;

	@Min(0)
	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal betreuungspensumProzent = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal betreuungspensumZeiteinheit = BigDecimal.ZERO;

	@Max(100) @Min(0)
	@Nonnull
	@Column(nullable = false)
	private int anspruchspensumProzent;

	@NotNull @Nonnull
	@Min(0)
	@Column(nullable = false)
	private BigDecimal anspruchspensumZeiteinheit = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Min(0)
	@Column(nullable = false)
	private BigDecimal bgPensumZeiteinheit = BigDecimal.ZERO;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Integer einkommensjahr;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzugFamGroesse = null;

	@Nullable
	@Column(nullable = true)
	private BigDecimal famGroesse = null;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal massgebendesEinkommenVorAbzugFamgr = BigDecimal.ZERO;

	@Column(nullable = false)
	private boolean zuSpaetEingereicht;

	@Column(nullable = false)
	private boolean minimalesEwpUnterschritten;

	@Column(nullable = false)
	private boolean besondereBeduerfnisseBestaetigt;


	@Transient
	@Nonnull
	private Function<BigDecimal, BigDecimal> zeiteinheitenRoundingStrategy = MathUtil::toTwoKommastelle;

	public BGCalculationResult() {

	}

	public BGCalculationResult(@Nonnull BGCalculationResult toCopy) {
		this.vollkosten = toCopy.vollkosten;
		this.verguenstigungOhneBeruecksichtigungVollkosten = toCopy.verguenstigungOhneBeruecksichtigungVollkosten;
		this.verguenstigungOhneBeruecksichtigungMinimalbeitrag = toCopy.verguenstigungOhneBeruecksichtigungMinimalbeitrag;
		this.minimalerElternbeitrag = toCopy.minimalerElternbeitrag;
		this.elternbeitrag = toCopy.elternbeitrag;
		this.minimalerElternbeitragGekuerzt = toCopy.minimalerElternbeitragGekuerzt;
		this.verguenstigung = toCopy.verguenstigung;

		this.zeiteinheit = toCopy.zeiteinheit;
		this.betreuungspensumProzent = toCopy.betreuungspensumProzent;
		this.betreuungspensumZeiteinheit = toCopy.betreuungspensumZeiteinheit;
		this.anspruchspensumProzent = toCopy.anspruchspensumProzent;
		this.anspruchspensumZeiteinheit = toCopy.anspruchspensumZeiteinheit;
		this.bgPensumZeiteinheit = toCopy.bgPensumZeiteinheit;

		this.einkommensjahr = toCopy.einkommensjahr;
		this.abzugFamGroesse = toCopy.abzugFamGroesse;
		this.famGroesse = toCopy.famGroesse;
		this.massgebendesEinkommenVorAbzugFamgr = toCopy.massgebendesEinkommenVorAbzugFamgr;

		this.besondereBeduerfnisseBestaetigt = toCopy.besondereBeduerfnisseBestaetigt;
		this.zuSpaetEingereicht = toCopy.zuSpaetEingereicht;
		this.minimalesEwpUnterschritten = toCopy.minimalesEwpUnterschritten;
	}

	public boolean isCloseTo(@Nonnull BGCalculationResult that) {
		BigDecimal rapenError = BigDecimal.valueOf(0.20);
		// Folgende Attribute sollen bei einer "kleinen" Änderung nicht zu einer Neuberechnung führen:
		return MathUtil.isSame(vollkosten, that.vollkosten)
			&& MathUtil.isClose(verguenstigungOhneBeruecksichtigungVollkosten, that.getVerguenstigungOhneBeruecksichtigungVollkosten(), rapenError)
			&& MathUtil.isClose(verguenstigungOhneBeruecksichtigungMinimalbeitrag, that.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag(), rapenError)
			&& MathUtil.isClose(minimalerElternbeitrag, that.getMinimalerElternbeitrag(), rapenError)
			&& MathUtil.isClose(elternbeitrag, that.getElternbeitrag(), rapenError)
			&& MathUtil.isClose(this.getMinimalerElternbeitragGekuerztNullSafe(), that.getMinimalerElternbeitragGekuerztNullSafe(), rapenError)
			&& MathUtil.isClose(verguenstigung, that.getVerguenstigung(), rapenError)
			&& MathUtil.isClose(this.getBgPensumProzent(), that.getBgPensumProzent(), rapenError);
	}

	public void copyCalculationResult(@Nullable BGCalculationResult that) {
		if (that == null){
			return;
		}
		verguenstigungOhneBeruecksichtigungVollkosten = that.verguenstigungOhneBeruecksichtigungVollkosten;
		verguenstigungOhneBeruecksichtigungMinimalbeitrag = that.verguenstigungOhneBeruecksichtigungMinimalbeitrag;
		minimalerElternbeitrag = that.minimalerElternbeitrag;
		elternbeitrag = that.elternbeitrag;
		minimalerElternbeitragGekuerzt = that.minimalerElternbeitragGekuerzt;
		verguenstigung = that.verguenstigung;
	}

	public void roundAllValues() {
		this.vollkosten = roundToFrankenRappen(vollkosten);
		this.verguenstigungOhneBeruecksichtigungVollkosten = roundToFrankenRappen(verguenstigungOhneBeruecksichtigungVollkosten);
		this.verguenstigungOhneBeruecksichtigungMinimalbeitrag = roundToFrankenRappen(verguenstigungOhneBeruecksichtigungMinimalbeitrag);
		this.minimalerElternbeitrag = roundToFrankenRappen(minimalerElternbeitrag);
		this.elternbeitrag = roundToFrankenRappen(elternbeitrag);
		this.minimalerElternbeitragGekuerzt = roundToFrankenRappen(minimalerElternbeitragGekuerzt);
		this.verguenstigung = roundToFrankenRappen(verguenstigung);

		this.betreuungspensumZeiteinheit = zeiteinheitenRoundingStrategy.apply(betreuungspensumZeiteinheit);
		this.betreuungspensumProzent = zeiteinheitenRoundingStrategy.apply(betreuungspensumProzent);
		this.anspruchspensumZeiteinheit = zeiteinheitenRoundingStrategy.apply(anspruchspensumZeiteinheit);
		this.bgPensumZeiteinheit = zeiteinheitenRoundingStrategy.apply(bgPensumZeiteinheit);

		this.abzugFamGroesse = zeiteinheitenRoundingStrategy.apply(abzugFamGroesse);
		if (this.famGroesse != null) {
			this.famGroesse = MathUtil.toOneKommastelle(famGroesse);
		}

		this.abzugFamGroesse = zeiteinheitenRoundingStrategy.apply(abzugFamGroesse);
		this.massgebendesEinkommenVorAbzugFamgr = zeiteinheitenRoundingStrategy.apply(massgebendesEinkommenVorAbzugFamgr);
		this.abzugFamGroesse = zeiteinheitenRoundingStrategy.apply(abzugFamGroesse);
		this.abzugFamGroesse = zeiteinheitenRoundingStrategy.apply(abzugFamGroesse);
		this.abzugFamGroesse = zeiteinheitenRoundingStrategy.apply(abzugFamGroesse);
	}

	@Override
	@Nonnull
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("vollkosten", vollkosten)
			.add("verguenstigungOhneBeruecksichtigungVollkosten", verguenstigungOhneBeruecksichtigungVollkosten)
			.add("verguenstigungOhneBeruecksichtigungMinimalbeitrag", verguenstigungOhneBeruecksichtigungMinimalbeitrag)
			.add("minimalerElternbeitrag", minimalerElternbeitrag)
			.add("elternbeitrag", elternbeitrag)
			.add("verguenstigung", verguenstigung)

			.add("zeiteinheit", zeiteinheit)
			.add("betreuungspensumZeiteinheit", betreuungspensumZeiteinheit)
			.add("betreuungspensumProzent", betreuungspensumProzent)
			.add("anspruchsberechtigteAnzahlZeiteinheiten", anspruchspensumZeiteinheit)
			.add("anspruchberechtigtesPensum", anspruchspensumProzent)
			.add("getBgPensumProzent", getBgPensumProzent())
			.add("bgPensumZeiteinheit", bgPensumZeiteinheit)

			.add("einkommensjahr", einkommensjahr)
			.add("massgebendesEinkommenVorAbzugFamgr", massgebendesEinkommenVorAbzugFamgr)
			.add("abzugFamGroesse", abzugFamGroesse)
			.toString();
	}

	@Override
	public boolean isSame(@Nullable AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof BGCalculationResult)) {
			return false;
		}
		final BGCalculationResult otherResult = (BGCalculationResult) other;
		return isSameZeiteinheiten(this, otherResult) &&
			MathUtil.isSame(betreuungspensumProzent, otherResult.betreuungspensumProzent) &&
			this.anspruchspensumProzent == otherResult.anspruchspensumProzent &&
			MathUtil.isSame(abzugFamGroesse, otherResult.abzugFamGroesse) &&
			MathUtil.isSame(famGroesse, otherResult.famGroesse) &&
			MathUtil.isSame(massgebendesEinkommenVorAbzugFamgr, otherResult.massgebendesEinkommenVorAbzugFamgr) &&
			zuSpaetEingereicht == otherResult.zuSpaetEingereicht &&
			minimalesEwpUnterschritten == otherResult.minimalesEwpUnterschritten &&
			Objects.equals(einkommensjahr, otherResult.einkommensjahr) &&
			besondereBeduerfnisseBestaetigt == otherResult.besondereBeduerfnisseBestaetigt;
	}

	public static boolean isSameSichtbareDaten(@Nullable BGCalculationResult thisEntity, @Nullable BGCalculationResult otherEntity) {
		return (thisEntity == null && otherEntity == null)
			|| (thisEntity != null && otherEntity != null && (
				MathUtil.isSame(thisEntity.betreuungspensumZeiteinheit, otherEntity.betreuungspensumZeiteinheit) &&
				MathUtil.isSame(thisEntity.vollkosten, otherEntity.vollkosten) &&
				MathUtil.isSame(thisEntity.elternbeitrag, otherEntity.elternbeitrag) &&
				MathUtil.isSame(thisEntity.betreuungspensumProzent, otherEntity.betreuungspensumProzent) &&
				thisEntity.anspruchspensumProzent == otherEntity.anspruchspensumProzent &&
				MathUtil.isSame(thisEntity.abzugFamGroesse, otherEntity.abzugFamGroesse) &&
				MathUtil.isSame(thisEntity.famGroesse, otherEntity.famGroesse) &&
				MathUtil.isSame(thisEntity.massgebendesEinkommenVorAbzugFamgr, otherEntity.massgebendesEinkommenVorAbzugFamgr) &&
				Objects.equals(thisEntity.einkommensjahr, otherEntity.einkommensjahr) &&
				(thisEntity.besondereBeduerfnisseBestaetigt == otherEntity.besondereBeduerfnisseBestaetigt) &&
				(thisEntity.minimalesEwpUnterschritten == otherEntity.minimalesEwpUnterschritten)
			));
	}

	/**
	 * Vergleich nur die relevanten Daten fuer die Berechnung einer Verfuegung.
	 */
	public static boolean isSameBerechnung(@Nullable BGCalculationResult thisEntity, @Nullable BGCalculationResult otherEntity) {
		return (thisEntity == null && otherEntity == null)
			|| (thisEntity != null && otherEntity != null && (
			MathUtil.isSame(thisEntity.verguenstigung, otherEntity.verguenstigung) &&
			MathUtil.isSame(thisEntity.getBgPensumProzent(), otherEntity.getBgPensumProzent()) &&
			MathUtil.isSame(thisEntity.minimalerElternbeitragGekuerzt, otherEntity.minimalerElternbeitragGekuerzt) &&
			thisEntity.anspruchspensumProzent == otherEntity.anspruchspensumProzent
		));
	}

	/**
	 * Aller persistierten Daten ohne Kommentar
	 */
	@SuppressWarnings({ "OverlyComplexBooleanExpression", "AccessingNonPublicFieldOfAnotherObject",
		"QuestionableName" })
	public static boolean isSamePersistedValues(@Nullable BGCalculationResult thisEntity, @Nullable BGCalculationResult otherEntity) {
		// zuSpaetEingereicht und zahlungsstatus sind hier nicht aufgefuehrt, weil;
		// Es sollen die Resultate der Verfuegung verglichen werden und nicht der Weg, wie wir zu diesem Resultat
		// gelangt sind
		return (thisEntity == null && otherEntity == null)
			|| (thisEntity != null && otherEntity != null && (
				MathUtil.isSame(thisEntity.betreuungspensumZeiteinheit, otherEntity.betreuungspensumZeiteinheit) &&
				MathUtil.isSame(thisEntity.vollkosten, otherEntity.vollkosten) &&
				MathUtil.isSame(thisEntity.elternbeitrag, otherEntity.elternbeitrag) &&
				MathUtil.isSame(thisEntity.betreuungspensumProzent, otherEntity.betreuungspensumProzent) &&
				thisEntity.anspruchspensumProzent == otherEntity.anspruchspensumProzent &&
				MathUtil.isSame(thisEntity.abzugFamGroesse, otherEntity.abzugFamGroesse) &&
				MathUtil.isSame(thisEntity.famGroesse, otherEntity.famGroesse) &&
				MathUtil.isSame(thisEntity.massgebendesEinkommenVorAbzugFamgr, otherEntity.massgebendesEinkommenVorAbzugFamgr) &&
				Objects.equals(thisEntity.einkommensjahr, otherEntity.einkommensjahr) &&
				(thisEntity.minimalesEwpUnterschritten == otherEntity.minimalesEwpUnterschritten) &&
				isSameZeiteinheiten(thisEntity, otherEntity)
		));
	}

	public void add(@Nonnull BGCalculationResult other) {
		this.betreuungspensumZeiteinheit = this.betreuungspensumZeiteinheit.add(other.betreuungspensumZeiteinheit);
		this.betreuungspensumProzent = this.betreuungspensumProzent.add(other.betreuungspensumProzent);
		this.anspruchspensumZeiteinheit = this.anspruchspensumZeiteinheit.add(other.anspruchspensumZeiteinheit);
		this.anspruchspensumProzent = this.anspruchspensumProzent + other.anspruchspensumProzent;
		this.bgPensumZeiteinheit = this.bgPensumZeiteinheit.add(other.bgPensumZeiteinheit);

		this.einkommensjahr = other.einkommensjahr;
		this.massgebendesEinkommenVorAbzugFamgr = this.massgebendesEinkommenVorAbzugFamgr.add(other.massgebendesEinkommenVorAbzugFamgr);

		// Die Felder betreffend Familienabzug können nicht linear addiert werden. Es darf also nie Überschneidungen geben!
		if (other.getAbzugFamGroesse() != null) {
			Validate.isTrue(this.getAbzugFamGroesse() == null, "Familiengoressenabzug kann nicht gemerged werden");
			this.setAbzugFamGroesse(other.getAbzugFamGroesse());
		}
		// Die Familiengroesse kann nicht linear addiert werden, daher darf es hier nie uebschneidungen geben
		if (other.getFamGroesse() != null) {
			Validate.isTrue(this.getFamGroesse() == null, "Familiengoressen kann nicht gemerged werden");
			this.setFamGroesse(other.getFamGroesse());
		}

		this.zuSpaetEingereicht = this.zuSpaetEingereicht || other.zuSpaetEingereicht;
		this.besondereBeduerfnisseBestaetigt = this.besondereBeduerfnisseBestaetigt || other.besondereBeduerfnisseBestaetigt;
		this.minimalesEwpUnterschritten = this.minimalesEwpUnterschritten || other.minimalesEwpUnterschritten;
	}

	private static boolean isSameZeiteinheiten(@Nonnull BGCalculationResult thisEntity, @Nonnull BGCalculationResult otherEntity) {
		return MathUtil.isSame(thisEntity.bgPensumZeiteinheit, otherEntity.bgPensumZeiteinheit) &&
			MathUtil.isSame(thisEntity.anspruchspensumZeiteinheit, otherEntity.anspruchspensumZeiteinheit) &&
			thisEntity.zeiteinheit == otherEntity.zeiteinheit;
	}

	/**
	 * Das BG-Pensum (Pensum des Gutscheins) wird zum BG-Tarif berechnet und kann höchstens so gross sein, wie das
	 * Betreuungspensum.
	 * Falls das anspruchsberechtigte Pensum unter dem Betreuungspensum liegt, entspricht das BG-Pensum dem
	 * anspruchsberechtigten Pensum.
	 * <p>
	 * Ein Kind mit einem Betreuungspensum von 60% und einem anspruchsberechtigten Pensum von 40% hat ein BG-Pensum
	 * von 40%.
	 * Ein Kind mit einem Betreuungspensum von 40% und einem anspruchsberechtigten Pensum von 60% hat ein BG-Pensum
	 * von 40%.
	 */
	@Nonnull
	public BigDecimal getBgPensumProzent() {
		return getBetreuungspensumProzent().min(MathUtil.DEFAULT.from(getAnspruchspensumProzent()));
	}

	/**
	 * @return berechneter Wert. Zieht vom massgebenenEinkommenVorAbzug den Familiengroessen Abzug ab
	 */
	@Nonnull
	public BigDecimal getMassgebendesEinkommen() {
		BigDecimal abzugFamSize = this.abzugFamGroesse;
		return MathUtil.DEFAULT.subtractNullSafe(this.massgebendesEinkommenVorAbzugFamgr, abzugFamSize);
	}

	@Nonnull
	public BigDecimal getMinimalerElternbeitragGekuerztNullSafe() {
		// Spezialfall: Dieses Feld wurde frueher nicht gespeichert sondern immer neu berechnet
		// Dies bedeutet dass in allen bereits verfügten Gesuchen der Wert nie mehr gesetzt werden
		// kann!
		// Kann entfernt werden, wenn Gesuschsperiode 2019/20 gelöscht wurde,
		// minimalerElternbeitragGekuerzt kann dann Nonnull gesetzt werden
		if (getMinimalerElternbeitragGekuerzt() != null) {
			return getMinimalerElternbeitragGekuerzt();
		}
		BigDecimal vollkostenMinusVerguenstigung = MathUtil.DEFAULT
			.subtract(getVollkosten(), getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		if (vollkostenMinusVerguenstigung.compareTo(getMinimalerElternbeitrag()) > 0) {
			return MathUtil.DEFAULT.from(0);
		}
		return MathUtil.DEFAULT.subtract(getMinimalerElternbeitrag(), vollkostenMinusVerguenstigung);
	}



	@Nonnull
	public BigDecimal getMinimalerElternbeitrag() {
		return minimalerElternbeitrag;
	}

	public void setMinimalerElternbeitrag(@Nonnull BigDecimal minimalerElternbeitrag) {
		this.minimalerElternbeitrag = minimalerElternbeitrag;
	}

	@Nonnull
	public BigDecimal getVerguenstigungOhneBeruecksichtigungMinimalbeitrag() {
		return verguenstigungOhneBeruecksichtigungMinimalbeitrag;
	}

	public void setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(@Nonnull BigDecimal verguenstigungOhneBeruecksichtigungMinimalbeitrag) {
		this.verguenstigungOhneBeruecksichtigungMinimalbeitrag = verguenstigungOhneBeruecksichtigungMinimalbeitrag;
	}

	@Nonnull
	public BigDecimal getVerguenstigungOhneBeruecksichtigungVollkosten() {
		return verguenstigungOhneBeruecksichtigungVollkosten;
	}

	public void setVerguenstigungOhneBeruecksichtigungVollkosten(@Nonnull BigDecimal verguenstigungOhneBeruecksichtigungVollkosten) {
		this.verguenstigungOhneBeruecksichtigungVollkosten = verguenstigungOhneBeruecksichtigungVollkosten;
	}

	@Nullable
	public BigDecimal getMinimalerElternbeitragGekuerzt() {
		return minimalerElternbeitragGekuerzt;
	}

	public void setMinimalerElternbeitragGekuerzt(@Nonnull BigDecimal minimalerElternbeitragGekuerzt) {
		this.minimalerElternbeitragGekuerzt = minimalerElternbeitragGekuerzt;
	}

	@Nonnull
	public BigDecimal getVerguenstigung() {
		return verguenstigung;
	}

	public void setVerguenstigung(@Nonnull BigDecimal verguenstigung) {
		this.verguenstigung = verguenstigung;
	}

	@Nonnull
	public BigDecimal getVollkosten() {
		return vollkosten;
	}

	public void setVollkosten(@Nonnull BigDecimal vollkosten) {
		this.vollkosten = vollkosten;
	}

	@Nonnull
	public BigDecimal getElternbeitrag() {
		return elternbeitrag;
	}

	public void setElternbeitrag(@Nonnull BigDecimal elternbeitrag) {
		this.elternbeitrag = elternbeitrag;
	}

	@Nonnull
	public BigDecimal getBgPensumZeiteinheit() {
		return bgPensumZeiteinheit;
	}

	public void setBgPensumZeiteinheit(@Nonnull BigDecimal bgPensumZeiteinheit) {
		this.bgPensumZeiteinheit = bgPensumZeiteinheit;
	}

	@Nonnull
	public BigDecimal getAnspruchspensumZeiteinheit() {
		return anspruchspensumZeiteinheit;
	}

	public void setAnspruchspensumZeiteinheit(
		@Nonnull BigDecimal anspruchspensumZeiteinheit) {
		this.anspruchspensumZeiteinheit = anspruchspensumZeiteinheit;
	}

	@Nonnull
	public PensumUnits getZeiteinheit() {
		return zeiteinheit;
	}

	public void setZeiteinheit(@Nonnull PensumUnits zeiteinheit) {
		this.zeiteinheit = zeiteinheit;
	}

	@Nonnull
	public BigDecimal getBetreuungspensumZeiteinheit() {
		return betreuungspensumZeiteinheit;
	}

	public void setBetreuungspensumZeiteinheit(@Nonnull BigDecimal betreuungspensumZeiteinheit) {
		this.betreuungspensumZeiteinheit = betreuungspensumZeiteinheit;
	}

	@Nonnull
	public Function<BigDecimal, BigDecimal> getZeiteinheitenRoundingStrategy() {
		return zeiteinheitenRoundingStrategy;
	}

	public void setZeiteinheitenRoundingStrategy(@Nonnull Function<BigDecimal, BigDecimal> strategy) {
		this.zeiteinheitenRoundingStrategy = strategy;
	}

	@Nonnull
	public BigDecimal getBetreuungspensumProzent() {
		return betreuungspensumProzent;
	}

	public void setBetreuungspensumProzent(@Nonnull BigDecimal betreuungspensumProzent) {
		this.betreuungspensumProzent = betreuungspensumProzent;
	}

	public int getAnspruchspensumProzent() {
		return anspruchspensumProzent;
	}

	public void setAnspruchspensumProzent(int anspruchspensumProzent) {
		this.anspruchspensumProzent = anspruchspensumProzent;
	}

	@Nonnull
	public Integer getEinkommensjahr() {
		return einkommensjahr;
	}

	public void setEinkommensjahr(@Nonnull Integer einkommensjahr) {
		this.einkommensjahr = einkommensjahr;
	}

	@Nullable
	public BigDecimal getAbzugFamGroesse() {
		return abzugFamGroesse;
	}

	public void setAbzugFamGroesse(@Nullable BigDecimal abzugFamGroesse) {
		this.abzugFamGroesse = abzugFamGroesse;
	}

	@Nullable
	public BigDecimal getFamGroesse() {
		return famGroesse;
	}

	public void setFamGroesse(@Nullable BigDecimal famGroesse) {
		this.famGroesse = famGroesse;
	}

	@Nonnull
	public BigDecimal getMassgebendesEinkommenVorAbzugFamgr() {
		return massgebendesEinkommenVorAbzugFamgr;
	}

	public void setMassgebendesEinkommenVorAbzugFamgr(@Nonnull BigDecimal massgebendesEinkommenVorAbzugFamgr) {
		this.massgebendesEinkommenVorAbzugFamgr = massgebendesEinkommenVorAbzugFamgr;
	}

	public boolean isZuSpaetEingereicht() {
		return zuSpaetEingereicht;
	}

	public void setZuSpaetEingereicht(boolean zuSpaetEingereicht) {
		this.zuSpaetEingereicht = zuSpaetEingereicht;
	}

	public boolean isMinimalesEwpUnterschritten() {
		return minimalesEwpUnterschritten;
	}

	public void setMinimalesEwpUnterschritten(boolean minimalesEwpUnterschritten) {
		this.minimalesEwpUnterschritten = minimalesEwpUnterschritten;
	}

	public boolean isBesondereBeduerfnisseBestaetigt() {
		return besondereBeduerfnisseBestaetigt;
	}

	public void setBesondereBeduerfnisseBestaetigt(boolean besondereBeduerfnisseBestaetigt) {
		this.besondereBeduerfnisseBestaetigt = besondereBeduerfnisseBestaetigt;
	}
}
