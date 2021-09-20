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

package ch.dvbern.ebegu.inbox.handler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.AbstractDecimalPensum;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.ErweiterteBetreuung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionExternalClient;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.MitteilungStatus;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.inbox.services.BetreuungEventHelper;
import ch.dvbern.ebegu.kafka.BaseEventHandler;
import ch.dvbern.ebegu.kafka.EventType;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.MitteilungService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.Gueltigkeit;
import ch.dvbern.ebegu.util.GueltigkeitsUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;
import ch.dvbern.kibon.exchange.commons.types.Zeiteinheit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED;
import static ch.dvbern.ebegu.util.EbeguUtil.collectionComparator;

@ApplicationScoped
public class PlatzbestaetigungEventHandler extends BaseEventHandler<BetreuungEventDTO> {

	private static final Logger LOG = LoggerFactory.getLogger(PlatzbestaetigungEventHandler.class);

	private static final String BETREFF_KEY = "mutationsmeldung_betreff_von";
	private static final String MESSAGE_KEY = "mutationsmeldung_message";
	private static final String MESSAGE_MAHLZEIT_KEY = "mutationsmeldung_message_mahlzeitverguenstigung_mit_tarif";
	static final LocalDate GO_LIVE = LocalDate.of(2021, 1, 1);

	static final Comparator<AbstractMahlzeitenPensum> COMPARATOR = Comparator
		.comparing(AbstractMahlzeitenPensum::getMonatlicheBetreuungskosten)
		.thenComparing(AbstractDecimalPensum::getPensumRounded)
		.thenComparing(AbstractMahlzeitenPensum::getTarifProHauptmahlzeit)
		.thenComparing(AbstractMahlzeitenPensum::getTarifProNebenmahlzeit)
		.thenComparing(AbstractMahlzeitenPensum::getMonatlicheHauptmahlzeiten)
		.thenComparing(AbstractMahlzeitenPensum::getMonatlicheNebenmahlzeiten);

	private static final Comparator<AbstractMahlzeitenPensum> COMPARATOR_WITH_GUELTIGKEIT = COMPARATOR
		.thenComparing(AbstractMahlzeitenPensum::getGueltigkeit);

	private static final Comparator<Betreuungsmitteilung> MITTEILUNG_COMPARATOR = Comparator
		.comparing(Betreuungsmitteilung::getBetreuungspensen, collectionComparator(COMPARATOR_WITH_GUELTIGKEIT));

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private MitteilungService mitteilungService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private BetreuungEventHelper betreuungEventHelper;

	@Inject
	private BetreuungMonitoringService betreuungMonitoringService;

	@Override
	protected void processEvent(
		@Nonnull LocalDateTime eventTime,
		@Nonnull EventType eventType,
		@Nonnull String key,
		@Nonnull BetreuungEventDTO dto,
		@Nonnull String clientName) {

		String refnr = dto.getRefnr();
		EventMonitor eventMonitor = new EventMonitor(betreuungMonitoringService, eventTime, refnr, clientName);
		Processing processing = attemptProcessing(eventMonitor, dto);

		if (!processing.isProcessingSuccess()) {
			String message = processing.getMessage();
			LOG.warn(
				"Platzbestaetigung Event für Betreuung mit RefNr: {} nicht verarbeitet: {}",
				refnr,
				message);
			eventMonitor.record("Eine Platzbestaetigung Event wurde nicht verarbeitet: " + message);
		}
	}

	@Nonnull
	protected Processing attemptProcessing(@Nonnull EventMonitor eventMonitor, @Nonnull BetreuungEventDTO dto) {

		if (dto.getZeitabschnitte().isEmpty()) {
			return Processing.failure("Es wurden keine Zeitabschnitte übergeben.");
		}

		return betreuungService.findBetreuungByBGNummer(dto.getRefnr(), false)
			.map(betreuung -> processEventForBetreuung(eventMonitor, dto, betreuung))
			.orElseGet(() -> Processing.failure("Betreuung nicht gefunden."));
	}

