/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.dtos.gemeindeantrag;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;
import ch.dvbern.ebegu.api.dtos.JaxAdresse;
import ch.dvbern.ebegu.api.dtos.JaxGemeinde;

public class JaxFerienbetreuungAngabenAngebot extends JaxAbstractDTO {

	private static final long serialVersionUID = -3543367177495435040L;

	@Nullable
	private String angebot;

	@Nullable
	private String angebotKontaktpersonVorname;

	@Nullable
	private String angebotKontaktpersonNachname;

	@Nullable
	private JaxAdresse angebotAdresse;

	@Nullable
	private BigDecimal anzahlFerienwochenHerbstferien;

	@Nullable
	private BigDecimal anzahlFerienwochenWinterferien;

	@Nullable
	private BigDecimal anzahlFerienwochenFruehlingsferien;

	@Nullable
	private BigDecimal anzahlFerienwochenSommerferien;

	@Nullable
	private BigDecimal anzahlTage;

	@Nullable
	private BigDecimal anzahlStundenProBetreuungstag;

	@Nullable
	private String bemerkungenOeffnungszeiten;

	@Nullable
	private List<JaxGemeinde> finanziellBeteiligteGemeinden;

	@Nullable
	private Boolean gemeindeFuehrtAngebotSelber;

	@Nullable
	private Boolean gemeindeBeauftragtExterneAnbieter;

	@Nullable
	private Boolean angebotVereineUndPrivateIntegriert;

	@Nullable
	private Boolean bemerkungenKooperation;

	@Nullable
	private String leitungDurchPersonMitAusbildung;

	@Nullable
	private BigDecimal aufwandBetreuungspersonal;

	@Nullable
	private BigDecimal zusaetzlicherAufwandLeitungAdmin;

	@Nullable
	private String bemerkungenPersonal;

	@Nullable
	private Boolean fixerTarifKinderDerGemeinde;

	@Nullable
	private Boolean einkommensabhaengigerTarifKinderDerGemeinde;

	@Nullable
	private Boolean tagesschuleTarifGiltFuerFerienbetreuung;

	@Nullable
	private Boolean ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet;

	@Nullable
	private Boolean kinderAusAnderenGemeindenZahlenAnderenTarif;

	@Nullable
	private String bemerkungenTarifsystem;

	@Nullable
	public String getAngebot() {
		return angebot;
	}

	public void setAngebot(@Nullable String angebot) {
		this.angebot = angebot;
	}

	@Nullable
	public String getAngebotKontaktpersonVorname() {
		return angebotKontaktpersonVorname;
	}

	public void setAngebotKontaktpersonVorname(@Nullable String angebotKontaktpersonVorname) {
		this.angebotKontaktpersonVorname = angebotKontaktpersonVorname;
	}

	@Nullable
	public String getAngebotKontaktpersonNachname() {
		return angebotKontaktpersonNachname;
	}

	public void setAngebotKontaktpersonNachname(@Nullable String angebotKontaktpersonNachname) {
		this.angebotKontaktpersonNachname = angebotKontaktpersonNachname;
	}

	@Nullable
	public JaxAdresse getAngebotAdresse() {
		return angebotAdresse;
	}

	public void setAngebotAdresse(@Nullable JaxAdresse angebotAdresse) {
		this.angebotAdresse = angebotAdresse;
	}

	@Nullable
	public BigDecimal getAnzahlFerienwochenHerbstferien() {
		return anzahlFerienwochenHerbstferien;
	}

	public void setAnzahlFerienwochenHerbstferien(@Nullable BigDecimal anzahlFerienwochenHerbstferien) {
		this.anzahlFerienwochenHerbstferien = anzahlFerienwochenHerbstferien;
	}

	@Nullable
	public BigDecimal getAnzahlFerienwochenWinterferien() {
		return anzahlFerienwochenWinterferien;
	}

	public void setAnzahlFerienwochenWinterferien(@Nullable BigDecimal anzahlFerienwochenWinterferien) {
		this.anzahlFerienwochenWinterferien = anzahlFerienwochenWinterferien;
	}

	@Nullable
	public BigDecimal getAnzahlFerienwochenFruehlingsferien() {
		return anzahlFerienwochenFruehlingsferien;
	}

	public void setAnzahlFerienwochenFruehlingsferien(@Nullable BigDecimal anzahlFerienwochenFruehlingsferien) {
		this.anzahlFerienwochenFruehlingsferien = anzahlFerienwochenFruehlingsferien;
	}

	@Nullable
	public BigDecimal getAnzahlFerienwochenSommerferien() {
		return anzahlFerienwochenSommerferien;
	}

	public void setAnzahlFerienwochenSommerferien(@Nullable BigDecimal anzahlFerienwochenSommerferien) {
		this.anzahlFerienwochenSommerferien = anzahlFerienwochenSommerferien;
	}

	@Nullable
	public BigDecimal getAnzahlTage() {
		return anzahlTage;
	}

	public void setAnzahlTage(@Nullable BigDecimal anzahlTage) {
		this.anzahlTage = anzahlTage;
	}

	@Nullable
	public BigDecimal getAnzahlStundenProBetreuungstag() {
		return anzahlStundenProBetreuungstag;
	}

	public void setAnzahlStundenProBetreuungstag(@Nullable BigDecimal anzahlStundenProBetreuungstag) {
		this.anzahlStundenProBetreuungstag = anzahlStundenProBetreuungstag;
	}

	@Nullable
	public String getBemerkungenOeffnungszeiten() {
		return bemerkungenOeffnungszeiten;
	}

