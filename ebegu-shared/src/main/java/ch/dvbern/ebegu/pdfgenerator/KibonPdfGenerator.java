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

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import sun.misc.Unsafe;

public abstract class KibonPdfGenerator {

	private static final Logger LOG = Logger.getLogger(KibonPdfGenerator.class.getName());

	protected static final String REFERENZNUMMER = "PdfGeneration_Referenznummer";
	protected static final String ABSENDER_TELEFON = "PdfGeneration_Telefon";
	protected static final String FAMILIE = "PdfGeneration_Familie";
	protected static final String NAME = "PdfGeneration_Name";
	protected static final String BETREUUNG_INSTITUTION = "PdfGeneration_Institution";


	@Nonnull
	private PdfGenerator pdfGenerator;

	@Nonnull
	protected final Gesuch gesuch;

	@Nonnull
	protected final GemeindeStammdaten gemeindeStammdaten;

	protected Locale sprache;

	// Load all kiBon-Fonts
	static {
		try
		{
			Font font = PdfUtilities.DEFAULT_FONT; // Make sure, that PdfUtilities is already loaded...
			FontFactory.register("/font/OpenSans-Light.ttf", "OpenSans-Light");
			FontFactory.register("/font/OpenSans-SemiBold.ttf", "OpenSans-Bold");
			Unsafe unsafe;
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe)field.get(null);
			updateFont("DEFAULT_FONT", FontFactory.getFont("OpenSans-Light", "Cp1252", true, 9.5F), unsafe);
			updateFont("DEFAULT_FONT_BOLD", FontFactory.getFont("OpenSans-Bold", "Cp1252", true, 9.5F), unsafe);
			updateFont("TITLE_FONT", FontFactory.getFont("OpenSans-Bold", "Cp1252", true, 12.0F), unsafe);
			updateFont("H1_FONT", FontFactory.getFont("OpenSans-Bold", "Cp1252", true, 12.0F),unsafe);
			updateFont("H2_FONT", PdfUtilities.DEFAULT_FONT, unsafe);
		}
		catch (Exception e)
		{
			LOG.severe("Could not load kiBon-Fonts for document");
		}
	}

	private static void updateFont(String constant, Font font, Unsafe unsafe) throws NoSuchFieldException {
		final Field fieldToUpdate = PdfUtilities.class.getDeclaredField(constant);
		final Object base = unsafe.staticFieldBase( fieldToUpdate );
		final long offset = unsafe.staticFieldOffset( fieldToUpdate );
		unsafe.putObject( base, offset, font);
	}


	@SuppressWarnings("PMD.ConstructorCallsOverridableMethod") // Stimmt nicht, die Methode ist final
	protected KibonPdfGenerator(@Nonnull Gesuch gesuch, @Nonnull GemeindeStammdaten stammdaten) {
		this.gesuch = gesuch;
		this.gemeindeStammdaten = stammdaten;
		initLocale(stammdaten);
		initGenerator(stammdaten);
	}

	@Nonnull
	protected abstract String getDocumentTitle();

	@Nonnull
	protected abstract List<String> getEmpfaengerAdresse();

	@Nonnull
	protected abstract CustomGenerator getCustomGenerator();


	public void generate(@Nonnull final OutputStream outputStream) throws InvoiceGeneratorException {
		getPdfGenerator().generate(outputStream, getDocumentTitle(), getEmpfaengerAdresse(), getCustomGenerator());
	}

	@Nonnull
	protected PdfGenerator getPdfGenerator() {
		return pdfGenerator;
	}

	@Nonnull
	protected Gesuch getGesuch() {
		return gesuch;
	}

	@Nonnull
	protected GemeindeStammdaten getGemeindeStammdaten() {
		return gemeindeStammdaten;
	}

	private void initLocale(@Nonnull GemeindeStammdaten stammdaten) {
		this.sprache = Locale.GERMAN; // Default, falls nichts gesetzt ist
		Sprache[] korrespondenzsprachen = stammdaten.getKorrespondenzsprache().getSprache();
		if (korrespondenzsprachen.length == 1) {
			sprache = korrespondenzsprachen[0].getLocale();
		} else {
			if (gesuch.getGesuchsteller1() != null && gesuch.getGesuchsteller1().getGesuchstellerJA().getKorrespondenzSprache() != null) {
				sprache = gesuch.getGesuchsteller1().getGesuchstellerJA().getKorrespondenzSprache().getLocale();
			}
		}
	}

	private void initGenerator(@Nonnull GemeindeStammdaten stammdaten) {
		this.pdfGenerator = PdfGenerator.create(stammdaten.getLogoContent(), getAbsenderAdresse());
	}

	@Nonnull
	protected final List<String> getAbsenderAdresse() {
		List<String> absender = new ArrayList<>();
		absender.addAll(getGemeindeAdresse());
		absender.addAll(getGemeindeKontaktdaten());
		return absender;
	}

	@Nonnull
	protected List<String> getGemeindeAdresse() {
		Adresse adresse = gemeindeStammdaten.getAdresse();
		List<String> gemeindeHeader = Arrays.asList(
			adresse.getAddressAsString(),
			""
		);
		return gemeindeHeader;
	}

	@Nonnull
	private List<String> getGemeindeKontaktdaten() {
		List<String> gemeindeKontaktdaten = Arrays.asList(
			translate(ABSENDER_TELEFON, gemeindeStammdaten.getTelefon()),
			gemeindeStammdaten.getMail(),
			gemeindeStammdaten.getWebseite(),
			"",
			"",
			gemeindeStammdaten.getGemeinde().getName() + ", " + Constants.DATE_FORMATTER.format(LocalDate.now())
		);
		return gemeindeKontaktdaten;
	}

	@Nonnull
	protected List<String> getFamilieAdresse() {
		final List<String> empfaengerAdresse = new ArrayList<>();
		empfaengerAdresse.add(translate(FAMILIE));
		empfaengerAdresse.add(KibonPrintUtil.getGesuchstellerNameAsString(getGesuch().getGesuchsteller1()));
		if (getGesuch().getGesuchsteller2() != null) {
			empfaengerAdresse.add(KibonPrintUtil.getGesuchstellerNameAsString(getGesuch().getGesuchsteller2()));
		}
		empfaengerAdresse.add(KibonPrintUtil.getGesuchstellerAddressAsString(getGesuch().getGesuchsteller1()));
		return empfaengerAdresse;
	}

	protected String translateEnumValue(@Nullable final Enum<?> key) {
		return ServerMessageUtil.translateEnumValue(key, sprache);
	}

	protected String translate(String key) {
		return ServerMessageUtil.getMessage(key, sprache);
	}

	protected String translate(String key, Object... args) {
		return ServerMessageUtil.getMessage(key, sprache, args);
	}
}
