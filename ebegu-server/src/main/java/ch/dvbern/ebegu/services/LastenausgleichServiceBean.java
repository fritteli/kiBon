/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.LastenausgleichDetail;
import ch.dvbern.ebegu.entities.LastenausgleichDetail_;
import ch.dvbern.ebegu.entities.LastenausgleichGrundlagen;
import ch.dvbern.ebegu.entities.LastenausgleichGrundlagen_;
import ch.dvbern.ebegu.entities.Lastenausgleich_;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.lastenausgleich.AbstractLastenausgleichRechner;
import ch.dvbern.ebegu.lastenausgleich.LastenausgleichRechnerNew;
import ch.dvbern.ebegu.lastenausgleich.LastenausgleichRechnerOld;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.persistence.TransactionHelper;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/**
 * Service fuer den Lastenausgleich
 */
@Stateless
@Local(LastenausgleichService.class)
public class LastenausgleichServiceBean extends AbstractBaseService implements LastenausgleichService {

	private static final Logger LOG = LoggerFactory.getLogger(LastenausgleichServiceBean.class.getSimpleName());

	private static final BigDecimal SELBSTBEHALT = MathUtil.EXACT.fromNullSafe(0.20);
	private static final String NEWLINE = "\n";

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private VerfuegungService verfuegungService;

	@Inject
	private MailService mailService;

	@Inject
	private TransactionHelper transactionHelper;