	@Nonnull
	private Processing processEventForBetreuung(
		@Nonnull EventMonitor eventMonitor,
		@Nonnull BetreuungEventDTO dto,
		@Nonnull Betreuung betreuung) {

		if (betreuung.extractGesuchsperiode().getStatus() != GesuchsperiodeStatus.AKTIV) {
			return Processing.failure("Die Gesuchsperiode ist nicht aktiv.");
		}

		if (eventMonitor.isTooLate(betreuung.getTimestampMutiert())) {
			return Processing.failure("Die Betreuung wurde verändert, nachdem das BetreuungEvent generiert wurde.");
		}

		if (hasZeitabschnittWithPensumUnit(dto, Zeiteinheit.HOURS) && !betreuung.isAngebotTagesfamilien()) {
			return Processing.failure("Eine Pensum in HOURS kann nur für ein Angebot in einer TFO angegeben werden.");
		}

		if (hasZeitabschnittWithPensumUnit(dto, Zeiteinheit.DAYS) && !betreuung.isAngebotKita()) {
			return Processing.failure("Eine Pensum in DAYS kann nur für ein Angebot in einer Kita angegeben werden.");
		}

		return betreuungEventHelper.getExternalClient(eventMonitor.getClientName(), betreuung)
			.map(client -> processEventForExternalClient(eventMonitor, dto, betreuung, client))
			.orElseGet(() -> betreuungEventHelper.clientNotFoundFailure(eventMonitor.getClientName(), betreuung));
	}

	private boolean hasZeitabschnittWithPensumUnit(@Nonnull BetreuungEventDTO dto, @Nonnull Zeiteinheit zeiteinheit) {
		return dto.getZeitabschnitte().stream().anyMatch(z -> z.getPensumUnit() == zeiteinheit);
	}

	@Nonnull
	private Processing processEventForExternalClient(
		@Nonnull EventMonitor eventMonitor,
		@Nonnull BetreuungEventDTO dto,
		@Nonnull Betreuung betreuung,
		@Nonnull InstitutionExternalClient client) {

		DateRange gesuchsperiode = betreuung.extractGesuchsperiode().getGueltigkeit();
		DateRange clientGueltigkeit = client.getGueltigkeit();
		Optional<DateRange> overlap = gesuchsperiode.getOverlap(clientGueltigkeit);
		if (overlap.isEmpty()) {
			return Processing.failure("Der Client hat innerhalb der Periode keine Berechtigung.");
		}

		DateRange institutionGueltigkeit = betreuung.getInstitutionStammdaten().getGueltigkeit();
		Processing validationProcess = validateZeitabschnitteGueltigkeit(dto, overlap.get(), institutionGueltigkeit);
		if (!validationProcess.isProcessingSuccess()) {
			return validationProcess;
		}

		boolean mahlzeitVergunstigungEnabled = isEnabled(betreuung, GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED);
		ProcessingContext ctx =
			new ProcessingContext(betreuung, dto, overlap.get(), mahlzeitVergunstigungEnabled, eventMonitor);

		Betreuungsstatus status = betreuung.getBetreuungsstatus();

		if (isPlatzbestaetigungStatus(status, betreuung.extractGesuch().getStatus())) {
			return handlePlatzbestaetigung(ctx);
		}

		if (isMutationsMitteilungStatus(status)) {
			return handleMutationsMitteilung(ctx);
		}

		return Processing.failure("Die Betreuung hat einen ungültigen Status: " + status);
	}

	@Nonnull
	private Processing validateZeitabschnitteGueltigkeit(
		@Nonnull BetreuungEventDTO dto,
		@Nonnull DateRange clientGueltigkeitInPeriode,
		@Nonnull DateRange institutionGueltigkeit) {

		List<DateRange> ranges = dto.getZeitabschnitte().stream()
			.map(z -> new DateRange(z.getVon(), z.getBis()))
			.collect(Collectors.toList());

		if (GueltigkeitsUtil.hasOverlap(ranges)) {
			return Processing.failure("Zeitabschnitte dürfen nicht überlappen.");
		}

		Optional<DateRange> writableOverlap = clientGueltigkeitInPeriode.getOverlap(institutionGueltigkeit);
		if (writableOverlap.isEmpty()) {
			return Processing.failure("Die Institution Gültigkeit überlappt nicht mit der Client Gültigkeit.");
		}

		if (ranges.stream().noneMatch(r -> r.intersects(writableOverlap.get()))) {
			return Processing.failure(
				"Kein Zeitabschnitt liegt innerhalb Client Gültigkeit & Periode & Institution Gültigkeit.");
		}

		return Processing.success();
	}