	public void setBemerkungenOeffnungszeiten(@Nullable String bemerkungenOeffnungszeiten) {
		this.bemerkungenOeffnungszeiten = bemerkungenOeffnungszeiten;
	}

	@Nullable
	public List<JaxGemeinde> getFinanziellBeteiligteGemeinden() {
		return finanziellBeteiligteGemeinden;
	}

	public void setFinanziellBeteiligteGemeinden(@Nullable List<JaxGemeinde> finanziellBeteiligteGemeinden) {
		this.finanziellBeteiligteGemeinden = finanziellBeteiligteGemeinden;
	}

	@Nullable
	public Boolean getGemeindeFuehrtAngebotSelber() {
		return gemeindeFuehrtAngebotSelber;
	}

	public void setGemeindeFuehrtAngebotSelber(Boolean gemeindeFuehrtAngebotSelber) {
		this.gemeindeFuehrtAngebotSelber = gemeindeFuehrtAngebotSelber;
	}

	@Nullable
	public Boolean getGemeindeBeauftragtExterneAnbieter() {
		return gemeindeBeauftragtExterneAnbieter;
	}

	public void setGemeindeBeauftragtExterneAnbieter(@Nullable Boolean gemeindeBeauftragtExterneAnbieter) {
		this.gemeindeBeauftragtExterneAnbieter = gemeindeBeauftragtExterneAnbieter;
	}

	@Nullable
	public Boolean getAngebotVereineUndPrivateIntegriert() {
		return angebotVereineUndPrivateIntegriert;
	}

	public void setAngebotVereineUndPrivateIntegriert(@Nullable Boolean angebotVereineUndPrivateIntegriert) {
		this.angebotVereineUndPrivateIntegriert = angebotVereineUndPrivateIntegriert;
	}

	@Nullable
	public Boolean getBemerkungenKooperation() {
		return bemerkungenKooperation;
	}

	public void setBemerkungenKooperation(@Nullable Boolean bemerkungenKooperation) {
		this.bemerkungenKooperation = bemerkungenKooperation;
	}

	@Nullable
	public String getLeitungDurchPersonMitAusbildung() {
		return leitungDurchPersonMitAusbildung;
	}

	public void setLeitungDurchPersonMitAusbildung(@Nullable String leitungDurchPersonMitAusbildung) {
		this.leitungDurchPersonMitAusbildung = leitungDurchPersonMitAusbildung;
	}

	@Nullable
	public BigDecimal getAufwandBetreuungspersonal() {
		return aufwandBetreuungspersonal;
	}

	public void setAufwandBetreuungspersonal(@Nullable BigDecimal aufwandBetreuungspersonal) {
		this.aufwandBetreuungspersonal = aufwandBetreuungspersonal;
	}

	@Nullable
	public BigDecimal getZusaetzlicherAufwandLeitungAdmin() {
		return zusaetzlicherAufwandLeitungAdmin;
	}

	public void setZusaetzlicherAufwandLeitungAdmin(@Nullable BigDecimal zusaetzlicherAufwandLeitungAdmin) {
		this.zusaetzlicherAufwandLeitungAdmin = zusaetzlicherAufwandLeitungAdmin;
	}

	@Nullable
	public String getBemerkungenPersonal() {
		return bemerkungenPersonal;
	}

	public void setBemerkungenPersonal(@Nullable String bemerkungenPersonal) {
		this.bemerkungenPersonal = bemerkungenPersonal;
	}

	@Nullable
	public Boolean getFixerTarifKinderDerGemeinde() {
		return fixerTarifKinderDerGemeinde;
	}

	public void setFixerTarifKinderDerGemeinde(@Nullable Boolean fixerTarifKinderDerGemeinde) {
		this.fixerTarifKinderDerGemeinde = fixerTarifKinderDerGemeinde;
	}

	@Nullable
	public Boolean getEinkommensabhaengigerTarifKinderDerGemeinde() {
		return einkommensabhaengigerTarifKinderDerGemeinde;
	}

	public void setEinkommensabhaengigerTarifKinderDerGemeinde(@Nullable Boolean einkommensabhaengigerTarifKinderDerGemeinde) {
		this.einkommensabhaengigerTarifKinderDerGemeinde = einkommensabhaengigerTarifKinderDerGemeinde;
	}

	@Nullable
	public Boolean getTagesschuleTarifGiltFuerFerienbetreuung() {
		return tagesschuleTarifGiltFuerFerienbetreuung;
	}

	public void setTagesschuleTarifGiltFuerFerienbetreuung(@Nullable Boolean tagesschuleTarifGiltFuerFerienbetreuung) {
		this.tagesschuleTarifGiltFuerFerienbetreuung = tagesschuleTarifGiltFuerFerienbetreuung;
	}

	@Nullable
	public Boolean getFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet() {
		return ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet;
	}

	public void setFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet(@Nullable Boolean ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet) {
		this.ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet = ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet;
	}

	@Nullable
	public Boolean getKinderAusAnderenGemeindenZahlenAnderenTarif() {
		return kinderAusAnderenGemeindenZahlenAnderenTarif;
	}

	public void setKinderAusAnderenGemeindenZahlenAnderenTarif(@Nullable Boolean kinderAusAnderenGemeindenZahlenAnderenTarif) {
		this.kinderAusAnderenGemeindenZahlenAnderenTarif = kinderAusAnderenGemeindenZahlenAnderenTarif;
	}

	@Nullable
	public String getBemerkungenTarifsystem() {
		return bemerkungenTarifsystem;
	}

	public void setBemerkungenTarifsystem(@Nullable String bemerkungenTarifsystem) {
		this.bemerkungenTarifsystem = bemerkungenTarifsystem;
	}
}