	@Nonnull
	@Override
	public Collection<Lastenausgleich> getAllLastenausgleiche(@Nonnull Mandant mandant) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Lastenausgleich> query = cb.createQuery(Lastenausgleich.class);
		Root<Lastenausgleich> root = query.from(Lastenausgleich.class);
		var mandantPredicate = cb.equal(root.get(Lastenausgleich_.mandant), mandant);
		query.where(mandantPredicate);
		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	@Override
	public Collection<Lastenausgleich> getLastenausgleicheForGemeinden(@Nonnull Set<Gemeinde> gemeinden, @Nonnull Mandant mandant) {
		return this.getAllLastenausgleiche(mandant).stream().map(lastenausgleich -> {
			Lastenausgleich clone = new Lastenausgleich();
			// filter gemeinden that are not in the list
			clone.setLastenausgleichDetails(
				lastenausgleich.getLastenausgleichDetails()
					.stream()
					.filter(lastenausgleichDetail -> gemeinden.contains(lastenausgleichDetail.getGemeinde()))
					.collect(Collectors.toList()));
			// set total from filtered gemeinden
			clone.setTotalAlleGemeinden(clone.getLastenausgleichDetails().stream().reduce(
				BigDecimal.ZERO,
				(subtotal, lastenausgleichDetail) -> subtotal.add(lastenausgleichDetail.getBetragLastenausgleich())
					.add(lastenausgleichDetail.getTotalBetragGutscheineOhneSelbstbehalt()),
				BigDecimal::add));
			clone.setJahr(lastenausgleich.getJahr());
			clone.setId(lastenausgleich.getId());
			clone.setTimestampErstellt(lastenausgleich.getTimestampErstellt() != null ?
				lastenausgleich.getTimestampErstellt() :
				LocalDateTime.MIN);

			return clone;
		}).collect(Collectors.toSet());
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	@Override
	@Nonnull
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Lastenausgleich createLastenausgleichOld(
			int jahr,
			@Nonnull BigDecimal selbstbehaltPro100ProzentPlatz,
			Mandant mandant) {

		// Ueberpruefen, dass es nicht schon einen Lastenausgleich oder LastenausgleichGrundlagen gibt fuer dieses Jahr
		assertUnique(jahr);

		LOG.info("Berechnung Lastenausgleich wird gestartet");

		StringBuilder sb = new StringBuilder();
		sb.append("Erstelle Lastenausgleich für Jahr ").append(jahr)
			.append(" bei einem Selbstbehalt pro 100% Platz von ").append(selbstbehaltPro100ProzentPlatz)
			.append(NEWLINE);

		BigDecimal kostenPro100ProzentPlatz =
			MathUtil.DEFAULT.divideNullSafe(selbstbehaltPro100ProzentPlatz, SELBSTBEHALT);
		sb.append("Kosten pro 100% Platz: ").append(kostenPro100ProzentPlatz)
			.append(NEWLINE);

		LastenausgleichGrundlagen grundlagenErhebungsjahr = new LastenausgleichGrundlagen();
		grundlagenErhebungsjahr.setJahr(jahr);
		grundlagenErhebungsjahr.setSelbstbehaltPro100ProzentPlatz(selbstbehaltPro100ProzentPlatz);
		grundlagenErhebungsjahr.setKostenPro100ProzentPlatz(kostenPro100ProzentPlatz);
		persistence.persist(grundlagenErhebungsjahr);

		return calculateLastenausgleich(jahr, mandant, sb, grundlagenErhebungsjahr);
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	@Override
	@Nonnull
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Lastenausgleich createLastenausgleichNew(
		int jahr,
		Mandant mandant) {

		// Ueberpruefen, dass es nicht schon einen Lastenausgleich oder LastenausgleichGrundlagen gibt fuer dieses Jahr
		assertUnique(jahr);

		LOG.info("Berechnung Lastenausgleich wird gestartet");

		StringBuilder sb = new StringBuilder();
		sb.append("Erstelle Lastenausgleich für Jahr ").append(jahr)
			.append(" ohne Selbstbehalt pro 100% Platz ")
			.append(NEWLINE);

		LastenausgleichGrundlagen grundlagenErhebungsjahr = new LastenausgleichGrundlagen();
		grundlagenErhebungsjahr.setJahr(jahr);
		persistence.persist(grundlagenErhebungsjahr);

		return calculateLastenausgleich(jahr, mandant, sb, grundlagenErhebungsjahr);
	}

	private Lastenausgleich calculateLastenausgleich(
		int jahr,
		@Nonnull Mandant mandant,
		@Nonnull StringBuilder sb,
		@Nonnull LastenausgleichGrundlagen grundlagenErhebungsjahr
	) {
		transactionHelper.runInNewTransaction(() -> {
				Lastenausgleich lastenausgleich = new Lastenausgleich();
				lastenausgleich.setJahr(jahr);
				lastenausgleich.setMandant(mandant);
				persistence.persist(lastenausgleich);
			}
		);

		// Die regulare Abrechnung
		Collection<Gemeinde> aktiveGemeinden = gemeindeService.getAktiveGemeinden(mandant);
		int counter = 0;
		for (Gemeinde gemeinde : aktiveGemeinden) {
			transactionHelper.runInNewTransaction(() -> {
				AbstractLastenausgleichRechner lastenausgleichRechner = getLastenausgleichRechnerForYear(jahr);
				lastenausgleichRechner.logLastenausgleichRechnerType(jahr, sb);
				Lastenausgleich lastenausgleichToUpdate = findLastenausgleich(jahr).orElseThrow(() -> new EbeguEntityNotFoundException(
					"calculateLastenausgleich - details Berechnung",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					jahr
				));
				LastenausgleichDetail detailErhebung =
					lastenausgleichRechner.createLastenausgleichDetail(gemeinde, lastenausgleichToUpdate, grundlagenErhebungsjahr);
				if (detailErhebung != null) {
					lastenausgleichToUpdate.addLastenausgleichDetail(detailErhebung);
					sb.append("Reguläre Abrechnung Gemeinde ").append(gemeinde.getName()).append(NEWLINE);
					sb.append(detailErhebung).append(NEWLINE);
					persistence.merge(lastenausgleichToUpdate);
				}
			});

			if (counter % 10 == 0) {
				LOG.info("LastenausgleichDetail für " + counter + " Gemeinden von " + aktiveGemeinden.size() + " berechnet");
			}
			counter++;
		}
		// Korrekturen frueherer Jahre: Wir gehen bis 10 Jahre retour
		for (int i = 1; i < 10; i++) {
			int korrekturJahr = jahr - i;
			LOG.info("Berechne Korrekturen für Jahr " + korrekturJahr);

			Optional<LastenausgleichGrundlagen> grundlagenKorrekturjahr = findLastenausgleichGrundlagen(korrekturJahr);
			if (grundlagenKorrekturjahr.isPresent()) {
				sb.append("Korrekturen für Jahr ").append(korrekturJahr).append(NEWLINE);
				counter = 0;
				for (Gemeinde gemeinde : aktiveGemeinden) {
					transactionHelper.runInNewTransaction(() -> {
						Lastenausgleich lastenausgleichForKorrektur = findLastenausgleich(jahr).orElseThrow(() -> new EbeguEntityNotFoundException(
							"calculateLastenausgleich - korrektur",
							ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
							jahr
						));
						handleKorrekturJahrFuerGemeinde(
							korrekturJahr,
							gemeinde,
							lastenausgleichForKorrektur,
							grundlagenKorrekturjahr.get(),
							sb);
					});
					if (counter % 10 == 0) {
						LOG.info("Korrektur für " + counter + " Gemeinden von " + aktiveGemeinden.size() + " berechnet");
					}
					counter++;
				}
			}
			// Wir loggen dies mit WARN, damit wir es im Sentry sehen
			LOG.warn(sb.toString());
		}

		// Am Schluss das berechnete Total speichern
		transactionHelper.runInNewTransaction(() -> {
			Lastenausgleich lastenausgleichToUpdate = findLastenausgleich(jahr).orElseThrow(() -> new EbeguEntityNotFoundException(
				"calculateLastenausgleich - Total berechnen",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				jahr
			));
			BigDecimal totalGesamterLastenausgleich = BigDecimal.ZERO;
			for (LastenausgleichDetail lastenausgleichDetail : lastenausgleichToUpdate.getLastenausgleichDetails()) {
				totalGesamterLastenausgleich = MathUtil.DEFAULT.addNullSafe(
					totalGesamterLastenausgleich,
					lastenausgleichDetail.getBetragLastenausgleich());
				totalGesamterLastenausgleich = MathUtil.DEFAULT.addNullSafe(
					totalGesamterLastenausgleich,
					lastenausgleichDetail.getTotalBetragGutscheineOhneSelbstbehalt());
			}
			lastenausgleichToUpdate.setTotalAlleGemeinden(totalGesamterLastenausgleich);
			persistence.merge(lastenausgleichToUpdate);
		});
		Lastenausgleich lastenausgleich = findLastenausgleich(jahr).orElseThrow(() -> new EbeguEntityNotFoundException(
			"calculateLastenausgleich",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			jahr
		));
		return lastenausgleich;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void sendEmailsToGemeinden(@Nonnull Lastenausgleich storedLastenausgleich) {
		storedLastenausgleich.getLastenausgleichDetails().stream()
			.map(LastenausgleichDetail::getGemeinde)
			.distinct()
			.forEach(gemeinde -> mailService.sendInfoLastenausgleichGemeinde(gemeinde, storedLastenausgleich));
	}

	private void handleKorrekturJahrFuerGemeinde(
		int korrekturJahr,
		@Nonnull Gemeinde gemeinde,
		@Nonnull Lastenausgleich lastenausgleich,
		@Nonnull LastenausgleichGrundlagen grundlagenKorrekturjahr,
		@Nonnull StringBuilder logBuilder) {
		logBuilder.append("Gemeinde ").append(gemeinde.getName()).append(NEWLINE);
		AbstractLastenausgleichRechner lastenausgleichRechner = getLastenausgleichRechnerForYear(korrekturJahr);
		lastenausgleichRechner.logLastenausgleichRechnerType(korrekturJahr, logBuilder);

		// Wir ermitteln für die Gemeinde und das Korrekurjahr den aktuell gültigen Wert
		LastenausgleichDetail detailAktuellesTotalKorrekturjahr =
			lastenausgleichRechner.createLastenausgleichDetail(gemeinde, lastenausgleich, grundlagenKorrekturjahr);
		logBuilder.append("Aktuell berechnetes Total: ")
			.append(NEWLINE)
			.append(detailAktuellesTotalKorrekturjahr)
			.append(NEWLINE);

		if (detailAktuellesTotalKorrekturjahr != null) {
			// Dieses Detail ist jetzt aber das aktuelle Total für das Jahr. Uns interessiert aber die eventuelle
			// Differenz zu bereits ausgeglichenen Beträgen
			Collection<LastenausgleichDetail> detailsBereitsVerrechnetKorrekturjahr =
				findLastenausgleichDetailForKorrekturen(korrekturJahr, gemeinde);
			if (CollectionUtils.isNotEmpty(detailsBereitsVerrechnetKorrekturjahr)) {
				LastenausgleichDetail detailBisherigeWerte = new LastenausgleichDetail();
				for (LastenausgleichDetail detailBereitsVerrechnet : detailsBereitsVerrechnetKorrekturjahr) {
					detailBisherigeWerte.add(detailBereitsVerrechnet);
				}
				logBuilder.append("Davon bereits verrechnet: ")
					.append(NEWLINE)
					.append(detailBisherigeWerte)
					.append(NEWLINE);
				// Gibt es eine Differenz?
				if (detailBisherigeWerte.hasChanged(detailAktuellesTotalKorrekturjahr)) {
					// Es gibt eine Differenz (wobei wir nur den Betrag des Lastenausgleiches anschauen)
					// Wir rechnen das bisher verrechnete minus
					LastenausgleichDetail detailKorrektur = lastenausgleichRechner.createLastenausgleichDetailKorrektur(detailBisherigeWerte);
					detailKorrektur.setLastenausgleich(lastenausgleich);
					lastenausgleich.addLastenausgleichDetail(detailKorrektur);
					logBuilder.append("Korrektur PLUS: ").append(NEWLINE).append(detailKorrektur).append(NEWLINE);
					// Und erstellen einen neuen Korrektur-Eintrag mit dem aktuell berechneten Wert
					lastenausgleich.addLastenausgleichDetail(detailAktuellesTotalKorrekturjahr);
					logBuilder.append("Korrektur MINUS: ")
						.append(NEWLINE)
						.append(detailAktuellesTotalKorrekturjahr)
						.append(NEWLINE);
					persistence.merge(lastenausgleich);
				}
			}
		}
	}

	@Nonnull
	private AbstractLastenausgleichRechner getLastenausgleichRechnerForYear(
		int jahr
	) {
		if (jahr < Constants.FIRST_YEAR_LASTENAUSGLEICH_WITHOUT_SELBSTBEHALT) {
			return new LastenausgleichRechnerOld(verfuegungService);
		}
		return new LastenausgleichRechnerNew(verfuegungService);
	}

	@Nonnull
	@Override
	public Lastenausgleich findLastenausgleich(@Nonnull String lastenausgleichId) {
		return persistence.find(Lastenausgleich.class, lastenausgleichId);
	}

	@Nonnull
	private Optional<Lastenausgleich> findLastenausgleichByJahr(int jahr) {
		Optional<Lastenausgleich> optional = criteriaQueryHelper.getEntityByUniqueAttribute(
			Lastenausgleich.class,
			jahr,
			Lastenausgleich_.jahr);
		return optional;
	}

	@Override
	@Nonnull
	public Optional<LastenausgleichGrundlagen> findLastenausgleichGrundlagen(int jahr) {
		Optional<LastenausgleichGrundlagen> optional =
			criteriaQueryHelper.getEntityByUniqueAttribute(LastenausgleichGrundlagen.class, jahr,
				LastenausgleichGrundlagen_.jahr);
		return optional;
	}

	@Override
	public void removeLastenausgleich(@Nonnull String lastenausgleichId) {
		// Lastenausgleich und -Grundlagen löschen
		requireNonNull(lastenausgleichId);
		Lastenausgleich lastenausgleichToRemove = findLastenausgleich(lastenausgleichId);
		Optional<LastenausgleichGrundlagen> lastenausgleichGrundlagen =
			findLastenausgleichGrundlagen(lastenausgleichToRemove.getJahr());
		lastenausgleichGrundlagen.ifPresent(lastenausgleichGrundlagen1 -> persistence.remove(lastenausgleichGrundlagen1));
		persistence.remove(lastenausgleichToRemove);
	}

	@Override
	@Nonnull
	public Optional<Lastenausgleich> findLastenausgleich(@Nonnull Integer jahr) {
		return criteriaQueryHelper.getEntityByUniqueAttribute(Lastenausgleich.class, jahr,
				Lastenausgleich_.jahr);
	}

	private Collection<LastenausgleichDetail> findLastenausgleichDetailForKorrekturen(
		int jahr,
		@Nonnull Gemeinde gemeinde) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<LastenausgleichDetail> query = cb.createQuery(LastenausgleichDetail.class);
		Root<LastenausgleichDetail> root = query.from(LastenausgleichDetail.class);

		ParameterExpression<Integer> paramJahr = cb.parameter(Integer.class, "paramJahr");
		ParameterExpression<Gemeinde> paramGemeinde = cb.parameter(Gemeinde.class, "paramGemeinde");

		Predicate predicateJahr = cb.equal(root.get(LastenausgleichDetail_.jahr), paramJahr);
		Predicate predicateGemeinde = cb.equal(root.get(LastenausgleichDetail_.gemeinde), paramGemeinde);
		query.where(predicateJahr, predicateGemeinde);

		TypedQuery<LastenausgleichDetail> tq = persistence.getEntityManager().createQuery(query);

		tq.setParameter("paramJahr", jahr);
		tq.setParameter("paramGemeinde", gemeinde);
		return tq.getResultList();
	}

	private void assertUnique(int jahr) {
		if (findLastenausgleichGrundlagen(jahr).isPresent()) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"assertUnique",
				ErrorCodeEnum.ERROR_LASTENAUSGLEICH_GRUNDLAGEN_EXISTS);
		}
		if (findLastenausgleichByJahr(jahr).isPresent()) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"assertUnique",
				ErrorCodeEnum.ERROR_LASTENAUSGLEICH_EXISTS);
		}
	}
}
