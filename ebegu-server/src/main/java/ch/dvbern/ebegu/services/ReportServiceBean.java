package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.*;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.reporting.gesuchstellerKinderBetreuung.GesuchstellerKinderBetreuungDataRow;
import ch.dvbern.ebegu.reporting.gesuchstellerKinderBetreuung.GesuchstellerKinderBetreuungExcelConverter;
import ch.dvbern.ebegu.reporting.gesuchstellerKinderBetreuung.MergeFieldGesuchstellerKinderBetreuung;
import ch.dvbern.ebegu.reporting.gesuchstichtag.GesuchStichtagDataRow;
import ch.dvbern.ebegu.reporting.gesuchstichtag.GeuschStichtagExcelConverter;
import ch.dvbern.ebegu.reporting.gesuchstichtag.MergeFieldGesuchStichtag;
import ch.dvbern.ebegu.reporting.gesuchzeitraum.GesuchZeitraumDataRow;
import ch.dvbern.ebegu.reporting.gesuchzeitraum.GeuschZeitraumExcelConverter;
import ch.dvbern.ebegu.reporting.gesuchzeitraum.MergeFieldGesuchZeitraum;
import ch.dvbern.ebegu.reporting.kanton.KantonDataRow;
import ch.dvbern.ebegu.reporting.kanton.KantonExcelConverter;
import ch.dvbern.ebegu.reporting.kanton.MergeFieldKanton;
import ch.dvbern.ebegu.reporting.lib.DateUtil;
import ch.dvbern.ebegu.reporting.zahlungauftrag.MergeFieldZahlungAuftrag;
import ch.dvbern.ebegu.reporting.zahlungauftrag.MergeFieldZahlungAuftragPeriode;
import ch.dvbern.ebegu.reporting.zahlungauftrag.ZahlungAuftragExcelConverter;
import ch.dvbern.ebegu.reporting.zahlungauftrag.ZahlungAuftragPeriodeExcelConverter;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.lib.cdipersistence.Persistence;
import ch.dvbern.lib.excelmerger.ExcelMergeException;
import ch.dvbern.lib.excelmerger.ExcelMerger;
import ch.dvbern.lib.excelmerger.ExcelMergerDTO;
import ch.dvbern.lib.excelmerger.MergeField;
import org.apache.commons.lang3.Validate;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ch.dvbern.ebegu.enums.UserRoleName.*;
import static ch.dvbern.ebegu.services.ReportServiceBean.ReportResource.*;

/**
 * Copyright (c) 2016 DV Bern AG, Switzerland
 * <p>
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschuetzt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulaessig. Dies gilt
 * insbesondere fuer Vervielfaeltigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht uebergeben ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 * <p>
 * Created by medu on 31/01/2017.
 */
