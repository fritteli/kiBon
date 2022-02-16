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
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.FinanzDatenDTO;
import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.util.MathUtil;

public class FinanzielleSituationLuzernRechner extends AbstractFinanzielleSituationRechner {

	@Override
	public void calculateFinanzDaten(
		@Nonnull Gesuch gesuch,
		BigDecimal minimumEKV) {
		FinanzDatenDTO finanzDatenDTOAlleine = new FinanzDatenDTO();
		FinanzDatenDTO finanzDatenDTOZuZweit = new FinanzDatenDTO();

		// Finanzielle Situation berechnen
		FinanzielleSituationResultateDTO finanzielleSituationResultateDTOAlleine =
			calculateResultateFinanzielleSituation(gesuch, false);
		FinanzielleSituationResultateDTO finanzielleSituationResultateDTOZuZweit =
			calculateResultateFinanzielleSituation(gesuch, true);

		finanzDatenDTOAlleine.setMassgebendesEinkBjVorAbzFamGr(finanzielleSituationResultateDTOAlleine.getMassgebendesEinkVorAbzFamGr());
		finanzDatenDTOZuZweit.setMassgebendesEinkBjVorAbzFamGr(finanzielleSituationResultateDTOZuZweit.getMassgebendesEinkVorAbzFamGr());

		EinkommensverschlechterungInfo ekvInfo = gesuch.extractEinkommensverschlechterungInfo();
		if (ekvInfo != null && ekvInfo.getEinkommensverschlechterung()) {
			FinanzielleSituationResultateDTO resultateEKV1Alleine =
				calculateResultateEinkommensverschlechterung(gesuch, 1, false);
			FinanzielleSituationResultateDTO resultateEKV1ZuZweit =
				calculateResultateEinkommensverschlechterung(gesuch, 1, true);
			BigDecimal massgebendesEinkommenBasisjahrAlleine =
				finanzielleSituationResultateDTOAlleine.getMassgebendesEinkVorAbzFamGr();
			BigDecimal massgebendesEinkommenBasisjahrZuZweit =
				finanzielleSituationResultateDTOZuZweit.getMassgebendesEinkVorAbzFamGr();

			if (ekvInfo.getEkvFuerBasisJahrPlus1() != null && ekvInfo.getEkvFuerBasisJahrPlus1()) {
				finanzDatenDTOAlleine.setEkv1Erfasst(true);
				finanzDatenDTOZuZweit.setEkv1Erfasst(true);
				if (ekvInfo.getEkvBasisJahrPlus1Annulliert()) {
					finanzDatenDTOAlleine.setEkv1Annulliert(Boolean.TRUE);
					finanzDatenDTOZuZweit.setEkv1Annulliert(Boolean.TRUE);
				}
				// In der EKV 1 vergleichen wir immer mit dem Basisjahr
				handleEKV1(finanzDatenDTOAlleine, resultateEKV1Alleine.getMassgebendesEinkVorAbzFamGr(),
					massgebendesEinkommenBasisjahrAlleine, minimumEKV);
				handleEKV1(finanzDatenDTOZuZweit, resultateEKV1ZuZweit.getMassgebendesEinkVorAbzFamGr(),
					massgebendesEinkommenBasisjahrZuZweit, minimumEKV);
			}

			BigDecimal massgebendesEinkommenVorjahrAlleine;
			if (finanzDatenDTOAlleine.isEkv1AcceptedAndNotAnnuliert()) {
				massgebendesEinkommenVorjahrAlleine = resultateEKV1Alleine.getMassgebendesEinkVorAbzFamGr();
			} else {
				massgebendesEinkommenVorjahrAlleine = massgebendesEinkommenBasisjahrAlleine;
			}
			BigDecimal massgebendesEinkommenVorjahrZuZweit;
			if (finanzDatenDTOZuZweit.isEkv1AcceptedAndNotAnnuliert()) {
				massgebendesEinkommenVorjahrZuZweit = resultateEKV1ZuZweit.getMassgebendesEinkVorAbzFamGr();
			} else {
				massgebendesEinkommenVorjahrZuZweit = massgebendesEinkommenBasisjahrZuZweit;
			}

			if (ekvInfo.getEkvFuerBasisJahrPlus2() != null && ekvInfo.getEkvFuerBasisJahrPlus2()) {
				finanzDatenDTOAlleine.setEkv2Erfasst(true);
				finanzDatenDTOZuZweit.setEkv2Erfasst(true);
				if (ekvInfo.getEkvBasisJahrPlus2Annulliert()) {
					finanzDatenDTOAlleine.setEkv2Annulliert(Boolean.TRUE);
					finanzDatenDTOZuZweit.setEkv2Annulliert(Boolean.TRUE);
				}
				FinanzielleSituationResultateDTO resultateEKV2Alleine =
					calculateResultateEinkommensverschlechterung(gesuch, 2, false);
				FinanzielleSituationResultateDTO resultateEKV2ZuZweit =
					calculateResultateEinkommensverschlechterung(gesuch, 2, true);
				// In der EKV 2 vergleichen wir immer mit dem Basisjahr
				handleEKV2(finanzDatenDTOAlleine,
					resultateEKV2Alleine.getMassgebendesEinkVorAbzFamGr(),
					massgebendesEinkommenBasisjahrAlleine,
					minimumEKV);
				handleEKV2(finanzDatenDTOZuZweit,
					resultateEKV2ZuZweit.getMassgebendesEinkVorAbzFamGr(),
					massgebendesEinkommenBasisjahrZuZweit,
					minimumEKV);
			} else {
				finanzDatenDTOAlleine.setMassgebendesEinkBjP2VorAbzFamGr(massgebendesEinkommenVorjahrAlleine);
				finanzDatenDTOZuZweit.setMassgebendesEinkBjP2VorAbzFamGr(massgebendesEinkommenVorjahrZuZweit);
			}
		}
		gesuch.setFinanzDatenDTO_alleine(finanzDatenDTOAlleine);
		gesuch.setFinanzDatenDTO_zuZweit(finanzDatenDTOZuZweit);
	}