	private boolean isPlatzbestaetigungStatus(
		@Nonnull Betreuungsstatus status,
		@Nonnull AntragStatus antragStatus) {
		if (status == Betreuungsstatus.WARTEN) {
			return true;
		}

		return status == Betreuungsstatus.BESTAETIGT && isNotYetFreigegeben(antragStatus);
	}

	private boolean isNotYetFreigegeben(@Nonnull AntragStatus antragStatus) {
		return antragStatus == AntragStatus.IN_BEARBEITUNG_GS
			|| antragStatus == AntragStatus.IN_BEARBEITUNG_SOZIALDIENST;
	}

	protected boolean isMutationsMitteilungStatus(@Nonnull Betreuungsstatus status) {
		return status == Betreuungsstatus.VERFUEGT
			|| status == Betreuungsstatus.BESTAETIGT
			|| status == Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG
			|| status == Betreuungsstatus.STORNIERT;
	}

	/**
	 * Update the Betreuung and automatically confirm if all required data is present.
	 */
	@Nonnull
	private Processing handlePlatzbestaetigung(@Nonnull ProcessingContext ctx) {
		boolean isReadyForBestaetigen = updateBetreuungForPlatzbestaetigung(ctx);
		String clientName = ctx.getEventMonitor().getClientName();
		String refnr = ctx.getDto().getRefnr();

		if (isReadyForBestaetigen) {
			ctx.getBetreuung().setDatumBestaetigung(LocalDate.now());
			//noinspection ResultOfMethodCallIgnored

			betreuungService.betreuungPlatzBestaetigen(ctx.getBetreuung(), clientName);
			LOG.info("PlatzbestaetigungEvent Betreuung mit RefNr: {} automatisch bestätigt", refnr);
			ctx.getEventMonitor().record("PlatzbestaetigungEvent automatisch bestätigt");
		} else {
			//noinspection ResultOfMethodCallIgnored
			betreuungService.saveBetreuung(ctx.getBetreuung(), false, clientName);
			LOG.info(
				"PlatzbestaetigungEvent Betreuung mit RefNr: {} eingelesen, aber nicht automatisch bestätigt",
				refnr);
			ctx.getEventMonitor().record(
				"PlatzbestaetigungEvent eingelesen, aber nicht automatisch bestätigt: %s",
				ctx.getHumanConfirmationMessage());
		}

		return Processing.success();
	}

	/**
	 * Update the Betreuung Object and return if its ready for Bestaetigen
	 */
	private boolean updateBetreuungForPlatzbestaetigung(@Nonnull ProcessingContext ctx) {

		if (!ctx.isGueltigkeitCoveringPeriode()) {
			ctx.requireHumanConfirmation();
			LOG.info(
				"PlatzbestaetigungEvent fuer Betreuung mit RefNr: {} hat Zeitabschnitte die ausserhalb der "
					+ "Schnittstellen- oder Gesuchsperiode-Gültigkeit liegen",
				ctx.getDto().getRefnr());
			ctx.setHumanConfirmationMessage(
				"PlatzbestaetigungEvent hat Zeitabschnitte die ausserhalb der Schnittstellen- oder "
					+ "Gesuchsperiode-Gültigkeit liegen");
		}

		setErweitereBeduerfnisseBestaetigt(ctx);
		setBetreuungInGemeinde(ctx);
		PensumMappingUtil.addZeitabschnitteToBetreuung(ctx);

		return ctx.isReadyForBestaetigen();
	}

	private void setErweitereBeduerfnisseBestaetigt(@Nonnull ProcessingContext ctx) {
		ErweiterteBetreuung eb = ctx.getBetreuung().getErweiterteBetreuungContainer().getErweiterteBetreuungJA();
		// Der Wert aus dem DTO wird nur berücksichtigt, wenn bereits bei dem Antrag erweitere Bedürfnisse angemeldet
		// wurden.
		if (eb != null) {
			Boolean claimed = eb.getErweiterteBeduerfnisse();
			boolean confirmed = ctx.getDto().getAusserordentlicherBetreuungsaufwand();

			eb.setErweiterteBeduerfnisseBestaetigt(claimed == confirmed);
		}
	}

