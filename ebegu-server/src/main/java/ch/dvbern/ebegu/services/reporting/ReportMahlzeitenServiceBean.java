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

package ch.dvbern.ebegu.services.reporting;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuung_;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Dossier_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.entities.TSCalculationResult;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt_;
import ch.dvbern.ebegu.entities.Verfuegung_;
import ch.dvbern.ebegu.enums.BelegungTagesschuleModulIntervall;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.reporting.ReportMahlzeitenService;
import ch.dvbern.ebegu.reporting.mahlzeiten.MahlzeitenverguenstigungDataRow;
import ch.dvbern.ebegu.reporting.mahlzeiten.MahlzeitenverguenstigungExcelConverter;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.FileSaverService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.lib.cdipersistence.Persistence;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.ExcelMerger;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.commons.lang3.Validate;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jboss.ejb3.annotation.TransactionTimeout;

@Stateless
@Local(ReportMahlzeitenService.class)
public class ReportMahlzeitenServiceBean extends AbstractReportServiceBean implements ReportMahlzeitenService {

	private MahlzeitenverguenstigungExcelConverter mahlzeitenverguenstigungExcelConverter = new MahlzeitenverguenstigungExcelConverter();

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private Persistence persistence;

	@Nonnull
	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public UploadFileInfo generateExcelReportMahlzeiten(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nonnull Locale locale
	) throws ExcelMergeException {

		final ReportVorlage reportVorlage = ReportVorlage.VORLAGE_REPORT_MAHLZEITENVERGUENSTIGUNG;

		InputStream is = ReportMahlzeitenServiceBean.class.getResourceAsStream(reportVorlage.getTemplatePath());
		Validate.notNull(is, VORLAGE + reportVorlage.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

		List<MahlzeitenverguenstigungDataRow> reportData = getReportMahlzeitenverguenstigung(datumVon, datumBis);
		ExcelMergerDTO excelMergerDTO = mahlzeitenverguenstigungExcelConverter.toExcelMergerDTO(reportData, locale, datumVon, datumBis);

		mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
		mahlzeitenverguenstigungExcelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(bytes,
			ServerMessageUtil.translateEnumValue(reportVorlage.getDefaultExportFilename(), Locale.GERMAN) + ".xlsx",
			Constants.TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	@Nonnull
	private List<MahlzeitenverguenstigungDataRow> getReportMahlzeitenverguenstigung(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis
	) {

		List<VerfuegungZeitabschnitt> zeitabschnittList = getReportDataMahlzeitenverguenstigung(datumVon, datumBis);
		List<MahlzeitenverguenstigungDataRow> dataRows = convertToMahlzeitDataRow(zeitabschnittList);

		dataRows.sort(Comparator.comparing(MahlzeitenverguenstigungDataRow::getBgNummer)
			.thenComparing(MahlzeitenverguenstigungDataRow::getZeitabschnittVon));

		return dataRows;
	}

	private List<MahlzeitenverguenstigungDataRow> convertToMahlzeitDataRow(
		List<VerfuegungZeitabschnitt> zeitabschnittList
	) {

		List<MahlzeitenverguenstigungDataRow> dataRowList = new ArrayList<>();

		Map<Long, Gesuch> neustesVerfuegtesGesuchCache = new HashMap<>();

		for (VerfuegungZeitabschnitt zeitabschnitt : zeitabschnittList) {
			MahlzeitenverguenstigungDataRow row =
				createRowForKinderReport(zeitabschnitt, neustesVerfuegtesGesuchCache);
			dataRowList.add(row);
		}

		return dataRowList;
	}

	private MahlzeitenverguenstigungDataRow createRowForKinderReport(
		VerfuegungZeitabschnitt zeitabschnitt,
		Map<Long, Gesuch> neustesVerfuegtesGesuchCache) {

		AbstractPlatz gueltigePlatz = zeitabschnitt.getVerfuegung().getBetreuung();
		//und Anmeldungen auch betrachten
		// zu abklaeren, gibt es eine Verfuegung pro betreuung oder kann es gemischt sein?
		if (gueltigePlatz == null) {
			gueltigePlatz = zeitabschnitt.getVerfuegung().getAnmeldungTagesschule();
		}
		Objects.requireNonNull(gueltigePlatz);
		Gesuch gesuch = gueltigePlatz.extractGesuch();
		Gesuch gueltigeGesuch = null;

		//prüfen ob Gesuch ist gültig, und via GesuchService oder Cache holen, inkl. Kind & Betreuung
		if (!gesuch.isGueltig()) {

			gueltigeGesuch = getGueltigesGesuch(neustesVerfuegtesGesuchCache, gesuch);

			Optional<KindContainer> gueltigeKind = getGueltigesKind(zeitabschnitt, gueltigeGesuch);

			if (gueltigePlatz.getBetreuungsangebotTyp().isTagesschule()) {
				gueltigePlatz = getGueltigeAnmeldung(
					zeitabschnitt,
					zeitabschnitt.getVerfuegung().getAnmeldungTagesschule(),
					gueltigeKind);
			} else {
				gueltigePlatz =
					getGueltigeBetreuung(zeitabschnitt, zeitabschnitt.getVerfuegung().getBetreuung(), gueltigeKind);
			}

			neustesVerfuegtesGesuchCache.put(gesuch.getFall().getFallNummer(), gueltigeGesuch);
		} else {
			gueltigeGesuch = gesuch;
		}

		MahlzeitenverguenstigungDataRow row = new MahlzeitenverguenstigungDataRow();
		addStammdaten(row, zeitabschnitt);

		// Gesuchsteller 1
		GesuchstellerContainer gs1Container = gueltigeGesuch.getGesuchsteller1();
		if (gs1Container != null) {
			Gesuchsteller gs1 = gs1Container.getGesuchstellerJA();
			row.setGs1Name(gs1.getNachname());
			row.setGs1Vorname(gs1.getVorname());
		}
		// Gesuchsteller 2
		if (gueltigeGesuch.getGesuchsteller2() != null) {
			Gesuchsteller gs2 = gueltigeGesuch.getGesuchsteller2().getGesuchstellerJA();
			row.setGs2Name(gs2.getNachname());
			row.setGs2Vorname(gs2.getVorname());
		}

		// Kind
		Kind kind = gueltigePlatz.getKind().getKindJA();
		row.setKindName(kind.getNachname());
		row.setKindVorname(kind.getVorname());
		row.setKindGeburtsdatum(kind.getGeburtsdatum());

		// Betreuung
		addMahlzeitendatenToMahlzeitenverguenstigungDataRow(row, zeitabschnitt, gueltigePlatz);

		return row;
	}

	private void addMahlzeitendatenToMahlzeitenverguenstigungDataRow(
		MahlzeitenverguenstigungDataRow row,
		VerfuegungZeitabschnitt zeitabschnitt,
		AbstractPlatz platz
	) {
		row.setZeitabschnittVon(zeitabschnitt.getGueltigkeit().getGueltigAb());
		row.setZeitabschnittBis(zeitabschnitt.getGueltigkeit().getGueltigBis());
		Objects.requireNonNull(zeitabschnitt.getBgCalculationResultGemeinde());
		if (platz.getBetreuungsangebotTyp().isTagesschule()) {
			TSCalculationResult tsCalculationResultMit =
				zeitabschnitt.getBgCalculationResultGemeinde().getTsCalculationResultMitPaedagogischerBetreuung();
			BigDecimal berechneteMahlzeitenverguenstigung = BigDecimal.ZERO;
			if (tsCalculationResultMit != null) {
				berechneteMahlzeitenverguenstigung = tsCalculationResultMit.getVerpflegungskostenVerguenstigt();
			}
			TSCalculationResult tsCalculationResultOhne =
				zeitabschnitt.getBgCalculationResultGemeinde().getTsCalculationResultOhnePaedagogischerBetreuung();
			if (tsCalculationResultOhne != null) {
				berechneteMahlzeitenverguenstigung =
					berechneteMahlzeitenverguenstigung.add(tsCalculationResultOhne.getVerpflegungskostenVerguenstigt());
			}
			row.setBerechneteMahlzeitenverguenstigung(berechneteMahlzeitenverguenstigung);
			//immer 0
			row.setAnzahlNebenmahlzeiten(BigDecimal.ZERO);
			row.setKostenNebenmahlzeiten(BigDecimal.ZERO);
			//berechnen der anzahl von Hauptmahlzeiten und die Kosten
			BigDecimal totalHauptMahlzeiten = BigDecimal.ZERO;
			BigDecimal totalKostenHauptMahlzeiten = BigDecimal.ZERO;
			double scale = 1.0;
			if (zeitabschnitt.getVerfuegung().getAnmeldungTagesschule() != null
				&& zeitabschnitt.getVerfuegung().getAnmeldungTagesschule().getBelegungTagesschule() != null) {
				for (BelegungTagesschuleModul belegungTagesschuleModul : zeitabschnitt.getVerfuegung()
					.getAnmeldungTagesschule()
					.getBelegungTagesschule()
					.getBelegungTagesschuleModule()) {
					scale =
						belegungTagesschuleModul.getIntervall() == BelegungTagesschuleModulIntervall.ALLE_ZWEI_WOCHEN ?
							0.5 : 1.0;
					BigDecimal verpflegungskosten =
						belegungTagesschuleModul.getModulTagesschule()
							.getModulTagesschuleGroup()
							.getVerpflegungskosten();
					if (verpflegungskosten != null && verpflegungskosten.compareTo(BigDecimal.ZERO) > 0) {
						totalKostenHauptMahlzeiten = MathUtil.DEFAULT.add(
							totalKostenHauptMahlzeiten,
							MathUtil.DEFAULT.multiply(verpflegungskosten, BigDecimal.valueOf(scale)));
						totalHauptMahlzeiten = MathUtil.DEFAULT.add(totalHauptMahlzeiten, BigDecimal.valueOf(scale));
					}
				}
			}
			row.setKostenHauptmahlzeiten(totalKostenHauptMahlzeiten);
			row.setAnzahlHauptmahlzeiten(totalHauptMahlzeiten);

		} else {
			row.setBerechneteMahlzeitenverguenstigung(zeitabschnitt.getBgCalculationResultGemeinde()
				.getVerguenstigungMahlzeitenTotal());
			if (zeitabschnitt.getVerfuegung().getBetreuung() != null) {
				for (BetreuungspensumContainer betreuungspensumContainer : zeitabschnitt.getVerfuegung()
					.getBetreuung()
					.getBetreuungspensumContainers()
				) {
					Betreuungspensum betreuungspensum = betreuungspensumContainer.getBetreuungspensumJA();
					if(betreuungspensum.getGueltigkeit().contains(zeitabschnitt.getGueltigkeit())){
						row.setAnzahlHauptmahlzeiten(BigDecimal.valueOf(betreuungspensum.getMonatlicheHauptmahlzeiten()));
						row.setKostenHauptmahlzeiten(betreuungspensum.getTarifProHauptmahlzeit()); //TODO abklaeren ob total oder fuer eine, sonst mutliply
						row.setKostenNebenmahlzeiten(betreuungspensum.getTarifProNebenmahlzeit());
						row.setAnzahlNebenmahlzeiten(BigDecimal.valueOf(betreuungspensum.getMonatlicheNebenmahlzeiten()));
						break;
					}
				}
			}
		}
	}

	private void addStammdaten(
		MahlzeitenverguenstigungDataRow row,
		VerfuegungZeitabschnitt zeitabschnitt
	) {
		AbstractPlatz platz = zeitabschnitt.getVerfuegung().getBetreuung();
		if (platz != null) {
			platz = zeitabschnitt.getVerfuegung().getAnmeldungTagesschule();
		}
		Objects.requireNonNull(platz);
		row.setInstitution(platz
			.getInstitutionStammdaten()
			.getInstitution()
			.getName());
		if (platz.getInstitutionStammdaten().getInstitution().getTraegerschaft() != null) {
			row.setTraegerschaft(platz.getInstitutionStammdaten().getInstitution().getTraegerschaft().getName());
		}
		row.setBetreuungsTyp(platz.getBetreuungsangebotTyp());
		row.setBgNummer(platz.getBGNummer());
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	@Nonnull
	private List<VerfuegungZeitabschnitt> getReportDataMahlzeitenverguenstigung(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis) {
		validateDateParams(datumVon, datumBis);

		Benutzer user = benutzerService.getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"getReportDataMahlzeitenverguenstigung", NO_USER_IS_LOGGED_IN));

		// Alle Verfuegungszeitabschnitte zwischen datumVon und datumBis. Aber pro Fall immer nur das zuletzt
		// verfuegte.
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<VerfuegungZeitabschnitt> query = builder.createQuery(VerfuegungZeitabschnitt.class);
		query.distinct(true);
		Root<VerfuegungZeitabschnitt> root = query.from(VerfuegungZeitabschnitt.class);
		Join<VerfuegungZeitabschnitt, Verfuegung> joinVerfuegung = root.join(VerfuegungZeitabschnitt_.verfuegung);
		Join<Verfuegung, Betreuung> joinBetreuung = joinVerfuegung.join(Verfuegung_.betreuung);
		Join<Betreuung, KindContainer> joinBetreuungKindContainer = joinBetreuung.join(Betreuung_.kind, JoinType.LEFT);
		Join<KindContainer, Gesuch> joinBetreuungGesuch =
			joinBetreuungKindContainer.join(KindContainer_.gesuch, JoinType.LEFT);
		Join<Gesuch, Dossier> joinBetreuungDossier = joinBetreuungGesuch.join(Gesuch_.dossier, JoinType.LEFT);
		Join<Dossier, Gemeinde> joinBetreuungGemeinde = joinBetreuungDossier.join(Dossier_.gemeinde, JoinType.LEFT);

		Join<Verfuegung, AnmeldungTagesschule> joinAnmeldung = joinVerfuegung.join(Verfuegung_.anmeldungTagesschule);
		Join<AnmeldungTagesschule, KindContainer> joinAnmeldungKindContainer =
			joinAnmeldung.join(Betreuung_.kind, JoinType.LEFT);
		Join<KindContainer, Gesuch> joinAnmeldungGesuch =
			joinAnmeldungKindContainer.join(KindContainer_.gesuch, JoinType.LEFT);
		Join<Gesuch, Dossier> joinAnmeldungDossier = joinAnmeldungGesuch.join(Gesuch_.dossier, JoinType.LEFT);
		Join<Dossier, Gemeinde> joinAnmeldungGemeinde = joinAnmeldungDossier.join(Dossier_.gemeinde, JoinType.LEFT);

		List<Predicate> predicatesToUse = new ArrayList<>();

		//TODO: Superadmin berucksichtigen + nur Gesuchen wo mahlzeiten beantragt sind
		// startAbschnitt <= datumBis && endeAbschnitt >= datumVon
		Path<DateRange> dateRangePath = root.get(AbstractDateRangedEntity_.gueltigkeit);
		Predicate predicateStart = builder.lessThanOrEqualTo(dateRangePath.get(DateRange_.gueltigAb), datumBis);
		predicatesToUse.add(predicateStart);
		Predicate predicateEnd = builder.greaterThanOrEqualTo(dateRangePath.get(DateRange_.gueltigBis), datumVon);
		predicatesToUse.add(predicateEnd);

		// Nur neueste Verfuegung jedes Falls beachten
		Predicate predicateGueltig = builder.equal(joinBetreuung.get(Betreuung_.gueltig), Boolean.TRUE);
		predicatesToUse.add(predicateGueltig);

		// Nur Gesuche von Gemeinden, fuer die ich berechtigt bin
		Collection<Gemeinde> gemeindenForBenutzer = user.extractGemeindenForUser();
		if(gemeindenForBenutzer.isEmpty()){
			return Collections.emptyList();
		}
		Predicate inGemeindeForBetreuung = joinBetreuungGemeinde.in(gemeindenForBenutzer);
		Predicate inGemeindeForTagesschule = joinAnmeldungGemeinde.in(gemeindenForBenutzer);
		Predicate inGemeinde = builder.or(inGemeindeForBetreuung, inGemeindeForTagesschule);
		predicatesToUse.add(inGemeinde);

		Predicate predicateForBenutzerRole = getPredicateForBenutzerRole(builder, root);
		if (predicateForBenutzerRole != null) {
			predicatesToUse.add(predicateForBenutzerRole);
		}
		query.where(CriteriaQueryHelper.concatenateExpressions(builder, predicatesToUse));
		return persistence.getCriteriaResults(query);
	}
}