	@Override
	public void setFinanzielleSituationParameters(
		@Nonnull Gesuch gesuch,
		final FinanzielleSituationResultateDTO finSitResultDTO,
		boolean hasSecondGesuchsteller) {
		final FinanzielleSituation finanzielleSituationGS1 = getFinanzielleSituationGS(gesuch.getGesuchsteller1());
		// Die Daten fuer GS 2 werden nur beruecksichtigt, wenn es (aktuell) zwei Gesuchsteller hat
		FinanzielleSituation finanzielleSituationGS2 = null;
		if (hasSecondGesuchsteller && gesuch.getGesuchsteller2() != null) {
			finanzielleSituationGS2 = getFinanzielleSituationGS(gesuch.getGesuchsteller2());
		}
		calculateZusammen(
			finSitResultDTO,
			finanzielleSituationGS1,
			finanzielleSituationGS2);
		finSitResultDTO.setMassgebendesEinkVorAbzFamGrGS1(getMassgegebenesEinkommenAleine(finanzielleSituationGS1));
		finSitResultDTO.setMassgebendesEinkVorAbzFamGrGS2(getMassgegebenesEinkommenAleine(finanzielleSituationGS2));
	}

	@Override
	public void setEinkommensverschlechterungParameters(
		@Nonnull Gesuch gesuch, int basisJahrPlus,
		final FinanzielleSituationResultateDTO einkVerResultDTO, boolean hasSecondGesuchsteller) {
		Einkommensverschlechterung einkommensverschlechterungGS1Bjp1 =
			getEinkommensverschlechterungGS(gesuch.getGesuchsteller1(), 1);
		Einkommensverschlechterung einkommensverschlechterungGS1Bjp2 =
			getEinkommensverschlechterungGS(gesuch.getGesuchsteller1(), 2);

		// Die Daten fuer GS 2 werden nur beruecksichtigt, wenn es (aktuell) zwei Gesuchsteller hat
		Einkommensverschlechterung einkommensverschlechterungGS2Bjp1 = null;
		Einkommensverschlechterung einkommensverschlechterungGS2Bjp2 = null;
		if (hasSecondGesuchsteller) {
			einkommensverschlechterungGS2Bjp1 = getEinkommensverschlechterungGS(gesuch.getGesuchsteller2(), 1);
			einkommensverschlechterungGS2Bjp2 = getEinkommensverschlechterungGS(gesuch.getGesuchsteller2(), 2);
		}

		if (basisJahrPlus == 2) {
			calculateZusammen(
				einkVerResultDTO,
				einkommensverschlechterungGS1Bjp2,
				einkommensverschlechterungGS2Bjp2);
		} else {
			calculateZusammen(
				einkVerResultDTO,
				einkommensverschlechterungGS1Bjp1,
				einkommensverschlechterungGS2Bjp1);
		}
	}