	private void setBetreuungInGemeinde(@Nonnull ProcessingContext ctx) {
		Betreuung betreuung = ctx.getBetreuung();
		boolean enabled = isEnabled(betreuung, EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED);

		if (!enabled) {
			// no need to set BetreuungInGemeinde -> continue automated processing
			return;
		}

		String incomingName = ctx.getDto().getGemeindeName();
		Long incomingBfsNumber = ctx.getDto().getGemeindeBfsNr();

		if (incomingName == null && incomingBfsNumber == null) {
			// Gemeinde not received, cannot evaluate -> abort automated processing
			ctx.requireHumanConfirmation();
			LOG.info(
				"PlatzbestaetigungEvent fuer Betreuung mit RefNr: {} hat keine Gemeinde spezifiziert",
				ctx.getDto().getRefnr());
			ctx.setHumanConfirmationMessage("PlatzbestaetigungEvent hat keine Gemeinde spezifiziert");
			return;
		}

		Gemeinde gemeinde = betreuung.extractGemeinde();
		Long bfsNummer = gemeinde.getBfsNummer();
		String name = gemeinde.getName();

		getOrCreateErweiterteBetreuung(betreuung)
			.setBetreuungInGemeinde(bfsNummer.equals(incomingBfsNumber) || name.equalsIgnoreCase(incomingName));
	}

	@Nonnull
	private Processing handleMutationsMitteilung(@Nonnull ProcessingContext ctx) {
		Betreuung betreuung = ctx.getBetreuung();

		Collection<Betreuungsmitteilung> open =
			mitteilungService.findOffeneBetreuungsmitteilungenForBetreuung(betreuung);

		Optional<Betreuungsmitteilung> latest = findLatest(open);

		Betreuungsmitteilung betreuungsmitteilung = createBetreuungsmitteilung(ctx, latest.orElse(null));

		if (latest.filter(l -> MITTEILUNG_COMPARATOR.compare(l, betreuungsmitteilung) == 0).isPresent()) {
			return Processing.failure(
				"Die Betreuungsmeldung ist identisch mit der neusten offenen Betreuungsmeldung.");
		}

		if (latest.isEmpty() && isSame(betreuungsmitteilung, betreuung)) {
			return Processing.failure("Die Betreuungsmeldung und die Betreuung sind identisch.");
		}

		mitteilungService.replaceBetreungsmitteilungen(betreuungsmitteilung);
		LOG.info(
			"PlatzbestaetigungEvent: Mutationsmeldung erstellt für die Betreuung mit RefNr: {}",
			ctx.getDto().getRefnr());
		ctx.getEventMonitor().record("PlatzbestaetigungEvent: Mutationsmeldung erstellt");

		return Processing.success();
	}

	@Nonnull
	private Betreuungsmitteilung createBetreuungsmitteilung(
		@Nonnull ProcessingContext ctx,
		@Nullable Betreuungsmitteilung latest) {

		Betreuung betreuung = ctx.getBetreuung();
		Gesuch gesuch = betreuung.extractGesuch();
		Locale locale = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService).getLocale();

		Betreuungsmitteilung betreuungsmitteilung = new Betreuungsmitteilung();
		betreuungsmitteilung.setDossier(gesuch.getDossier());
		betreuungsmitteilung.setSenderTyp(MitteilungTeilnehmerTyp.INSTITUTION);
		betreuungsmitteilung.setSender(betreuungEventHelper.getMutationsmeldungBenutzer());
		betreuungsmitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
		betreuungsmitteilung.setEmpfaenger(gesuch.getDossier().getFall().getBesitzer());
		betreuungsmitteilung.setMitteilungStatus(MitteilungStatus.NEU);
		betreuungsmitteilung.setSubject(ServerMessageUtil.getMessage(BETREFF_KEY, locale)
			+ ' '
			+ ctx.getEventMonitor().getClientName());
		betreuungsmitteilung.setBetreuung(betreuung);

		PensumMappingUtil.addZeitabschnitteToBetreuungsmitteilung(ctx, latest, betreuungsmitteilung);

		betreuungsmitteilung.getBetreuungspensen().stream()
			.filter(p -> betreuung.getBetreuungsstatus() == Betreuungsstatus.STORNIERT && p.getPensum().compareTo(
				BigDecimal.ZERO) == 0)
			.forEach(p -> p.setVollstaendig(false));

		BiFunction<BetreuungsmitteilungPensum, Integer, String> messageMapper = ctx.isMahlzeitVerguenstigungEnabled() ?
			mahlzeitenMessage(locale) :
			defaultMessage(locale);