@Stateless
@Local(ReportService.class)
public class ReportServiceBean extends AbstractReportServiceBean implements ReportService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportServiceBean.class);

	public static final String DATA = "Data";
	public static final String NICHT_GEFUNDEN = "' nicht gefunden";
	public static final String VORLAGE = "Vorlage '";
	private static final String VALIDIERUNG_DATUM_VON = "Das Argument 'datumVon' darf nicht leer sein";
	private static final String VALIDIERUNG_DATUM_BIS = "Das Argument 'datumBis' darf nicht leer sein";

	@Inject
	private GeuschStichtagExcelConverter geuschStichtagExcelConverter;

	@Inject
	private GeuschZeitraumExcelConverter geuschZeitraumExcelConverter;

	@Inject
	private KantonExcelConverter kantonExcelConverter;

	@Inject
	private ZahlungAuftragExcelConverter zahlungAuftragExcelConverter;

	@Inject
	private ZahlungAuftragPeriodeExcelConverter zahlungAuftragPeriodeExcelConverter;

	@Inject
	private GesuchstellerKinderBetreuungExcelConverter gesuchstellerKinderBetreuungExcelConverter;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private Persistence<Gesuch> persistence;

	@Inject
	private ZahlungService zahlungService;

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private KindService kindService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private GesuchstellerAdresseService gesuchstellerAdresseService;

	private static final String MIME_TYPE_EXCEL = "application/vnd.ms-excel";
	private static final String TEMP_REPORT_FOLDERNAME = "tempReports";

	@Override
	@RolesAllowed({SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, REVISOR, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, SCHULAMT})
	public List<GesuchStichtagDataRow> getReportDataGesuchStichtag(@Nonnull LocalDateTime datetime, @Nullable String gesuchPeriodeID) {

		Objects.requireNonNull(datetime, "Das Argument 'date' darf nicht leer sein");

		EntityManager em = persistence.getEntityManager();

		List<GesuchStichtagDataRow> results = null;

		if (em != null) {

			Query gesuchStichtagQuery = em.createNamedQuery("GesuchStichtagNativeSQLQuery");
			// Wir rechnen zum Stichtag einen Tag dazu, damit es bis 24.00 des Vorabends gilt.
			gesuchStichtagQuery.setParameter("stichTagDate", DateUtil.SQL_DATETIME_FORMAT.format(datetime.plusDays(1)));
			gesuchStichtagQuery.setParameter("gesuchPeriodeID", gesuchPeriodeID);
			gesuchStichtagQuery.setParameter("onlySchulamt", principalBean.isCallerInRole(SCHULAMT) ? 1 : 0);
			results = gesuchStichtagQuery.getResultList();
		}
		return results;
	}

	@Override
	@RolesAllowed({SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, REVISOR, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, SCHULAMT})
	public UploadFileInfo generateExcelReportGesuchStichtag(@Nonnull LocalDateTime datetime, @Nullable String gesuchPeriodeID) throws ExcelMergeException, IOException, MergeDocException, URISyntaxException {

		Objects.requireNonNull(datetime, "Das Argument 'date' darf nicht leer sein");

		final ReportResource reportResource = VORLAGE_REPORT_GESUCH_STICHTAG;

		InputStream is = ReportServiceBean.class.getResourceAsStream(reportResource.getTemplatePath());
		Objects.requireNonNull(is, VORLAGE+ reportResource.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportResource.getDataSheetName());

		List<GesuchStichtagDataRow> reportData = getReportDataGesuchStichtag(datetime, gesuchPeriodeID);
		ExcelMergerDTO excelMergerDTO = geuschStichtagExcelConverter.toExcelMergerDTO(reportData, Locale.getDefault());

		mergeData(sheet, excelMergerDTO, reportResource.getMergeFields());
		geuschStichtagExcelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(bytes,
			reportResource.getDefaultExportFilename(),
			TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	@Override
	@RolesAllowed({SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, REVISOR, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, SCHULAMT})
	public List<GesuchZeitraumDataRow> getReportDataGesuchZeitraum(@Nonnull LocalDateTime datetimeVon, @Nonnull LocalDateTime datetimeBis, @Nullable String gesuchPeriodeID) throws IOException, URISyntaxException {

		Objects.requireNonNull(datetimeVon, "Das Argument 'datetimeVon' darf nicht leer sein");
		Objects.requireNonNull(datetimeBis, "Das Argument 'datetimeBis' darf nicht leer sein");

		// Bevor wir die Statistik starten, muessen gewissen Werte nachgefuehrt werden
		runStatisticsBetreuung();
		runStatisticsAbwesenheiten();
		runStatisticsKinder();

		EntityManager em = persistence.getEntityManager();

		List<GesuchZeitraumDataRow> results = null;

		if (em != null) {
			Query gesuchPeriodeQuery = em.createNamedQuery("GesuchZeitraumNativeSQLQuery");
			gesuchPeriodeQuery.setParameter("fromDateTime", DateUtil.SQL_DATETIME_FORMAT.format(datetimeVon));
			gesuchPeriodeQuery.setParameter("fromDate", DateUtil.SQL_DATE_FORMAT.format(datetimeVon));
			gesuchPeriodeQuery.setParameter("toDateTime", DateUtil.SQL_DATETIME_FORMAT.format(datetimeBis));
			gesuchPeriodeQuery.setParameter("toDate", DateUtil.SQL_DATE_FORMAT.format(datetimeBis));
			gesuchPeriodeQuery.setParameter("gesuchPeriodeID", gesuchPeriodeID);
			gesuchPeriodeQuery.setParameter("onlySchulamt", principalBean.isCallerInRole(SCHULAMT) ? 1 : 0);
			results = gesuchPeriodeQuery.getResultList();
		}
		return results;
	}

	@Override
	@RolesAllowed({SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, REVISOR, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, SCHULAMT})
	public UploadFileInfo generateExcelReportGesuchZeitraum(@Nonnull LocalDateTime datetimeVon, @Nonnull LocalDateTime datetimeBis, @Nullable String gesuchPeriodeID) throws ExcelMergeException, IOException, MergeDocException, URISyntaxException {

		Objects.requireNonNull(datetimeVon, "Das Argument 'datetimeVon' darf nicht leer sein");
		Objects.requireNonNull(datetimeBis, "Das Argument 'datetimeBis' darf nicht leer sein");

		final ReportResource reportResource = VORLAGE_REPORT_GESUCH_ZEITRAUM;

		InputStream is = ReportServiceBean.class.getResourceAsStream(reportResource.getTemplatePath());
		Objects.requireNonNull(is, VORLAGE + reportResource.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportResource.getDataSheetName());

		List<GesuchZeitraumDataRow> reportData = getReportDataGesuchZeitraum(datetimeVon, datetimeBis, gesuchPeriodeID);
		ExcelMergerDTO excelMergerDTO = geuschZeitraumExcelConverter.toExcelMergerDTO(reportData, Locale.getDefault());

		mergeData(sheet, excelMergerDTO, reportResource.getMergeFields());
		geuschZeitraumExcelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(bytes,
			reportResource.getDefaultExportFilename(),
			TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	@SuppressWarnings("PMD.NcssMethodCount, PMD.AvoidDuplicateLiterals")
	@Override
	@RolesAllowed({SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, REVISOR, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, SCHULAMT})
	public List<KantonDataRow> getReportDataKanton(@Nonnull LocalDate datumVon, @Nonnull LocalDate datumBis) throws IOException, URISyntaxException {
		Validate.notNull(datumVon, VALIDIERUNG_DATUM_VON);
		Validate.notNull(datumBis, VALIDIERUNG_DATUM_BIS);

		Collection<Gesuchsperiode> relevanteGesuchsperioden = gesuchsperiodeService.getGesuchsperiodenBetween(datumVon, datumBis);
		if (relevanteGesuchsperioden.isEmpty()) {
			return Collections.emptyList();
		}
		// Alle Verfuegungszeitabschnitte zwischen datumVon und datumBis. Aber pro Fall immer nur das zuletzt verfuegte.
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<VerfuegungZeitabschnitt> query = builder.createQuery(VerfuegungZeitabschnitt.class);
		query.distinct(true);
		Root<VerfuegungZeitabschnitt> root = query.from(VerfuegungZeitabschnitt.class);
		List<Expression<Boolean>> predicatesToUse = new ArrayList<>();

		// startAbschnitt <= datumBis && endeAbschnitt >= datumVon
		Predicate predicateStart = builder.lessThanOrEqualTo(root.get(VerfuegungZeitabschnitt_.gueltigkeit).get(DateRange_.gueltigAb), datumBis);
		predicatesToUse.add(predicateStart);
		Predicate predicateEnd = builder.greaterThanOrEqualTo(root.get(VerfuegungZeitabschnitt_.gueltigkeit).get(DateRange_.gueltigBis), datumVon);
		predicatesToUse.add(predicateEnd);

		// nur das neuest verfuegte Gesuch
        List<String> idsOfLetztVerfuegteAntraege = new ArrayList<>();
        for (Gesuchsperiode gesuchsperiode : relevanteGesuchsperioden) {
            idsOfLetztVerfuegteAntraege.addAll(gesuchService.getNeuesteVerfuegteAntraege(gesuchsperiode));
        }
        if (!idsOfLetztVerfuegteAntraege.isEmpty()) {
            Predicate predicateAktuellesGesuch = root.get(VerfuegungZeitabschnitt_.verfuegung).get(Verfuegung_.betreuung).get(Betreuung_.kind).get(KindContainer_.gesuch).get(Gesuch_.id).in(idsOfLetztVerfuegteAntraege);
            predicatesToUse.add(predicateAktuellesGesuch);
        } else {
            return Collections.emptyList();
        }

		// Sichtbarkeit nach eingeloggtem Benutzer
		boolean isInstitutionsbenutzer = principalBean.isCallerInAnyOfRole(UserRole.SACHBEARBEITER_INSTITUTION, UserRole.SACHBEARBEITER_TRAEGERSCHAFT);
		if (isInstitutionsbenutzer) {
			Collection<Institution> allowedInstitutionen = institutionService.getAllowedInstitutionenForCurrentBenutzer();
			Predicate predicateAllowedInstitutionen = root.get(VerfuegungZeitabschnitt_.verfuegung).get(Verfuegung_.betreuung).get(Betreuung_.institutionStammdaten).get(InstitutionStammdaten_.institution).in(allowedInstitutionen);
			predicatesToUse.add(predicateAllowedInstitutionen);
		}

		query.where(CriteriaQueryHelper.concatenateExpressions(builder, predicatesToUse));
		List<VerfuegungZeitabschnitt> zeitabschnittList = persistence.getCriteriaResults(query);
		return convertToKantonDataRow(zeitabschnittList);
	}

	private List<KantonDataRow> convertToKantonDataRow(List<VerfuegungZeitabschnitt> zeitabschnittList) {
		List<KantonDataRow> kantonDataRowList = new ArrayList<>();
		for (VerfuegungZeitabschnitt zeitabschnitt : zeitabschnittList) {
			KantonDataRow row = new KantonDataRow();
			row.setBgNummer(zeitabschnitt.getVerfuegung().getBetreuung().getBGNummer());
			row.setGesuchId(zeitabschnitt.getVerfuegung().getBetreuung().extractGesuch().getId());
			row.setName(zeitabschnitt.getVerfuegung().getBetreuung().getKind().getKindJA().getNachname());
			row.setVorname(zeitabschnitt.getVerfuegung().getBetreuung().getKind().getKindJA().getVorname());
			row.setGeburtsdatum(zeitabschnitt.getVerfuegung().getBetreuung().getKind().getKindJA().getGeburtsdatum());
			row.setZeitabschnittVon(zeitabschnitt.getGueltigkeit().getGueltigAb());
			row.setZeitabschnittBis(zeitabschnitt.getGueltigkeit().getGueltigBis());
			row.setBgPensum(MathUtil.DEFAULT.from(zeitabschnitt.getBgPensum()));
			row.setElternbeitrag(zeitabschnitt.getElternbeitrag());
			row.setVerguenstigung(zeitabschnitt.getVerguenstigung());
			row.setInstitution(zeitabschnitt.getVerfuegung().getBetreuung().getInstitutionStammdaten().getInstitution().getName());
			row.setBetreuungsTyp(zeitabschnitt.getVerfuegung().getBetreuung().getBetreuungsangebotTyp().name());
			row.setOeffnungstage(zeitabschnitt.getVerfuegung().getBetreuung().getInstitutionStammdaten().getOeffnungstage());
			kantonDataRowList.add(row);
		}
		return kantonDataRowList;
	}

	@Override
	@RolesAllowed({SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, REVISOR, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, SCHULAMT})
	public UploadFileInfo generateExcelReportKanton(@Nonnull LocalDate datumVon, @Nonnull LocalDate datumBis) throws ExcelMergeException, IOException, MergeDocException, URISyntaxException {
		Validate.notNull(datumVon, VALIDIERUNG_DATUM_VON);
		Validate.notNull(datumBis, VALIDIERUNG_DATUM_BIS);

		final ReportResource reportResource = VORLAGE_REPORT_KANTON;

		InputStream is = ReportServiceBean.class.getResourceAsStream(reportResource.getTemplatePath());
		Validate.notNull(is, VORLAGE + reportResource.getTemplatePath() + "' nicht gefunden");

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportResource.getDataSheetName());

		List<KantonDataRow> reportData = getReportDataKanton(datumVon, datumBis);
		ExcelMergerDTO excelMergerDTO = kantonExcelConverter.toExcelMergerDTO(reportData, Locale.getDefault(), datumVon, datumBis);

		mergeData(sheet, excelMergerDTO, reportResource.getMergeFields());
		kantonExcelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(bytes,
			reportResource.getDefaultExportFilename(),
			TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	@Nonnull
	private MimeType getContentTypeForExport() {
		try {
			return new MimeType(MIME_TYPE_EXCEL);
		} catch (MimeTypeParseException e) {
			throw new EbeguRuntimeException("getContentTypeForExport", "could not parse mime type", e, MIME_TYPE_EXCEL);

		}
	}

	@Override
	@RolesAllowed(value = {SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TRAEGERSCHAFT})
	public UploadFileInfo generateExcelReportZahlungAuftrag(String auftragId) throws ExcelMergeException {

		Zahlungsauftrag zahlungsauftrag = zahlungService.findZahlungsauftrag(auftragId)
			.orElseThrow(() -> new EbeguEntityNotFoundException("generateExcelReportZahlungAuftrag", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, auftragId));

		return getUploadFileInfoZahlung(zahlungsauftrag.getZahlungen(), zahlungsauftrag.getBeschrieb() + ".xlsx");
	}

	@Override
	@RolesAllowed(value = {SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TRAEGERSCHAFT})
	public UploadFileInfo generateExcelReportZahlung(String zahlungId) throws ExcelMergeException {

		List<Zahlung> reportData = new ArrayList<>();

		Zahlung zahlung = zahlungService.findZahlung(zahlungId)
			.orElseThrow(() -> new EbeguEntityNotFoundException("generateExcelReportZahlungAuftrag", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, zahlungId));

		reportData.add(zahlung);

		return getUploadFileInfoZahlung(reportData, "Zahlungen_" + zahlung.getInstitutionStammdaten().getInstitution().getName() + ".xlsx");
	}

	private UploadFileInfo getUploadFileInfoZahlung(List<Zahlung> reportData, String excelFileName) throws ExcelMergeException {
		final ReportResource reportResource = VORLAGE_REPORT_ZAHLUNG_AUFTRAG;

		InputStream is = ReportServiceBean.class.getResourceAsStream(reportResource.getTemplatePath());
		Objects.requireNonNull(is, VORLAGE + reportResource.getTemplatePath() +NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportResource.getDataSheetName());

		Collection<Institution> allowedInst = institutionService.getAllowedInstitutionenForCurrentBenutzer();

		ExcelMergerDTO excelMergerDTO = zahlungAuftragExcelConverter.toExcelMergerDTO(reportData, Locale.getDefault(), principalBean.discoverMostPrivilegedRole(), allowedInst);

		mergeData(sheet, excelMergerDTO, reportResource.getMergeFields());
		zahlungAuftragExcelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(bytes,
			excelFileName,
			TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	@Override
	@RolesAllowed(value = {SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TRAEGERSCHAFT})
	public UploadFileInfo generateExcelReportZahlungPeriode(@Nonnull String gesuchsperiodeId) throws ExcelMergeException {

		Gesuchsperiode gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId)
			.orElseThrow(() -> new EbeguEntityNotFoundException("generateExcelReportZahlungPeriode", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuchsperiodeId));

		final Collection<Zahlungsauftrag> zahlungsauftraegeInPeriode = zahlungService.getZahlungsauftraegeInPeriode(gesuchsperiode.getGueltigkeit().getGueltigAb(), gesuchsperiode.getGueltigkeit().getGueltigBis());

		final ReportResource reportResource = VORLAGE_REPORT_ZAHLUNG_AUFTRAG_PERIODE;

		InputStream is = ReportServiceBean.class.getResourceAsStream(reportResource.getTemplatePath());
		Objects.requireNonNull(is, VORLAGE + reportResource.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportResource.getDataSheetName());

		final List<Zahlung> allZahlungen = zahlungsauftraegeInPeriode.stream()
			.flatMap(zahlungsauftrag -> zahlungsauftrag.getZahlungen().stream())
			.collect(Collectors.toList());

		ExcelMergerDTO excelMergerDTO = zahlungAuftragPeriodeExcelConverter.toExcelMergerDTO(allZahlungen, gesuchsperiode.getGesuchsperiodeString(), Locale.getDefault());

		mergeData(sheet, excelMergerDTO, reportResource.getMergeFields());
		zahlungAuftragPeriodeExcelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(bytes,
			reportResource.getDefaultExportFilename(),
			TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	@Override
	@RolesAllowed({SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, REVISOR, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, SCHULAMT})
	public List<GesuchstellerKinderBetreuungDataRow> getReportDataGesuchstellerKinderBetreuung(@Nonnull LocalDate datumVon, @Nonnull LocalDate datumBis, @Nullable Gesuchsperiode gesuchsperiode) throws IOException, URISyntaxException {
		Validate.notNull(datumVon, VALIDIERUNG_DATUM_VON);
		Validate.notNull(datumBis, VALIDIERUNG_DATUM_BIS);

		// Alle Verfuegungszeitabschnitte zwischen datumVon und datumBis. Aber pro Fall immer nur das zuletzt verfuegte.
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<VerfuegungZeitabschnitt> query = builder.createQuery(VerfuegungZeitabschnitt.class);
		query.distinct(true);
		Root<VerfuegungZeitabschnitt> root = query.from(VerfuegungZeitabschnitt.class);
		List<Expression<Boolean>> predicatesToUse = new ArrayList<>();

		// startAbschnitt <= datumBis && endeAbschnitt >= datumVon
		Predicate predicateStart = builder.lessThanOrEqualTo(root.get(VerfuegungZeitabschnitt_.gueltigkeit).get(DateRange_.gueltigAb), datumBis);
		predicatesToUse.add(predicateStart);
		Predicate predicateEnd = builder.greaterThanOrEqualTo(root.get(VerfuegungZeitabschnitt_.gueltigkeit).get(DateRange_.gueltigBis), datumVon);
		predicatesToUse.add(predicateEnd);

		if (gesuchsperiode != null) {
			Predicate predicateGesuchsperiode = builder.equal(root.get(VerfuegungZeitabschnitt_.verfuegung).get(Verfuegung_.betreuung).get(Betreuung_.kind).get(KindContainer_.gesuch).get(Gesuch_.gesuchsperiode), gesuchsperiode);
			predicatesToUse.add(predicateGesuchsperiode);
		}

		// nur das neuest verfuegte Gesuch
		Collection<Gesuchsperiode> relevanteGesuchsperioden = gesuchsperiodeService.getGesuchsperiodenBetween(datumVon, datumBis);
		if (!relevanteGesuchsperioden.isEmpty()) {
			List<String> idsOfLetztVerfuegteAntraege = new ArrayList<>();
			for (Gesuchsperiode gp : relevanteGesuchsperioden) {
				idsOfLetztVerfuegteAntraege.addAll(gesuchService.getNeuesteVerfuegteAntraege(gp));
			}
			if (!idsOfLetztVerfuegteAntraege.isEmpty()) {
				Predicate predicateAktuellesGesuch = root.get(VerfuegungZeitabschnitt_.verfuegung).get(Verfuegung_.betreuung).get(Betreuung_.kind).get(KindContainer_.gesuch).get(Gesuch_.id).in(idsOfLetztVerfuegteAntraege);
				predicatesToUse.add(predicateAktuellesGesuch);
			} else {
				return Collections.emptyList();
			}
		} else {
			return Collections.emptyList();
		}

		// Sichtbarkeit nach eingeloggtem Benutzer
		boolean isInstitutionsbenutzer = principalBean.isCallerInAnyOfRole(UserRole.SACHBEARBEITER_INSTITUTION, UserRole.SACHBEARBEITER_TRAEGERSCHAFT);
		if (isInstitutionsbenutzer) {
			Collection<Institution> allowedInstitutionen = institutionService.getAllowedInstitutionenForCurrentBenutzer();
			Predicate predicateAllowedInstitutionen = root.get(VerfuegungZeitabschnitt_.verfuegung).get(Verfuegung_.betreuung).get(Betreuung_.institutionStammdaten).get(InstitutionStammdaten_.institution).in(allowedInstitutionen);
			predicatesToUse.add(predicateAllowedInstitutionen);
		}
		boolean isSchulamtBenutzer = principalBean.isCallerInAnyOfRole(UserRole.SCHULAMT);
		if (isSchulamtBenutzer) {
			Predicate predicateSchulamt = builder.equal(root.get(VerfuegungZeitabschnitt_.verfuegung).get(Verfuegung_.betreuung).get(Betreuung_.institutionStammdaten).get(InstitutionStammdaten_.betreuungsangebotTyp) , BetreuungsangebotTyp.TAGESSCHULE);
			predicatesToUse.add(predicateSchulamt);
		} else {
			Predicate predicateNotSchulamt = builder.notEqual(root.get(VerfuegungZeitabschnitt_.verfuegung).get(Verfuegung_.betreuung).get(Betreuung_.institutionStammdaten).get(InstitutionStammdaten_.betreuungsangebotTyp) , BetreuungsangebotTyp.TAGESSCHULE);
			predicatesToUse.add(predicateNotSchulamt);
		}

		query.where(CriteriaQueryHelper.concatenateExpressions(builder, predicatesToUse));
		List<VerfuegungZeitabschnitt> zeitabschnittList = persistence.getCriteriaResults(query);
		return convertToGesuchstellerKinderBetreuungDataRow(zeitabschnittList);
	}

	private List<GesuchstellerKinderBetreuungDataRow> convertToGesuchstellerKinderBetreuungDataRow(List<VerfuegungZeitabschnitt> zeitabschnittList) {
		List<GesuchstellerKinderBetreuungDataRow> dataRowList = new ArrayList<>();
		for (VerfuegungZeitabschnitt zeitabschnitt : zeitabschnittList) {
			Gesuch gesuch = zeitabschnitt.getVerfuegung().getBetreuung().extractGesuch();

			GesuchstellerKinderBetreuungDataRow row = new GesuchstellerKinderBetreuungDataRow();
			row.setInstitution(zeitabschnitt.getVerfuegung().getBetreuung().getInstitutionStammdaten().getInstitution().getName());
			row.setBetreuungsTyp(zeitabschnitt.getVerfuegung().getBetreuung().getBetreuungsangebotTyp());
			row.setPeriode(gesuch.getGesuchsperiode().getGesuchsperiodeString());
			row.setEingangsdatum(gesuch.getEingangsdatum());
			for (AntragStatusHistory antragStatusHistory : gesuch.getAntragStatusHistories()) {
				if (AntragStatus.VERFUEGT.equals(antragStatusHistory.getStatus()) ||
					AntragStatus.NUR_SCHULAMT.equals(antragStatusHistory.getStatus())) {
					row.setVerfuegungsdatum(antragStatusHistory.getTimestampVon().toLocalDate());
				}
			}
			row.setFallId(Integer.parseInt(""+gesuch.getFall().getFallNummer()));
			row.setBgNummer(zeitabschnitt.getVerfuegung().getBetreuung().getBGNummer());

			// Gesuchsteller 1
			addGesuchsteller1ToGesuchstellerKinderBetreuungDataRow(row, gesuch.getGesuchsteller1());
			// Gesuchsteller 2
			if (gesuch.getGesuchsteller2() != null) {
				addGesuchsteller2ToGesuchstellerKinderBetreuungDataRow(row, gesuch.getGesuchsteller2());
			}
			// Familiensituation / Einkommen
			row.setFamiliensituation(gesuch.getFamiliensituationContainer().extractFamiliensituation().getFamilienstatus());
			row.setKardinalitaet(gesuch.getFamiliensituationContainer().extractFamiliensituation().getGesuchstellerKardinalitaet());
			row.setFamiliengroesse(zeitabschnitt.getFamGroesse());
			row.setMassgEinkVorFamilienabzug(zeitabschnitt.getMassgebendesEinkommenVorAbzFamgr());
			row.setFamilienabzug(zeitabschnitt.getAbzugFamGroesse());
			row.setMassgEink(zeitabschnitt.getMassgebendesEinkommen());
			row.setEinkommensjahr(zeitabschnitt.getEinkommensjahr());
			if (gesuch.getEinkommensverschlechterungInfoContainer() != null) {
				row.setEkvVorhanden(gesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().getEinkommensverschlechterung());
			}
			row.setStvGeprueft(Boolean.FALSE); //TODO (team) haben wir noch nicht
			row.setVeranlagt(gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().getSteuerveranlagungErhalten());
			// Kind
			addKindToGesuchstellerKinderBetreuungDataRow(row, zeitabschnitt);
			// Betreuung
			addBetreuungToGesuchstellerKinderBetreuungDataRow(row, zeitabschnitt);

			dataRowList.add(row);
		}
		return dataRowList;
	}

	private void addGesuchsteller1ToGesuchstellerKinderBetreuungDataRow(GesuchstellerKinderBetreuungDataRow row, GesuchstellerContainer containerGS1) {
		Gesuchsteller gs1 = containerGS1.getGesuchstellerJA();
		row.setGs1Name(gs1.getNachname());
		row.setGs1Vorname(gs1.getVorname());
		GesuchstellerAdresse gs1Adresse = gesuchstellerAdresseService.getCurrentWohnadresse(containerGS1.getId()).getGesuchstellerAdresseJA();
		row.setGs1Strasse(gs1Adresse.getStrasse());
		row.setGs1Hausnummer(gs1Adresse.getHausnummer());
		row.setGs1Zusatzzeile(gs1Adresse.getZusatzzeile());
		row.setGs1Plz(gs1Adresse.getPlz());
		row.setGs1Ort(gs1Adresse.getOrt());
		row.setGs1EwkId(gs1.getZpvNumber());
		row.setGs1Diplomatenstatus(gs1.isDiplomatenstatus());
		// EWP Gesuchsteller 1
		Set<ErwerbspensumContainer> erwerbspensenGS1 = containerGS1.getErwerbspensenContainersNotEmpty();
		for (ErwerbspensumContainer erwerbspensumContainer : erwerbspensenGS1) {
			Erwerbspensum erwerbspensumJA = erwerbspensumContainer.getErwerbspensumJA();
			if (Taetigkeit.ANGESTELLT.equals(erwerbspensumJA.getTaetigkeit())) {
				row.setGs1EwpAngestellt(row.getGs1EwpAngestellt() != null ? row.getGs1EwpAngestellt() + erwerbspensumJA.getPensum() : erwerbspensumJA.getPensum());
			}
			if (Taetigkeit.AUSBILDUNG.equals(erwerbspensumJA.getTaetigkeit())) {
				row.setGs1EwpAusbildung(row.getGs1EwpAusbildung() != null ? row.getGs1EwpAusbildung() + erwerbspensumJA.getPensum() : erwerbspensumJA.getPensum());
			}
			if (Taetigkeit.SELBSTAENDIG.equals(erwerbspensumJA.getTaetigkeit())) {
				row.setGs1EwpSelbstaendig(row.getGs1EwpSelbstaendig() != null ? row.getGs1EwpSelbstaendig() + erwerbspensumJA.getPensum() : erwerbspensumJA.getPensum());
			}
			if (Taetigkeit.RAV.equals(erwerbspensumJA.getTaetigkeit())) {
				row.setGs1EwpRav(row.getGs1EwpRav() != null ? row.getGs1EwpRav() + erwerbspensumJA.getPensum() : erwerbspensumJA.getPensum());
			}
			if (Taetigkeit.GESUNDHEITLICHE_EINSCHRAENKUNGEN.equals(erwerbspensumJA.getTaetigkeit())) {
				row.setGs1EwpGesundhtl(row.getGs1EwpGesundhtl() != null ? row.getGs1EwpGesundhtl() + erwerbspensumJA.getPensum() : erwerbspensumJA.getPensum());
			}
			if (erwerbspensumJA.getZuschlagZuErwerbspensum()) {
				row.setGs1EwpZuschlag(row.getGs1EwpZuschlag() != null ? row.getGs1EwpZuschlag() + erwerbspensumJA.getPensum() : erwerbspensumJA.getPensum());
			}
		}
	}

	private void addGesuchsteller2ToGesuchstellerKinderBetreuungDataRow(GesuchstellerKinderBetreuungDataRow row, GesuchstellerContainer containerGS2) {
		Gesuchsteller gs2 = containerGS2.getGesuchstellerJA();
		row.setGs2Name(gs2.getNachname());
		row.setGs2Vorname(gs2.getVorname());
		GesuchstellerAdresse gs2Adresse = gesuchstellerAdresseService.getCurrentWohnadresse(containerGS2.getId()).getGesuchstellerAdresseJA();
		row.setGs2Strasse(gs2Adresse.getStrasse());
		row.setGs2Hausnummer(gs2Adresse.getHausnummer());
		row.setGs2Zusatzzeile(gs2Adresse.getZusatzzeile());
		row.setGs2Plz(gs2Adresse.getPlz());
		row.setGs2Ort(gs2Adresse.getOrt());
		row.setGs2EwkId(gs2.getZpvNumber());
		row.setGs2Diplomatenstatus(gs2.isDiplomatenstatus());
		// EWP Gesuchsteller 2
		Set<ErwerbspensumContainer> erwerbspensenGS2 = containerGS2.getErwerbspensenContainersNotEmpty();
		for (ErwerbspensumContainer erwerbspensumContainer : erwerbspensenGS2) {
			Erwerbspensum erwerbspensumJA = erwerbspensumContainer.getErwerbspensumJA();
			if (Taetigkeit.ANGESTELLT.equals(erwerbspensumJA.getTaetigkeit())) {
				row.setGs2EwpAngestellt(row.getGs2EwpAngestellt() != null ? row.getGs2EwpAngestellt() + erwerbspensumJA.getPensum() : erwerbspensumJA.getPensum());
			}
			if (Taetigkeit.AUSBILDUNG.equals(erwerbspensumJA.getTaetigkeit())) {
				row.setGs2EwpAusbildung(row.getGs2EwpAusbildung() != null ? row.getGs2EwpAusbildung() + erwerbspensumJA.getPensum() : erwerbspensumJA.getPensum());
			}
			if (Taetigkeit.SELBSTAENDIG.equals(erwerbspensumJA.getTaetigkeit())) {
				row.setGs2EwpSelbstaendig(row.getGs2EwpSelbstaendig() != null ? row.getGs2EwpSelbstaendig() + erwerbspensumJA.getPensum() : erwerbspensumJA.getPensum());
			}
			if (Taetigkeit.RAV.equals(erwerbspensumJA.getTaetigkeit())) {
				row.setGs2EwpRav(row.getGs2EwpRav() != null ? row.getGs2EwpRav() + erwerbspensumJA.getPensum() : erwerbspensumJA.getPensum());
			}
			if (Taetigkeit.GESUNDHEITLICHE_EINSCHRAENKUNGEN.equals(erwerbspensumJA.getTaetigkeit())) {
				row.setGs2EwpGesundhtl(row.getGs2EwpGesundhtl() != null ? row.getGs2EwpGesundhtl() + erwerbspensumJA.getPensum() : erwerbspensumJA.getPensum());
			}
			if (erwerbspensumJA.getZuschlagZuErwerbspensum()) {
				row.setGs2EwpZuschlag(row.getGs2EwpZuschlag() != null ? row.getGs2EwpZuschlag() + erwerbspensumJA.getPensum() : erwerbspensumJA.getPensum());
			}
		}
	}

	private void addKindToGesuchstellerKinderBetreuungDataRow(GesuchstellerKinderBetreuungDataRow row, VerfuegungZeitabschnitt zeitabschnitt) {
		Kind kind = zeitabschnitt.getVerfuegung().getBetreuung().getKind().getKindJA();
		row.setKindName(kind.getNachname());
		row.setKindVorname(kind.getVorname());
		row.setKindGeburtsdatum(kind.getGeburtsdatum());
		row.setKindFachstelle(kind.getPensumFachstelle() != null);
		row.setKindErwBeduerfnisse(zeitabschnitt.getVerfuegung().getBetreuung().getErweiterteBeduerfnisse());
		row.setKindDeutsch(kind.getMutterspracheDeutsch());
	}

	private void addBetreuungToGesuchstellerKinderBetreuungDataRow(GesuchstellerKinderBetreuungDataRow row, VerfuegungZeitabschnitt zeitabschnitt) {
		row.setZeitabschnittVon(zeitabschnitt.getGueltigkeit().getGueltigAb());
		row.setZeitabschnittBis(zeitabschnitt.getGueltigkeit().getGueltigBis());
		row.setBetreuungsPensum(MathUtil.DEFAULT.from(zeitabschnitt.getBetreuungspensum()));
		row.setAnspruchsPensum(MathUtil.DEFAULT.from(zeitabschnitt.getAnspruchberechtigtesPensum()));
		row.setBgPensum(MathUtil.DEFAULT.from(zeitabschnitt.getBgPensum()));
		row.setBgStunden(zeitabschnitt.getBetreuungsstunden());
		row.setVollkosten(zeitabschnitt.getVollkosten());
		row.setElternbeitrag(zeitabschnitt.getElternbeitrag());
		row.setVerguenstigt(zeitabschnitt.getVerguenstigung());
	}

	@Override
	@RolesAllowed({SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, REVISOR, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, SCHULAMT})
	public UploadFileInfo generateExcelReportGesuchstellerKinderBetreuung(@Nonnull LocalDate datumVon, @Nonnull LocalDate datumBis, @Nullable String gesuchPeriodeId) throws ExcelMergeException, IOException, MergeDocException, URISyntaxException {
		Validate.notNull(datumVon, VALIDIERUNG_DATUM_VON);
		Validate.notNull(datumBis, VALIDIERUNG_DATUM_BIS);

		final ReportResource reportResource = VORLAGE_REPORT_GESUCHSTELLER_KINDER_BETREUUNG;

		InputStream is = ReportServiceBean.class.getResourceAsStream(reportResource.getTemplatePath());
		Validate.notNull(is, VORLAGE + reportResource.getTemplatePath() + "' nicht gefunden");

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportResource.getDataSheetName());

		Gesuchsperiode gesuchsperiode = null;
		if (gesuchPeriodeId != null) {
			Optional<Gesuchsperiode> gesuchsperiodeOptional = gesuchsperiodeService.findGesuchsperiode(gesuchPeriodeId);
			if (gesuchsperiodeOptional.isPresent()) {
				gesuchsperiode = gesuchsperiodeOptional.get();
			}
		}

		List<GesuchstellerKinderBetreuungDataRow> reportData = getReportDataGesuchstellerKinderBetreuung(datumVon, datumBis, gesuchsperiode);
		ExcelMergerDTO excelMergerDTO = gesuchstellerKinderBetreuungExcelConverter.toExcelMergerDTO(reportData, Locale.getDefault(), datumVon, datumBis, gesuchsperiode);

		mergeData(sheet, excelMergerDTO, reportResource.getMergeFields());
		gesuchstellerKinderBetreuungExcelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(bytes,
			reportResource.getDefaultExportFilename(),
			TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	public enum ReportResource {

		// TODO Achtung mit Filename, da mehrere Dokumente mit gleichem Namen aber unterschiedlichem Inhalt gespeichert werden. Falls der Name geaendert wuerde, muesste das File wieder geloescht werden.
		VORLAGE_REPORT_GESUCH_STICHTAG("/reporting/GesuchStichtag.xlsx", "GesuchStichtag.xlsx", DATA,
			MergeFieldGesuchStichtag.class),
		VORLAGE_REPORT_GESUCH_ZEITRAUM("/reporting/GesuchZeitraum.xlsx", "GesuchZeitraum.xlsx", DATA,
			MergeFieldGesuchZeitraum.class),
		VORLAGE_REPORT_KANTON("/reporting/Kanton.xlsx", "Kanton.xlsx", DATA,
			MergeFieldKanton.class),
		VORLAGE_REPORT_ZAHLUNG_AUFTRAG("/reporting/ZahlungAuftrag.xlsx", "ZahlungAuftrag.xlsx", DATA,
			MergeFieldZahlungAuftrag.class),
		VORLAGE_REPORT_ZAHLUNG_AUFTRAG_PERIODE("/reporting/ZahlungAuftragPeriode.xlsx", "ZahlungAuftragPeriode.xlsx", DATA,
			MergeFieldZahlungAuftragPeriode.class),
		VORLAGE_REPORT_GESUCHSTELLER_KINDER_BETREUUNG("/reporting/GesuchstellerKinderBetreuung.xlsx", "GesuchstellerKinderBetreuung.xlsx", DATA,
			MergeFieldGesuchstellerKinderBetreuung.class);

		@Nonnull
		private final String templatePath;
		@Nonnull
		private final String defaultExportFilename;
		@Nonnull
		private final Class<? extends MergeField> mergeFields;
		@Nonnull
		private final String dataSheetName;

		ReportResource(@Nonnull String templatePath, @Nonnull String defaultExportFilename,
					   @Nonnull String dataSheetName, @Nonnull Class<? extends MergeField> mergeFields) {
			this.templatePath = templatePath;
			this.defaultExportFilename = defaultExportFilename;
			this.mergeFields = mergeFields;
			this.dataSheetName = dataSheetName;
		}

		@Nonnull
		public String getTemplatePath() {
			return templatePath;
		}

		@Nonnull
		public String getDefaultExportFilename() {
			return defaultExportFilename;
		}

		@Nonnull
		public MergeField[] getMergeFields() {
			return mergeFields.getEnumConstants();
		}

		@Nonnull
		public String getDataSheetName() {
			return dataSheetName;
		}
	}

	private void runStatisticsBetreuung() {
		List<Betreuung> allBetreuungen = betreuungService.getAllBetreuungenWithMissingStatistics();
		for (Betreuung betreuung : allBetreuungen) {
			if (betreuung.hasVorgaenger()) {
				Betreuung vorgaengerBetreuung = persistence.find(Betreuung.class, betreuung.getVorgaengerId());
				if (!betreuung.isSame(vorgaengerBetreuung, false, false)) {
					betreuung.setBetreuungMutiert(Boolean.TRUE);
					LOGGER.info("Betreuung hat geändert: " + betreuung.getId());
				} else {
					betreuung.setBetreuungMutiert(Boolean.FALSE);
					LOGGER.info("Betreuung hat nicht geändert: " + betreuung.getId());
				}
			} else {
				// Betreuung war auf dieser Mutation neu
				LOGGER.info("Betreuung ist neu: " + betreuung.getId());
				betreuung.setBetreuungMutiert(Boolean.TRUE);
			}
		}
	}

	private void runStatisticsAbwesenheiten() {
		List<Abwesenheit> allAbwesenheiten = betreuungService.getAllAbwesenheitenWithMissingStatistics();
		for (Abwesenheit abwesenheit : allAbwesenheiten) {
			Betreuung betreuung = abwesenheit.getAbwesenheitContainer().getBetreuung();
			if (abwesenheit.hasVorgaenger()) {
				Abwesenheit vorgaengerAbwesenheit = persistence.find(Abwesenheit.class, abwesenheit.getVorgaengerId());
				if (!abwesenheit.isSame(vorgaengerAbwesenheit)) {
					betreuung.setAbwesenheitMutiert(Boolean.TRUE);
					LOGGER.info("Abwesenheit hat geändert: " + abwesenheit.getId());
				} else {
					betreuung.setAbwesenheitMutiert(Boolean.FALSE);
					LOGGER.info("Abwesenheit hat nicht geändert: " + abwesenheit.getId());
				}
			} else {
				// Abwesenheit war auf dieser Mutation neu
				LOGGER.info("Abwesenheit ist neu: " + abwesenheit.getId());
				betreuung.setAbwesenheitMutiert(Boolean.TRUE);
			}
		}
	}

	private void runStatisticsKinder() {
		List<KindContainer> allKindContainer = kindService.getAllKinderWithMissingStatistics();
		for (KindContainer kindContainer : allKindContainer) {
			Kind kind = kindContainer.getKindJA();
			if (kind.hasVorgaenger()) {
				Kind vorgaengerKind = persistence.find(Kind.class, kind.getVorgaengerId());
				if (!kind.isSame(vorgaengerKind)) {
					kindContainer.setKindMutiert(Boolean.TRUE);
					LOGGER.info("Kind hat geändert: " + kindContainer.getId());
				} else {
					kindContainer.setKindMutiert(Boolean.FALSE);
					LOGGER.info("Kind hat nicht geändert: " + kindContainer.getId());
				}
			} else {
				// Kind war auf dieser Mutation neu
				LOGGER.info("Kind ist neu: " + kindContainer.getId());
				kindContainer.setKindMutiert(Boolean.TRUE);
			}
		}
	}
}
