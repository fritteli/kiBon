/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.pdfgenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.lib.invoicegenerator.dto.Alignment;
import ch.dvbern.lib.invoicegenerator.dto.OnPage;
import ch.dvbern.lib.invoicegenerator.dto.PageConfiguration;
import ch.dvbern.lib.invoicegenerator.dto.component.PhraseRenderer;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.FULL_WIDTH;

public class VerfuegungPdfGenerator extends DokumentAnFamilieGenerator {

	private static final String VERFUEGUNG_TITLE = "PdfGeneration_Verfuegung_Title";
	private static final String ANGEBOT = "PdfGeneration_Betreuungsangebot";
	private static final String VERFUEGUNG_CONTENT = "PdfGeneration_Verfuegung_Content";
	private static final String KEIN_ANSPRUCH_CONTENT = "PdfGeneration_KeinAnspruch_Content";
	private static final String NICHT_EINTRETEN_CONTENT_1 = "PdfGeneration_NichtEintreten_Content_1";
	private static final String NICHT_EINTRETEN_CONTENT_2 = "PdfGeneration_NichtEintreten_Content_2";
	private static final String NICHT_EINTRETEN_CONTENT_3 = "PdfGeneration_NichtEintreten_Content_3";
	private static final String NICHT_EINTRETEN_CONTENT_4 = "PdfGeneration_NichtEintreten_Content_4";
	private static final String NICHT_EINTRETEN_CONTENT_5 = "PdfGeneration_NichtEintreten_Content_5";
	private static final String NICHT_EINTRETEN_CONTENT_6 = "PdfGeneration_NichtEintreten_Content_6";
	private static final String NICHT_EINTRETEN_CONTENT_7 = "PdfGeneration_NichtEintreten_Content_7";
	private static final String NICHT_EINTRETEN_CONTENT_8 = "PdfGeneration_NichtEintreten_Content_8";
	private static final String BEMERKUNGEN = "PdfGeneration_Verfuegung_Bemerkungen";
	private static final String RECHTSMITTELBELEHRUNG_TITLE = "PdfGeneration_Rechtsmittelbelehrung_Title";
	private static final String RECHTSMITTELBELEHRUNG_CONTENT = "PdfGeneration_Rechtsmittelbelehrung_Content";
	private static final String FUSSZEILE_1 = "PdfGeneration_Verfuegung_Fusszeile1";
	private static final String FUSSZEILE_2 = "PdfGeneration_Verfuegung_Fusszeile2";

	public enum Art {
		NORMAL,
		KEIN_ANSPRUCH,
		NICHT_EINTRETTEN
	}

	private Betreuung betreuung;

	@Nonnull
	private final Art art;

	@Nonnull
	private final PhraseRenderer footer;



	public VerfuegungPdfGenerator(
		@Nonnull Betreuung betreuung,
		@Nonnull GemeindeStammdaten stammdaten,
		final boolean draft,
		@Nonnull Art art) {
		super(betreuung.extractGesuch(), stammdaten, draft);

		this.betreuung = betreuung;
		this.art = art;
		footer = new PhraseRenderer(getFooterLines(), PageConfiguration.LEFT_PAGE_DEFAULT_MARGIN_MM, 280,
						165, 20, OnPage.FIRST, 8, Alignment.LEFT, 1.2F);
	}

	@Nonnull
	@Override
	protected String getDocumentTitle() {
		return ServerMessageUtil.getMessage(VERFUEGUNG_TITLE);
	}