	private BigDecimal getMassgegebenesEinkommenAleine(@Nullable FinanzielleSituation finanzielleSituation) {
		BigDecimal einkommenAleine = calcEinkommen(finanzielleSituation, null);
		BigDecimal nettoVermoegenXProzent = calcVermoegen10Prozent(finanzielleSituation, null);
		BigDecimal abzuegeAleine = calcAbzuege(finanzielleSituation, null);
		BigDecimal anrechenbaresEinkommen = add(einkommenAleine, nettoVermoegenXProzent);
		return MathUtil.positiveNonNullAndRound(
			subtract(anrechenbaresEinkommen, abzuegeAleine));
	}

	private void calculateZusammen(
		@Nonnull final FinanzielleSituationResultateDTO finSitResultDTO,
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS1,
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS2) {

		finSitResultDTO.setEinkommenBeiderGesuchsteller(calcEinkommen(
			finanzielleSituationGS1,
			finanzielleSituationGS2));
		finSitResultDTO.setNettovermoegenXProzent(calcVermoegen10Prozent(
			finanzielleSituationGS1,
			finanzielleSituationGS2));
		finSitResultDTO.setAbzuegeBeiderGesuchsteller(calcAbzuege(finanzielleSituationGS1, finanzielleSituationGS2));

		finSitResultDTO.setAnrechenbaresEinkommen(add(
			finSitResultDTO.getEinkommenBeiderGesuchsteller(), finSitResultDTO.getNettovermoegenXProzent())
		);
		finSitResultDTO.setMassgebendesEinkVorAbzFamGr(
			MathUtil.positiveNonNullAndRound(
				subtract(
					finSitResultDTO.getAnrechenbaresEinkommen(),
					finSitResultDTO.getAbzuegeBeiderGesuchsteller())));
	}

	/**
	 * Als Abzuege habe ich die AbzuegeLiegenschaft und EinkaeufeVorsorge genommen
	 * Die Geschäftverlust habe ich bei der Einkommen genommen
	 */
	@Override
	protected BigDecimal calcAbzuege(
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS1,
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS2
	) {
		BigDecimal totalAbzuege = BigDecimal.ZERO;
		if (finanzielleSituationGS1 != null) {
			BigDecimal abzuegeGS1;
			if (!calculateByVeranlagung(finanzielleSituationGS1)) {
				abzuegeGS1 = finanzielleSituationGS1.getSelbstdeklaration() != null ? finanzielleSituationGS1.getSelbstdeklaration().calculateAbzuege() : BigDecimal.ZERO;
			} else {
				abzuegeGS1 = calcAbzuegeFromVeranlagung(finanzielleSituationGS1);
			}
			totalAbzuege = totalAbzuege.add(abzuegeGS1);
		}
		if (finanzielleSituationGS2 != null) {
			BigDecimal abzuegeGS2;
			if (!calculateByVeranlagung(finanzielleSituationGS2)) {
				abzuegeGS2 = finanzielleSituationGS2.getSelbstdeklaration() != null ? finanzielleSituationGS2.getSelbstdeklaration().calculateAbzuege() : BigDecimal.ZERO;
			} else {
				abzuegeGS2 = calcAbzuegeFromVeranlagung(finanzielleSituationGS2);
			}
			totalAbzuege = totalAbzuege.add(abzuegeGS2);
		}
		return totalAbzuege;
	}

	private BigDecimal calcAbzuegeFromVeranlagung(@Nonnull AbstractFinanzielleSituation finanzielleSituation) {
		BigDecimal total = BigDecimal.ZERO;
		// abzuege liegenschaften should be ignored if negative
		total = add(total, MathUtil.positiveNonNull(finanzielleSituation.getAbzuegeLiegenschaft()));
		total = add(total, finanzielleSituation.getEinkaeufeVorsorge());
		return total;
	}

