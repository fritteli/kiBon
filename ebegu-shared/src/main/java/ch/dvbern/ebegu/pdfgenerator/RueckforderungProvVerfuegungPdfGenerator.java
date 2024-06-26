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

package ch.dvbern.ebegu.pdfgenerator;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.util.Constants;
import com.google.common.collect.Lists;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Utilities;
import com.lowagie.text.pdf.PdfContentByte;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RueckforderungProvVerfuegungPdfGenerator extends MandantPdfGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(RueckforderungProvVerfuegungPdfGenerator.class);

	private static final String PROVISORISCHE_VERFUEGUNG_TITLE =
		"PdfGeneration_ProvisorischeVerfuegung_Title";
	private static final String VERFUEGUNG_INTRO =
		"PdfGeneration_ProvisorischeVerfuegung_Intro";
	private static final String BEGRUESSUNG =
		"PdfGeneration_ProvisorischeVerfuegung_Begruessung";
	private static final String INHALT_1A =
		"PdfGeneration_ProvisorischeVerfuegung_Inhalt_1A";
	private static final String INHALT_1B =
		"PdfGeneration_ProvisorischeVerfuegung_Inhalt_1B";
	private static final String INHALT_1C =
		"PdfGeneration_ProvisorischeVerfuegung_Inhalt_1C";
	private static final String INHALT_1D =
		"PdfGeneration_ProvisorischeVerfuegung_Inhalt_1D";
	private static final String INHALT_1E =
		"PdfGeneration_ProvisorischeVerfuegung_Inhalt_1E";
	private static final String INHALT_1F =
		"PdfGeneration_ProvisorischeVerfuegung_Inhalt_1F";
	private static final String INHALT_2 =
		"PdfGeneration_ProvisorischeVerfuegung_Inhalt_2";
	private static final String INHALT_3 =
		"PdfGeneration_ProvisorischeVerfuegung_Inhalt_3";
	private static final String FUSSZEILE_1 =
		"PdfGeneration_ProvisorischeVerfuegung_Fusszeile_1";
	private static final String FUSSZEILE_2 =
		"PdfGeneration_ProvisorischeVerfuegung_Fusszeile_2";
	private static final String FUSSZEILE_3 =
		"PdfGeneration_ProvisorischeVerfuegung_Fusszeile_3";
	private static final String FUSSZEILE_4 =
		"PdfGeneration_ProvisorischeVerfuegung_Fusszeile_4";
	private static final String FUSSZEILE_5 =
		"PdfGeneration_ProvisorischeVerfuegung_Fusszeile_5";
	private static final String GRUNDEN =
		"PdfGeneration_ProvisorischeVerfuegung_Grunden";
	private static final String VERFUEGT =
		"PdfGeneration_ProvisorischeVerfuegung_Verfuegt";
	private static final String GRUND_1 =
		"PdfGeneration_ProvisorischeVerfuegung_Grund_1";
	private static final String GRUND_2 =
		"PdfGeneration_ProvisorischeVerfuegung_Grund_2";
	private static final String GRUND_3 =
		"PdfGeneration_ProvisorischeVerfuegung_Grund_3";
	private static final String BEGRUESSUNG_ENDE =
		"PdfGeneration_ProvisorischeVerfuegung_Begruessung_End";
	private static final String BEGRUESSUNG_AMT =
		"PdfGeneration_ProvisorischeVerfuegung_Begruessung_Amt";
	private static final String VORSTEHERIN =
		"PdfGeneration_ProvisorischeVerfuegung_Vorsteherin";
	private static final String VERSAND_PER_EMAIL_AN = "PdfGeneration_Versand_Per_Email_An";

	private final RueckforderungFormular rueckforderungFormular;
	private final InstitutionStammdaten institutionStammdaten;
	private static final int SUPER_TEXT_SIZE = 6;
	private static final int SUPER_TEXT_RISE = 4;
	private final String nameVerantwortlichePerson;
	private final String pathToUnterschrift;

	public RueckforderungProvVerfuegungPdfGenerator(
		@Nonnull RueckforderungFormular rueckforderungFormular,
		@Nonnull String nameVerantwortlichePerson,
		@Nonnull String pathToUnterschrift
	) {
		super(rueckforderungFormular.getKorrespondenzSprache(),
				Objects.requireNonNull(rueckforderungFormular.getInstitutionStammdaten().getInstitution()
						.getMandant()));
		this.institutionStammdaten = rueckforderungFormular.getInstitutionStammdaten();
		this.rueckforderungFormular = rueckforderungFormular;
		this.nameVerantwortlichePerson = nameVerantwortlichePerson;
		this.pathToUnterschrift = pathToUnterschrift;
	}

	@Nonnull
	@Override
	protected String getDocumentTitle() {
		return "";
	}

	@Nonnull
	@Override
	protected List<String> getEmpfaengerAdresse() {
		final List<String> empfaengerAdresse = new ArrayList<>();
		//Es muss bei der Kanton Adresse anfangen: 4 leere Zeilen
		empfaengerAdresse.add("");
		empfaengerAdresse.add("");
		empfaengerAdresse.add("");
		empfaengerAdresse.add("");
		empfaengerAdresse.add("");
		Adresse adresse = institutionStammdaten.getAdresse();
		empfaengerAdresse.add(adresse.getAddressAsString());
		empfaengerAdresse.add("");
		empfaengerAdresse.add(translate(VERSAND_PER_EMAIL_AN, mandant));
		empfaengerAdresse.add(this.institutionStammdaten.getMail()); //auf zwei Zeilen wegen der Abstand
		empfaengerAdresse.add("");
		empfaengerAdresse.add("");
		empfaengerAdresse.add("");
		empfaengerAdresse.add("");
		empfaengerAdresse.add("");
		empfaengerAdresse.add(Constants.DATE_FORMATTER.format(LocalDate.now()));
		return empfaengerAdresse;
	}

	@Nonnull
	@Override
	protected CustomGenerator getCustomGenerator() {
		return (generator, ctx) -> {
			Document document = generator.getDocument();
			createContent(document, generator);
		};
	}

	public void createContent(
		@Nonnull final Document document,
		@Nonnull ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator generator) throws DocumentException {

		createErsteSeite(document);
		createFusszeileNichtEintreten(generator.getDirectContent());

		document.newPage();

		createHeaderSecondPage(generator.getDirectContent());
		createZweiteSeite(document);
		createEndBegruessung(document, generator.getDirectContent());
	}

	private void createEndBegruessung(Document document, PdfContentByte directContent) {
		final Font defaultFont = getPageConfiguration().getFonts().getFont();
		if (locale.equals(Locale.GERMAN)) {
			createContentWhereIWant(directContent, translate(BEGRUESSUNG_ENDE, mandant), 520, 122,
				defaultFont,10f);
		} else {
			document.add(PdfUtil.createParagraph(translate(BEGRUESSUNG_ENDE, mandant)));
		}

		createContentWhereIWant(directContent, translate(BEGRUESSUNG_AMT, mandant), 495, 122,
			defaultFont, 10f);
		try {
			byte[] signature = IOUtils.toByteArray(new FileInputStream(this.pathToUnterschrift));
			Image image = Image.getInstance(signature);
			float percent = 100.0F * Utilities.millimetersToPoints(50) / image.getWidth();
			image.scalePercent(percent);
			image.setAbsolutePosition(350, 430);
			document.add(image);
		} catch (IOException e) {
			LOG.error("{} konnte nicht geladen werden: {}", this.pathToUnterschrift, e.getMessage());
		}
		createContentWhereIWant(directContent, nameVerantwortlichePerson, 375, 122,
			defaultFont, 10f);
		createContentWhereIWant(directContent, translate(VORSTEHERIN, mandant), 360, 122,
			defaultFont, 10f);
	}

	private void createHeaderSecondPage(PdfContentByte directContent) {
		createContentWhereIWant(directContent, "Kanton Bern", 775, 20,
			getPageConfiguration().getFonts().getFontBold()
			, 10);
		createContentWhereIWant(directContent, "Canton de Berne", 765, 20,
			getPageConfiguration().getFonts().getFontBold()
			, 10);
		createContentWhereIWant(directContent, translate(PROVISORISCHE_VERFUEGUNG_TITLE, mandant), 775, 122,
			getPageConfiguration().getFonts().getFont(),
			6.5f);
		createContentWhereIWant(directContent, translate(VERFUEGUNG_INTRO, mandant), 760, 122,
			getPageConfiguration().getFonts().getFont(), 6.5f);
	}

	private void createErsteSeite(Document document) {
		Paragraph title = new Paragraph(translate(PROVISORISCHE_VERFUEGUNG_TITLE, mandant), PdfUtil.FONT_H2);
		title.setSpacingAfter(15);
		Paragraph intro = new Paragraph(translate(VERFUEGUNG_INTRO, mandant), PdfUtil.FONT_H2);
		intro.setSpacingAfter(30);
		document.add(title);
		document.add(intro);
		document.add(PdfUtil.createParagraph(translate(BEGRUESSUNG, mandant)));
		// Absatz 1 mit Fusszeilen erstellen
		Paragraph paragraphWithSupertext = PdfUtil.createParagraph(translate(INHALT_1A, mandant), 1);
		paragraphWithSupertext.add(PdfUtil.createSuperTextInText("1", SUPER_TEXT_SIZE, SUPER_TEXT_RISE));
		paragraphWithSupertext.add(new Chunk(translate(INHALT_1B, mandant)));
		paragraphWithSupertext.add(PdfUtil.createSuperTextInText("2", SUPER_TEXT_SIZE, SUPER_TEXT_RISE));
		paragraphWithSupertext.add(new Chunk(translate(INHALT_1C, mandant)));
		paragraphWithSupertext.add(PdfUtil.createSuperTextInText("3", SUPER_TEXT_SIZE, SUPER_TEXT_RISE));
		paragraphWithSupertext.add(new Chunk(translate(INHALT_1D, mandant)));
		paragraphWithSupertext.add(PdfUtil.createSuperTextInText("4", SUPER_TEXT_SIZE, SUPER_TEXT_RISE));
		paragraphWithSupertext.add(new Chunk(translate(INHALT_1E, mandant)));
		paragraphWithSupertext.add(PdfUtil.createSuperTextInText("5", SUPER_TEXT_SIZE, SUPER_TEXT_RISE));
		paragraphWithSupertext.add(new Chunk(translate(INHALT_1F, mandant)));
		document.add(paragraphWithSupertext);

		document.add(PdfUtil.createParagraph(translate(INHALT_2, mandant,
			this.rueckforderungFormular.getStufe2VoraussichtlicheBetrag())));
		document.add(PdfUtil.createParagraph(translate(INHALT_3, mandant,
			this.rueckforderungFormular.getStufe1FreigabeBetrag())));
	}

	private void createZweiteSeite(Document document) {
		List<Element> verfuegungElements = new ArrayList();
		verfuegungElements.add(PdfUtil.createParagraph(translate(GRUNDEN, mandant)));
		verfuegungElements.add(PdfUtil.createParagraph(translate(VERFUEGT, mandant), 2, PdfUtil.DEFAULT_FONT_BOLD));
		List<String> verfuegtList = new ArrayList();
		verfuegtList.add(translate(GRUND_1, mandant));
		verfuegtList.add(translate(GRUND_2, mandant, this.rueckforderungFormular.getStufe2VoraussichtlicheBetrag()));
		verfuegtList.add(translate(GRUND_3, mandant, this.rueckforderungFormular.getStufe1FreigabeBetrag()));
		verfuegungElements.add(PdfUtil.createListOrdered(verfuegtList));
		document.add(PdfUtil.createKeepTogetherTable(verfuegungElements, 0, 2));
	}

	private void createFusszeileNichtEintreten(@Nonnull PdfContentByte dirPdfContentByte) throws DocumentException {
		createFusszeile(
			dirPdfContentByte,
			Lists.newArrayList(translate(FUSSZEILE_1, mandant), translate(FUSSZEILE_2, mandant), translate(FUSSZEILE_3, mandant),
				translate(FUSSZEILE_4, mandant), translate(FUSSZEILE_5, mandant)),
			0, 0
		);
	}
}