	@Nonnull
	@Override
	protected CustomGenerator getCustomGenerator() {
		return (generator, ctx) -> {
			Document document = generator.getDocument();
			document.add(createIntro());
			document.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(ANREDE_FAMILIE)));
			createContent(document);
		};
	}

	public void createContent(@Nonnull final Document document) throws DocumentException {
		List<Element> bemerkungenElements = Lists.newArrayList();
		List<Element> gruesseElements = Lists.newArrayList();
		Kind kind = betreuung.getKind().getKindJA();
		DateRange gp = gesuch.getGesuchsperiode().getGueltigkeit();
		switch (art) {
			case NORMAL:
				footer.setPayload(Collections.emptyList());
				document.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(VERFUEGUNG_CONTENT,
					kind.getFullName(),
					Constants.DATE_FORMATTER.format(kind.getGeburtsdatum())), 2));
				document.add(createVerfuegungTable());
				bemerkungenElements.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(BEMERKUNGEN)));
				bemerkungenElements.add(PdfUtil.createList(getBemerkungen()));
				document.add(PdfUtil.createKeepTogetherTable(bemerkungenElements, 0, 2));
				break;
			case KEIN_ANSPRUCH:
				footer.setPayload(Collections.emptyList());

				document.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(KEIN_ANSPRUCH_CONTENT,
					kind.getFullName(),
					Constants.DATE_FORMATTER.format(kind.getGeburtsdatum()),
					Constants.DATE_FORMATTER.format(gp.getGueltigAb()),
					Constants.DATE_FORMATTER.format(gp.getGueltigBis())), 2));
				bemerkungenElements.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(BEMERKUNGEN)));
				bemerkungenElements.add(PdfUtil.createList(getBemerkungen()));
				document.add(PdfUtil.createKeepTogetherTable(bemerkungenElements, 0, 2));
				break;
			case NICHT_EINTRETTEN:
				footer.setPayload(getFooterLines());
				LocalDate eingangsdatum = gesuch.getEingangsdatum() != null ? gesuch.getEingangsdatum() : LocalDate.now();
				document.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(NICHT_EINTRETEN_CONTENT_1,
					Constants.DATE_FORMATTER.format(gp.getGueltigAb()),
					Constants.DATE_FORMATTER.format(gp.getGueltigBis()),
					kind.getFullName(),
					betreuung.getInstitutionStammdaten().getInstitution().getName(),
					betreuung.getBGNummer())));
				document.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(NICHT_EINTRETEN_CONTENT_2,
					Constants.DATE_FORMATTER.format(eingangsdatum))));
				document.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(NICHT_EINTRETEN_CONTENT_3)));
				document.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(NICHT_EINTRETEN_CONTENT_4)));
				document.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(NICHT_EINTRETEN_CONTENT_5)));
				document.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(NICHT_EINTRETEN_CONTENT_6), 2));
				document.newPage();
				document.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(NICHT_EINTRETEN_CONTENT_7)));
				document.add(PdfUtil.createBoldParagraph(ServerMessageUtil.getMessage(NICHT_EINTRETEN_CONTENT_8,
					Constants.DATE_FORMATTER.format(eingangsdatum)), 2));
				break;
		}
		gruesseElements.add(createParagraphGruss());
		gruesseElements.add(createParagraphSignatur());
		document.add(PdfUtil.createKeepTogetherTable(gruesseElements, 2, 0));
		document.add(createRechtsmittelBelehrung());
	}

	@Nonnull
	private PdfPTable createIntro() {
		List<LabelValuePair> introBasisjahr = new ArrayList<>();
		introBasisjahr.add(new LabelValuePair(REFERENZNUMMER, betreuung.getBGNummer()));
		introBasisjahr.add(new LabelValuePair(NAME, betreuung.getKind().getKindJA().getFullName()));
		introBasisjahr.add(new LabelValuePair(ANGEBOT, ServerMessageUtil.translateEnumValue(betreuung.getBetreuungsangebotTyp())));
		introBasisjahr.add(new LabelValuePair(BETREUUNG_INSTITUTION, betreuung.getInstitutionStammdaten().getInstitution().getName()));
		return PdfUtil.creatreIntroTable(introBasisjahr);
	}

	@Nonnull
	private PdfPTable createVerfuegungTable() {
		//TODO (hefr) Die richtige Verfuegungstabelle erstellen!
		final String[][] daten = {
			{"von", "bis", "effektive Betreuung", "Anspruch", "vergünstigt", "Vollkosten in CHF", "Vergünstigung in CHF", "Elternbeitrag in CHF"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"}
		};
		float[] columnWidths = {10, 10, 10, 10, 10, 10, 12, 12};
		int[] alignement = {Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT };
		return PdfUtil.createTable(daten, columnWidths, alignement, 2);
	}

	@Nonnull
	private List<String> getBemerkungen() {
		// Wenn die Betreuung VERFUEGT ist -> manuelle Bemerkungen Wenn die Betreuung noch nicht VERFUEGT ist ->
		// generated Bemerkungen
		Verfuegung verfuegung = betreuung.getVerfuegung();
		if (verfuegung != null) {
			String bemerkungenAsString = gesuch.getStatus().isAnyStatusOfVerfuegt() ? verfuegung.getManuelleBemerkungen()
				: verfuegung.getGeneratedBemerkungen();
			if (bemerkungenAsString != null) {
				return splitBemerkungen(bemerkungenAsString);
			}
		}
		return Collections.emptyList();
	}

	@Nonnull
	private List<String> splitBemerkungen(@Nonnull String bemerkungen) {
		String[] splitBemerkungenNewLine = bemerkungen.split('[' + System.getProperty("line.separator") + "]+");
		return new ArrayList<>(Arrays.asList(splitBemerkungenNewLine));
	}

	@Nonnull
	public PdfPTable createRechtsmittelBelehrung() {
		PdfPTable table = new PdfPTable(1);
		table.getDefaultCell().setLeading(0,PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		table.setWidthPercentage(FULL_WIDTH);
		PdfPTable innerTable = new PdfPTable(1);
		innerTable.setWidthPercentage(FULL_WIDTH);
		innerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		innerTable.getDefaultCell().setLeading(0,PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		innerTable.addCell(PdfUtil.createBoldParagraph(ServerMessageUtil.getMessage(RECHTSMITTELBELEHRUNG_TITLE), 0));
		innerTable.addCell(PdfUtil.createParagraph(ServerMessageUtil.getMessage(RECHTSMITTELBELEHRUNG_CONTENT)));
		table.addCell(innerTable);
		return table;
	}

	@Nonnull
	private List<String> getFooterLines() {
		return Arrays.asList(
			ServerMessageUtil.getMessage(FUSSZEILE_1),
			ServerMessageUtil.getMessage(FUSSZEILE_2));
	}
}
