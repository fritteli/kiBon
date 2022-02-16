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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.finanzielleSituationRechner;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituationSelbstdeklaration;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.wildfly.common.Assert;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FinanzielleSituationLuzernRechnerTest {

	private FinanzielleSituationLuzernRechner finSitRechner = new FinanzielleSituationLuzernRechner();

	/**
	 * Veranlagte Berechnung, entweder alleine oder zur zweit
	 *
	 * Steuerbares Einkommen
	 * Steuerbares Vermögen	10% von diesem addieren
	 * Abzüge für den effektiven Liegenschaftsunterhalt...	Subtrahieren
	 * Verrechenbare Geschäftsverluste aus den Vorjahren..	Subtrahieren
	 * Einkäufe in die berufliche Vorsorge Subtrahieren
	 *
	 * Einkommensverschlechterung sind noch nicht implementiert
	 */
	@Nested
	class VeranlagtTest {

		/**
		 * Steuerbares Einkommen								60'000
		 * Steuerbares Vermögen	10'000, 10% =			       + 1'000
		 * Abzüge für den effektiven Liegenschaftsunterhalt... - 1'000
		 * Verrechenbare Geschäftsverluste aus den Vorjahren.. - 1'000
		 * Einkäufe in die berufliche Vorsorge Subtrahieren    - 1'000
		 *                                                     -------
		 *                                                      58'000
		 *                                                     */
		@Test
		public void testAlleWertVorhanden() {
			Gesuch gesuch = prepareGesuch(false);
			finSitRechner.calculateFinanzDaten(gesuch, null);
			assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(58000)));

			//zwei Antragstellende, beides ueberpruefen
			gesuch = prepareGesuch(true);
			finSitRechner.calculateFinanzDaten(gesuch, null);
			assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(58000)));
			assertThat(gesuch.getFinanzDatenDTO_zuZweit().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(116000)));
		}

		/**
		 * In case der Rechner ist angerufen bevor der FinSit bekannt ist sollte keinen Fehler verursachen
		 */
		@Test
		public void testNullableWertVorhanden() {
			Gesuch gesuch = prepareGesuch(false);
			assert gesuch.getGesuchsteller1() != null;
			assert gesuch.getGesuchsteller1().getFinanzielleSituationContainer() != null;
			FinanzielleSituation emptyFinanzielleSituationForTest = new FinanzielleSituation();
			setPropertiesToCalculateByVeranlagung(emptyFinanzielleSituationForTest);
			gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(emptyFinanzielleSituationForTest);
			finSitRechner.calculateFinanzDaten(gesuch, null);
			assertThat(gesuch.getFinanzDatenDTO_alleine().getMassgebendesEinkBjVorAbzFamGr(), is(BigDecimal.valueOf(0)));
		}

		@Test
		public void testBerechnungAufgrundSelbstdeklarationOrVeranlagt() {
			FinanzielleSituation finSit = new FinanzielleSituation();
			finSit.setQuellenbesteuert(false);
			finSit.setGemeinsameStekVorjahr(true);
			finSit.setVeranlagt(true);
			Assert.assertTrue(finSitRechner.calculateByVeranlagung(finSit));

			FinanzielleSituation finSit2 = new FinanzielleSituation();
			finSit2.setQuellenbesteuert(false);
			finSit2.setAlleinigeStekVorjahr(true);
			finSit2.setVeranlagt(true);
			Assert.assertTrue(finSitRechner.calculateByVeranlagung(finSit2));

			FinanzielleSituation finSit3 = new FinanzielleSituation();
			finSit2.setQuellenbesteuert(true);
			Assert.assertFalse(finSitRechner.calculateByVeranlagung(finSit3));

			FinanzielleSituation finSit4 = new FinanzielleSituation();
			finSit4.setQuellenbesteuert(false);
			finSit4.setGemeinsameStekVorjahr(false);
			Assert.assertFalse(finSitRechner.calculateByVeranlagung(finSit4));

			FinanzielleSituation finSit5 = new FinanzielleSituation();
			finSit5.setQuellenbesteuert(false);
			finSit5.setAlleinigeStekVorjahr(false);
			Assert.assertFalse(finSitRechner.calculateByVeranlagung(finSit5));

			FinanzielleSituation finSit6 = new FinanzielleSituation();
			finSit6.setQuellenbesteuert(true);
			finSit6.setGemeinsameStekVorjahr(true);
			finSit6.setVeranlagt(false);
			Assert.assertFalse(finSitRechner.calculateByVeranlagung(finSit6));
		}

		@Test
		public void testCalculateSelbstdeklaration() {
			Gesuch gesuch = prepareGesuch(false);
			assert gesuch.getGesuchsteller1() != null;
			assert gesuch.getGesuchsteller1().getFinanzielleSituationContainer() != null;
			FinanzielleSituation finSit = new FinanzielleSituation();
			finSit.setSelbstdeklaration(createSelbstdeklaration());
			gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(finSit);
			var resultDTO = new FinanzielleSituationResultateDTO();

			finSitRechner.setFinanzielleSituationParameters(gesuch, resultDTO, false);
			assertThat(resultDTO.getEinkommenGS1(), is(BigDecimal.valueOf(241511)));
			assertThat(resultDTO.getAbzuegeGS1(), is(BigDecimal.valueOf(40195)));
			assertThat(resultDTO.getVermoegenXPercentAnrechenbarGS1(), is(BigDecimal.valueOf(9257)));
			assertThat(resultDTO.getMassgebendesEinkVorAbzFamGrGS1(), is(BigDecimal.valueOf(210573)));
		}
	}

	private void setPropertiesToCalculateByVeranlagung(@Nonnull FinanzielleSituation finanzielleSituation) {
		finanzielleSituation.setQuellenbesteuert(false);
		finanzielleSituation.setAlleinigeStekVorjahr(true);
		finanzielleSituation.setGemeinsameStekVorjahr(null);
		finanzielleSituation.setVeranlagt(true);
	}


	private Gesuch prepareGesuch(boolean secondGesuchsteller) {
		Gesuch gesuch = new Gesuch();
		gesuch.setGesuchsteller1(createGesuchstellerMitFinSit());
		if(secondGesuchsteller) {
			gesuch.setGesuchsteller2(createGesuchstellerMitFinSit());
		}
		return gesuch;
	}

	private GesuchstellerContainer createGesuchstellerMitFinSit() {
		GesuchstellerContainer gesuchstellerContainer = new GesuchstellerContainer();
		FinanzielleSituationContainer finanzielleSituationContainer = new FinanzielleSituationContainer();
		FinanzielleSituation finanzielleSituationForTest = new FinanzielleSituation();
		finanzielleSituationForTest.setSteuerbaresEinkommen(BigDecimal.valueOf(60000));
		finanzielleSituationForTest.setSteuerbaresVermoegen(BigDecimal.valueOf(10000));
		finanzielleSituationForTest.setGeschaeftsverlust(BigDecimal.valueOf(1000));
		finanzielleSituationForTest.setAbzuegeLiegenschaft(BigDecimal.valueOf(1000));
		finanzielleSituationForTest.setEinkaeufeVorsorge(BigDecimal.valueOf(1000));
		setPropertiesToCalculateByVeranlagung(finanzielleSituationForTest);
		finanzielleSituationContainer.setFinanzielleSituationJA(finanzielleSituationForTest);
		gesuchstellerContainer.setFinanzielleSituationContainer(finanzielleSituationContainer);
		return gesuchstellerContainer;
	}

	private FinanzielleSituationSelbstdeklaration createSelbstdeklaration() {
		var deklaration = new FinanzielleSituationSelbstdeklaration();
		deklaration.setEinkunftErwerb(new BigDecimal("35678.00"));
		deklaration.setEinkunftVersicherung(new BigDecimal("31319.00"));
		deklaration.setEinkunftAusgleichskassen(new BigDecimal("42249.00"));
		deklaration.setEinkunftWertschriften(new BigDecimal("13668.00"));
		deklaration.setEinkunftUnterhaltsbeitragSteuerpflichtige(new BigDecimal("37851.00"));
		deklaration.setEinkunftUnterhaltsbeitragKinder(new BigDecimal("40936.00"));
		deklaration.setEinkunftUeberige(new BigDecimal("16005.00"));
		deklaration.setEinkunftLiegenschaften(new BigDecimal("23805.00"));
		deklaration.setAbzugBerufsauslagen(new BigDecimal("3940.00"));
		deklaration.setAbzugUnterhaltsbeitragEhepartner(new BigDecimal("2183.00"));
		deklaration.setAbzugUnterhaltsbeitragKinder(new BigDecimal("4279.00"));
		deklaration.setAbzugRentenleistungen(new BigDecimal("3346.00"));
		deklaration.setAbzugSaeule3A(new BigDecimal("2358.00"));
		deklaration.setAbzugVersicherungspraemien(new BigDecimal("4521.00"));
		deklaration.setAbzugKrankheitsUnfallKosten(new BigDecimal("1061.00"));
		deklaration.setAbzugFreiweiligeZuwendungPartien(new BigDecimal("1929.00"));
		deklaration.setAbzugKinderVorschule(new BigDecimal("2684.00"));
		deklaration.setAbzugKinderSchule(new BigDecimal("1381.00"));
		deklaration.setAbzugKinderAuswaertigerAufenthalt(new BigDecimal("3041.00"));
		deklaration.setAbzugEigenbetreuung(new BigDecimal("2370.00"));
		deklaration.setAbzugFremdbetreuung(new BigDecimal("4211.00"));
		deklaration.setAbzugErwerbsunfaehigePersonen(new BigDecimal("2891.00"));
		deklaration.setVermoegen(new BigDecimal("100000.00"));
		deklaration.setAbzugSteuerfreierBetragErwachsene(new BigDecimal("2652.00"));
		deklaration.setAbzugSteuerfreierBetragKinder(new BigDecimal("4783.00"));
		return deklaration;
	}
}
