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

package ch.dvbern.ebegu.reporting;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.reporting.DatumTyp;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.reporting.benutzer.BenutzerDataRow;
import ch.dvbern.ebegu.reporting.gesuchstichtag.GesuchStichtagDataRow;
import ch.dvbern.ebegu.reporting.gesuchzeitraum.GesuchZeitraumDataRow;
import ch.dvbern.ebegu.reporting.kanton.KantonDataRow;
import ch.dvbern.ebegu.reporting.kanton.mitarbeiterinnen.MitarbeiterinnenDataRow;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;

public interface ReportService {

	// Gesuch Stichtag
	@Nonnull
	List<GesuchStichtagDataRow> getReportDataGesuchStichtag(@Nonnull LocalDate date, @Nullable String gesuchPeriodeID, @Nonnull Mandant mandant)
		throws IOException, URISyntaxException;

	@Nonnull
	UploadFileInfo generateExcelReportGesuchStichtag(
		@Nonnull LocalDate date,
		@Nullable String gesuchPeriodeID,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant)
		throws ExcelMergeException, IOException, MergeDocException, URISyntaxException;

	// Gesuch Zeitraum

	@Nonnull
	List<GesuchZeitraumDataRow> getReportDataGesuchZeitraum(
		@Nonnull LocalDate dateVon,
		@Nonnull LocalDate dateBis,
		@Nonnull DatumTyp datumTyp,
		@Nullable String gesuchPeriodeID,
		@Nonnull Mandant mandant)
		throws IOException, URISyntaxException;

	@Nonnull
	UploadFileInfo generateExcelReportGesuchZeitraum(
		@Nonnull LocalDate dateVon,
		@Nonnull LocalDate dateBis,
		@Nonnull DatumTyp datumTyp,
		@Nullable String gesuchPeriodeID,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	)
		throws ExcelMergeException, IOException, MergeDocException, URISyntaxException;

	// Kanton
	@Nonnull
	List<KantonDataRow> getReportDataKanton(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant)
		throws IOException, URISyntaxException;

	@Nonnull
	UploadFileInfo generateExcelReportKanton(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nullable BigDecimal kantonSelbstbehalt,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant)
		throws ExcelMergeException, IOException, MergeDocException, URISyntaxException;

	// MitarbeterInnen
	@Nonnull
	List<MitarbeiterinnenDataRow> getReportMitarbeiterinnen(@Nonnull LocalDate datumVon, @Nonnull LocalDate datumBis, @Nonnull Mandant mandant)
		throws IOException, URISyntaxException;

	@Nonnull
	UploadFileInfo generateExcelReportMitarbeiterinnen(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant)
		throws ExcelMergeException, IOException, MergeDocException, URISyntaxException;

	// Zahlungen

	@Nonnull
	UploadFileInfo generateExcelReportZahlungAuftrag(@Nonnull String auftragId, @Nonnull Locale locale)
		throws ExcelMergeException, IOException;

	@Nonnull
	UploadFileInfo generateExcelReportZahlung(@Nonnull String zahlungId, @Nonnull Locale locale)
		throws ExcelMergeException, IOException;

	@Nonnull
	UploadFileInfo generateExcelReportZahlungPeriode(@Nonnull String gesuchsperiodeId, @Nonnull Locale locale)
			throws ExcelMergeException, IOException;

	// Gesuchsteller / Kinder / Betreuung
	@Nonnull
	UploadFileInfo generateExcelReportGesuchstellerKinderBetreuung(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nullable String gesuchPeriodeId,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	)
		throws ExcelMergeException, IOException, MergeDocException, URISyntaxException;

	boolean isSozialhilfeBezueger(
			@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
			@Nonnull FamiliensituationContainer familiensituationContainer,
			@Nonnull Familiensituation familiensituation
	);

	@Nonnull
	UploadFileInfo generateExcelReportKinder(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nullable String gesuchPeriodeId,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	)
		throws ExcelMergeException, IOException, MergeDocException, URISyntaxException;

	@Nonnull
	UploadFileInfo generateExcelReportGesuchsteller(@Nonnull LocalDate stichtag, @Nonnull Locale locale, @Nonnull Mandant mandant)
		throws ExcelMergeException, IOException, MergeDocException, URISyntaxException;

	@Nonnull
	UploadFileInfo generateExcelReportBenutzer(@Nonnull Locale locale, @Nonnull Mandant mandant)
		throws ExcelMergeException, IOException;

	@Nonnull
	List<BenutzerDataRow> getReportDataBenutzer(@Nonnull Locale locale, @Nonnull Mandant mandant);

	@Nonnull
	UploadFileInfo generateExcelReportInstitutionen(@Nonnull Locale locale) throws ExcelMergeException, IOException;

	@Nonnull
	UploadFileInfo generateExcelReportFerienbetreuung(@Nonnull Locale locale) throws ExcelMergeException, IOException;

}