		betreuungsmitteilung.setMessage(getMessage(messageMapper, betreuungsmitteilung.getBetreuungspensen()));

		return betreuungsmitteilung;
	}

	@Nonnull
	private Optional<Betreuungsmitteilung> findLatest(@Nonnull Collection<Betreuungsmitteilung> open) {
		Comparator<Mitteilung> bySentDateTime = Comparator
			.comparing(Mitteilung::getSentDatum, Comparator.nullsFirst(Comparator.naturalOrder()))
			.thenComparing(AbstractEntity::getTimestampErstellt, Comparator.nullsFirst(Comparator.naturalOrder()))
			.thenComparing(AbstractEntity::getId);

		return open.stream().max(bySentDateTime);
	}

	@Nonnull
	private String getMessage(
		@Nonnull BiFunction<BetreuungsmitteilungPensum, Integer, String> messageMapper,
		@Nonnull Set<BetreuungsmitteilungPensum> pensen) {

		List<BetreuungsmitteilungPensum> sorted = pensen.stream()
			.sorted(Gueltigkeit.GUELTIG_AB_COMPARATOR)
			.collect(Collectors.toList());

		return IntStream.rangeClosed(1, sorted.size())
			.mapToObj(index -> messageMapper.apply(sorted.get(index - 1), index))
			.collect(Collectors.joining(StringUtils.LF));
	}

	@Nonnull
	private BiFunction<BetreuungsmitteilungPensum, Integer, String> mahlzeitenMessage(@Nonnull Locale lang) {
		return (pensum, counter) -> ServerMessageUtil.getMessage(
			MESSAGE_MAHLZEIT_KEY,
			lang,
			counter,
			pensum.getGueltigkeit().getGueltigAb(),
			pensum.getGueltigkeit().getGueltigBis(),
			pensum.getPensum(),
			pensum.getMonatlicheBetreuungskosten(),
			pensum.getMonatlicheHauptmahlzeiten(),
			pensum.getTarifProHauptmahlzeit(),
			pensum.getMonatlicheNebenmahlzeiten(),
			pensum.getTarifProNebenmahlzeit());
	}

	@Nonnull
	private BiFunction<BetreuungsmitteilungPensum, Integer, String> defaultMessage(@Nonnull Locale lang) {
		return (pensum, counter) -> ServerMessageUtil.getMessage(
			MESSAGE_KEY,
			lang,
			counter,
			pensum.getGueltigkeit().getGueltigAb(),
			pensum.getGueltigkeit().getGueltigBis(),
			pensum.getPensum(),
			pensum.getMonatlicheBetreuungskosten());
	}

	protected boolean isSame(@Nonnull Betreuungsmitteilung betreuungsmitteilung, @Nonnull Betreuung betreuung) {
		List<? extends AbstractMahlzeitenPensum> fromBetreuung = betreuung.getBetreuungspensumContainers().stream()
			.map(BetreuungspensumContainer::getBetreuungspensumJA)
			.collect(Collectors.toList());

		Set<? extends AbstractMahlzeitenPensum> fromMitteilung = betreuungsmitteilung.getBetreuungspensen();

		return EbeguUtil.areComparableCollections(fromBetreuung, fromMitteilung, COMPARATOR_WITH_GUELTIGKEIT);
	}

	private boolean isEnabled(@Nonnull Betreuung betreuung, @Nonnull EinstellungKey key) {
		Gemeinde gemeinde = betreuung.extractGemeinde();
		Gesuchsperiode periode = betreuung.extractGesuchsperiode();

		return einstellungService.findEinstellung(key, gemeinde, periode).getValueAsBoolean();
	}

	@Nonnull
	private ErweiterteBetreuung getOrCreateErweiterteBetreuung(@Nonnull Betreuung betreuung) {
		ErweiterteBetreuungContainer container = betreuung.getErweiterteBetreuungContainer();
		ErweiterteBetreuung erweiterteBetreuung = container.getErweiterteBetreuungJA();

		if (erweiterteBetreuung != null) {
			return erweiterteBetreuung;
		}

		ErweiterteBetreuung created = new ErweiterteBetreuung();
		betreuung.getErweiterteBetreuungContainer().setErweiterteBetreuungJA(created);

		return created;
	}
}