	private BigDecimal calcVermoegen10Prozent(
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS1,
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS2) {
		BigDecimal gs1SteuerbaresVermoegen = BigDecimal.ZERO;
		if (finanzielleSituationGS1 != null) {
			if (!calculateByVeranlagung(finanzielleSituationGS1)) {
				gs1SteuerbaresVermoegen = finanzielleSituationGS1.getSelbstdeklaration() != null ? finanzielleSituationGS1.getSelbstdeklaration().calculateVermoegen() : BigDecimal.ZERO;
			} else {
				gs1SteuerbaresVermoegen = finanzielleSituationGS1.getSteuerbaresVermoegen();
			}
		}
		BigDecimal gs2SteuerbaresVermoegen = BigDecimal.ZERO;
		if (finanzielleSituationGS2 != null) {
			if (!calculateByVeranlagung(finanzielleSituationGS2)) {
				gs2SteuerbaresVermoegen = finanzielleSituationGS2.getSelbstdeklaration() != null ? finanzielleSituationGS2.getSelbstdeklaration().calculateVermoegen() : BigDecimal.ZERO;
			} else {
				gs2SteuerbaresVermoegen = finanzielleSituationGS2.getSteuerbaresVermoegen();
			}
		}

		final BigDecimal totalBruttovermoegen = add(gs1SteuerbaresVermoegen, gs2SteuerbaresVermoegen);

		BigDecimal total = percent(totalBruttovermoegen, 10);
		return MathUtil.GANZZAHL.from(total);
	}

	@Nonnull
	private BigDecimal calcEinkommen(
		@Nullable AbstractFinanzielleSituation abstractFinanzielleSituation1,
		@Nullable AbstractFinanzielleSituation abstractFinanzielleSituation2
	) {
		BigDecimal total = BigDecimal.ZERO;
		total = calcEinkommenProGS(abstractFinanzielleSituation1, total);
		total = calcEinkommenProGS(abstractFinanzielleSituation2, total);
		return total;
	}

	@Nonnull
	private BigDecimal calcEinkommenProGS(
		@Nullable AbstractFinanzielleSituation abstractFinanzielleSituation,
		@Nonnull BigDecimal total
	) {
		if (abstractFinanzielleSituation != null) {
			if (!calculateByVeranlagung(abstractFinanzielleSituation)) {
				total = total.add(abstractFinanzielleSituation.getSelbstdeklaration() != null ? abstractFinanzielleSituation.getSelbstdeklaration().calculateEinkuenfte() : BigDecimal.ZERO);
			} else {
				total = add(total, abstractFinanzielleSituation.getSteuerbaresEinkommen());
				total = subtract(total, abstractFinanzielleSituation.getGeschaeftsverlust());
			}
		}
		return total;
	}

	boolean calculateByVeranlagung(@Nonnull AbstractFinanzielleSituation abstractFinanzielleSituation) {
		// for Einkommensverschlechterung we always use Selbstdeklaration
		if (!(abstractFinanzielleSituation instanceof FinanzielleSituation)) {
			return false;
		}
		FinanzielleSituation finanzielleSituation = (FinanzielleSituation) abstractFinanzielleSituation;
		if (
			finanzielleSituation.getQuellenbesteuert() == null
			|| finanzielleSituation.getVeranlagt() == null
		) {
			return false;
		}
		return !finanzielleSituation.getQuellenbesteuert()
			&& isSameVeranlagungAsVorjahr(finanzielleSituation)
			&& finanzielleSituation.getVeranlagt();
	}

	@Override
	public boolean acceptEKV(
		BigDecimal massgebendesEinkommenBasisjahr,
		BigDecimal massgebendesEinkommenJahr,
		BigDecimal minimumEKV) {

		boolean result = massgebendesEinkommenBasisjahr.compareTo(BigDecimal.ZERO) > 0;
		if (result) {
			BigDecimal differenzGerundet = getCalculatedProzentualeDifferenzRounded(massgebendesEinkommenBasisjahr, massgebendesEinkommenJahr);
			// wenn es gibt mehr als minimumEKV in einer positive oder negative Richtung ist der EKV akkzeptiert
			return differenzGerundet.compareTo(minimumEKV.negate()) <= 0 || differenzGerundet.compareTo(minimumEKV) >= 0;
		}
		return false;
	}

	// one of gemeinsameStekVorjahr or alleinigeStekVorjahr could be null
	private boolean isSameVeranlagungAsVorjahr(@Nonnull FinanzielleSituation finanzielleSituation) {
		boolean same = false;
		if (finanzielleSituation.getGemeinsameStekVorjahr() != null) {
			same = finanzielleSituation.getGemeinsameStekVorjahr();
		}
		if (finanzielleSituation.getAlleinigeStekVorjahr() != null) {
			same |= finanzielleSituation.getAlleinigeStekVorjahr();
		}
		return same;
	}
}
