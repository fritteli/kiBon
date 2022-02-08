/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.pdfgenerator.finanzielleSituation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.finanzielleSituationRechner.AbstractFinanzielleSituationRechner;
import ch.dvbern.ebegu.pdfgenerator.DokumentAnFamilieGenerator;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.pdfgenerator.PdfUtil;
import ch.dvbern.ebegu.pdfgenerator.TableRowLabelValue;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfPTable;

public abstract class FinanzielleSituationPdfGenerator extends DokumentAnFamilieGenerator {

	protected static final String TITLE = "PdfGeneration_FinSit_Title";
	private static final String NAME = "PdfGeneration_FinSit_Name";
	private static final String BASISJAHR = "PdfGeneration_FinSit_BasisJahr";

	protected final Verfuegung verfuegungFuerMassgEinkommen;
	protected final LocalDate erstesEinreichungsdatum;
	protected final boolean hasSecondGesuchsteller;
	protected final Mandant mandant;
	@Nonnull
	protected AbstractFinanzielleSituationRechner finanzielleSituationRechner;

	protected FinanzielleSituationPdfGenerator(
		@Nonnull Gesuch gesuch,
		@Nonnull Verfuegung verfuegungFuerMassgEinkommen,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull LocalDate erstesEinreichungsdatum,
		@Nonnull AbstractFinanzielleSituationRechner finanzielleSituationRechner
	) {
		super(gesuch, stammdaten);
		this.verfuegungFuerMassgEinkommen = verfuegungFuerMassgEinkommen;
		this.erstesEinreichungsdatum = erstesEinreichungsdatum;

		// Der zweite GS wird gedruckt, wenn er Ende Gesuchsperiode zwingend war ODER es sich um eine Mutation handelt
		// und
		// der zweite GS bereits existiert.
		boolean hasSecondGsEndeGP = hasSecondGesuchsteller();
		boolean isMutationWithSecondGs = gesuch.isMutation() && gesuch.getGesuchsteller2() != null;
		this.hasSecondGesuchsteller = hasSecondGsEndeGP || isMutationWithSecondGs;
		this.finanzielleSituationRechner = finanzielleSituationRechner;
		this.mandant = gesuch.getFall().getMandant();
	}

	@Nonnull
	@Override
	protected final CustomGenerator getCustomGenerator() {
		return (generator, ctx) -> {
			Document document = generator.getDocument();

			initializeValues();

			// Basisjahr
			createPageBasisJahr(generator, document);
			// Eventuelle Einkommenverschlechterung
			EinkommensverschlechterungInfo ekvInfo = gesuch.extractEinkommensverschlechterungInfo();
			if (ekvInfo != null) {
				if (ekvInfo.getEkvFuerBasisJahrPlus1()) {
					createPageEkv1(generator, document);
				}
				if (ekvInfo.getEkvFuerBasisJahrPlus2()) {
					createPageEkv2(generator, document);
				}
			}
			// Massgebendes Einkommen
			createPageMassgebendesEinkommen(document);
		};
	}

	protected abstract void initializeValues();
	protected abstract void createPageBasisJahr(@Nonnull PdfGenerator generator, @Nonnull Document document);
	protected abstract void createPageEkv1(@Nonnull PdfGenerator generator, @Nonnull Document document);
	protected abstract void createPageEkv2(@Nonnull PdfGenerator generator, @Nonnull Document document);
	protected abstract void createPageMassgebendesEinkommen(@Nonnull Document document);

	@Nonnull
	@Override
	protected final String getDocumentTitle() {
		return translate(TITLE);
	}

	@Nonnull
	protected final PdfPTable createIntroMassgebendesEinkommen() {
		List<TableRowLabelValue> introMassgEinkommen = new ArrayList<>();
		introMassgEinkommen.add(new TableRowLabelValue(REFERENZNUMMER, gesuch.getJahrFallAndGemeindenummer()));
		introMassgEinkommen.add(new TableRowLabelValue(NAME, String.valueOf(gesuch.extractFullnamesString())));
		return PdfUtil.createIntroTable(introMassgEinkommen, sprache, mandant);
	}

	@Nonnull
	protected final PdfPTable createIntroBasisjahr() {
		List<TableRowLabelValue> introBasisjahr = new ArrayList<>();
		introBasisjahr.add(new TableRowLabelValue(REFERENZNUMMER, gesuch.getJahrFallAndGemeindenummer()));
		introBasisjahr.add(new TableRowLabelValue(
			BASISJAHR,
			String.valueOf(gesuch.getGesuchsperiode().getBasisJahr())));
		return PdfUtil.createIntroTable(introBasisjahr, sprache, mandant);
	}

	protected final FinanzielleSituationRow createRow(
		String message,
		Function<AbstractFinanzielleSituation, BigDecimal> getter,
		@Nullable AbstractFinanzielleSituation gs1,
		@Nullable AbstractFinanzielleSituation gs2,
		@Nullable AbstractFinanzielleSituation gs1Urspruenglich,
		@Nullable AbstractFinanzielleSituation gs2Urspruenglich
	) {
		BigDecimal gs1BigDecimal = gs1 == null ? null : getter.apply(gs1);
		BigDecimal gs2BigDecimal = gs2 == null ? null : getter.apply(gs2);
		BigDecimal gs1UrspruenglichBigDecimal = gs1Urspruenglich == null ? null : getter.apply(gs1Urspruenglich);
		BigDecimal gs2UrspruenglichBigDecimal = gs2Urspruenglich == null ? null : getter.apply(gs2Urspruenglich);
		FinanzielleSituationRow row = new FinanzielleSituationRow(message, gs1BigDecimal);
		row.setGs2(gs2BigDecimal);
		if (!MathUtil.isSameWithNullAsZero(gs1BigDecimal, gs1UrspruenglichBigDecimal)) {
			row.setGs1Urspruenglich(gs1UrspruenglichBigDecimal, sprache, mandant);
		}
		if (!MathUtil.isSameWithNullAsZero(gs2BigDecimal, gs2UrspruenglichBigDecimal)) {
			row.setGs2Urspruenglich(gs2UrspruenglichBigDecimal, sprache, mandant);
		}
		return row;
	}

	protected final boolean isAbschnittZuSpaetEingereicht(VerfuegungZeitabschnitt abschnitt) {
		return !abschnitt.getGueltigkeit().getGueltigAb().isAfter(erstesEinreichungsdatum);
	}
}
