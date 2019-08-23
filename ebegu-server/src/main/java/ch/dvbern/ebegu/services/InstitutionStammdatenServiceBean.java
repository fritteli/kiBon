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

package ch.dvbern.ebegu.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdatenFerieninsel;
import ch.dvbern.ebegu.entities.InstitutionStammdatenFerieninsel_;
import ch.dvbern.ebegu.entities.InstitutionStammdatenTagesschule;
import ch.dvbern.ebegu.entities.InstitutionStammdatenTagesschule_;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.lib.cdipersistence.Persistence;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service fuer InstitutionStammdaten
 */
@Stateless
@Local(InstitutionStammdatenService.class)
@PermitAll
public class InstitutionStammdatenServiceBean extends AbstractBaseService implements InstitutionStammdatenService {

	private static final String GP_START = "gpStart";
	private static final String GP_END = "gpEnd";
	private static final String GEMEINDEN = "gemeinden";

	// ID der statischen, unbekannten Institution Stammdaten. Wird verwendet um eine provisorische Berechnung zu generieren
	// und darf dem Benutzer <b>nie>/b> angezeigt werden
	private static final String ID_UNKNOWN_INSTITUTION_STAMMDATEN_KITA = "00000000-0000-0000-0000-000000000000";
	private static final String ID_UNKNOWN_INSTITUTION_STAMMDATEN_TAGESFAMILIE = "00000000-0000-0000-0000-000000000001";

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private PrincipalBean principalBean;

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_INSTITUTION, ADMIN_TRAEGERSCHAFT })
	public InstitutionStammdaten saveInstitutionStammdaten(@Nonnull InstitutionStammdaten institutionStammdaten) {
		Objects.requireNonNull(institutionStammdaten);

		// always when stammdaten are saved we need to reset the flag stammdatenCheckRequired to false
		institutionService.updateStammdatenCheckRequired(institutionStammdaten.getInstitution().getId(), false);

		return persistence.merge(institutionStammdaten);
	}

	@Nonnull
	@Override
	@PermitAll
	public Optional<InstitutionStammdaten> findInstitutionStammdaten(@Nonnull final String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		InstitutionStammdaten a = persistence.find(InstitutionStammdaten.class, id);
		return Optional.ofNullable(a);
	}

	@Override
	@Nonnull
	@PermitAll
	public Collection<InstitutionStammdaten> getAllInstitutionStammdaten() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<InstitutionStammdaten> query = cb.createQuery(InstitutionStammdaten.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);

		query.where(excludeUnknownInstitutionStammdatenPredicate(root));

		return persistence.getCriteriaResults(query);
	}

	@Override
	@RolesAllowed(SUPER_ADMIN)
	public void removeInstitutionStammdatenByInstitution(@Nonnull String institutionId) {
		Objects.requireNonNull(institutionId);
		InstitutionStammdaten institutionStammdatenToRemove = getInstitutionStammdatenByInstitution(institutionId);

		persistence.remove(institutionStammdatenToRemove);
	}

	@Override
	@PermitAll
	public Collection<InstitutionStammdaten> getAllInstitutionStammdatenByDate(@Nonnull LocalDate date) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<InstitutionStammdaten> query = cb.createQuery(InstitutionStammdaten.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);
		query.select(root);

		ParameterExpression<LocalDate> dateParam = cb.parameter(LocalDate.class, "date");
		Predicate intervalPredicate = cb.between(dateParam,
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis));

		query.where(intervalPredicate, excludeUnknownInstitutionStammdatenPredicate(root));

		TypedQuery<InstitutionStammdaten> q = persistence.getEntityManager().createQuery(query).setParameter(dateParam, date);
		List<InstitutionStammdaten> resultList = q.getResultList();
		return resultList;
	}

	@Override
	public Collection<InstitutionStammdaten> getAllActiveInstitutionStammdatenByGesuchsperiode(@Nonnull String gesuchsperiodeId) {

		Benutzer currentBenutzer = principalBean.getBenutzer();
		if (currentBenutzer.getCurrentBerechtigung().getRole().isRoleGemeindeabhaengig()) {
			return getAllActiveInstitutionStammdatenByGesuchsperiodeAndGemeinde(gesuchsperiodeId, currentBenutzer.extractGemeindenForUser());
		}

		Gesuchsperiode gesuchsperiode = persistence.find(Gesuchsperiode.class, gesuchsperiodeId);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<InstitutionStammdaten> query = cb.createQuery(InstitutionStammdaten.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);
		query.select(root);
		List<Predicate> predicates = new ArrayList<>();

		ParameterExpression<LocalDate> startParam = cb.parameter(LocalDate.class, GP_START);
		ParameterExpression<LocalDate> endParam = cb.parameter(LocalDate.class, GP_END);

		// InstStammdaten Ende muss NACH GP Start sein
		// InstStammdaten Start muss VOR GP Ende sein
		predicates.add(cb.greaterThanOrEqualTo(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis), startParam));
		predicates.add(cb.lessThanOrEqualTo(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb), endParam));

		predicates.add(excludeUnknownInstitutionStammdatenPredicate(root));
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		TypedQuery<InstitutionStammdaten> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(GP_START, gesuchsperiode.getGueltigkeit().getGueltigAb());
		typedQuery.setParameter(GP_END, gesuchsperiode.getGueltigkeit().getGueltigBis());
		return typedQuery.getResultList();
	}

	@Override
	public Collection<InstitutionStammdaten> getAllActiveInstitutionStammdatenByGesuchsperiodeAndGemeinde(@Nonnull String gesuchsperiodeId,
		@Nonnull String gemeindeId) {

		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId)
			.orElseThrow(() -> new EbeguEntityNotFoundException("getAllActiveInstitutionStammdatenByGesuchsperiodeAndGemeinde",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gemeindeId));

		List<Gemeinde> gemeinden = Collections.singletonList(gemeinde );
		return getAllActiveInstitutionStammdatenByGesuchsperiodeAndGemeinde(gesuchsperiodeId, gemeinden);
	}

	private Collection<InstitutionStammdaten> getAllActiveInstitutionStammdatenByGesuchsperiodeAndGemeinde(
		@Nonnull String gesuchsperiodeId,
		@Nonnull Collection<Gemeinde> gemeinden
	) {

		Gesuchsperiode gesuchsperiode = persistence.find(Gesuchsperiode.class, gesuchsperiodeId);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<InstitutionStammdaten> query = cb.createQuery(InstitutionStammdaten.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);
		query.select(root);
		List<Predicate> predicates = new ArrayList<>();

		ParameterExpression<LocalDate> startParam = cb.parameter(LocalDate.class, GP_START);
		ParameterExpression<LocalDate> endParam = cb.parameter(LocalDate.class, GP_END);
		ParameterExpression<Collection> gemeindeParam = cb.parameter(Collection.class, GEMEINDEN);

		// InstStammdaten Ende muss NACH GP Start sein
		// InstStammdaten Start muss VOR GP Ende sein
		predicates.add(cb.greaterThanOrEqualTo(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis), startParam));
		predicates.add(cb.lessThanOrEqualTo(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb), endParam));

		// Falls es sich um ein Tagesschule- oder Ferieninselangebot handelt, muss ich für die Gemeinde berechtigt sein
		Join<InstitutionStammdaten, InstitutionStammdatenTagesschule> joinTagesschule = root.join(InstitutionStammdaten_.institutionStammdatenTagesschule, JoinType.LEFT);
		Join<InstitutionStammdaten, InstitutionStammdatenFerieninsel> joinFerieninsel = root.join(InstitutionStammdaten_.institutionStammdatenFerieninsel, JoinType.LEFT);

		Predicate predicateBetreuungsgutschein =
			root.get(InstitutionStammdaten_.betreuungsangebotTyp).in(BetreuungsangebotTyp.getBetreuungsgutscheinTypes());
		Predicate predicateTypTagesschule = cb.equal(root.get(InstitutionStammdaten_.betreuungsangebotTyp), BetreuungsangebotTyp.TAGESSCHULE);
		Predicate predicateTypFerieninsel = cb.equal(root.get(InstitutionStammdaten_.betreuungsangebotTyp), BetreuungsangebotTyp.FERIENINSEL);

		Predicate predicateGemeindeTagesschule = joinTagesschule.get(InstitutionStammdatenTagesschule_.gemeinde).in(gemeindeParam);
		Predicate predicateGemeindeFerieninsel = joinFerieninsel.get(InstitutionStammdatenFerieninsel_.gemeinde).in(gemeindeParam);

		// TS und FI sind okay, wenn es der jeweilige Typ ist UND ich für die Gemeinde berechtigt bin
		Predicate predicateTagesschule = cb.and(predicateTypTagesschule, predicateGemeindeTagesschule);
		Predicate predicateFerieninsel = cb.and(predicateTypFerieninsel, predicateGemeindeFerieninsel);

		// Die Institution insgesamt ist okay, wenn es ein BG ist ODER eine berechtigte TS oder FI
		Predicate predicateBerechtigteInstitution = cb.or(predicateBetreuungsgutschein, predicateTagesschule, predicateFerieninsel);
		predicates.add(predicateBerechtigteInstitution);

		predicates.add(excludeUnknownInstitutionStammdatenPredicate(root));
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		TypedQuery<InstitutionStammdaten> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(GP_START, gesuchsperiode.getGueltigkeit().getGueltigAb());
		typedQuery.setParameter(GP_END, gesuchsperiode.getGueltigkeit().getGueltigBis());
		typedQuery.setParameter(GEMEINDEN, gemeinden);
		return typedQuery.getResultList();
	}

	@Override
	@Nonnull
	@PermitAll
	public InstitutionStammdaten getInstitutionStammdatenByInstitution(String institutionId) {
		Institution institution = institutionService.findInstitution(institutionId)
			.orElseThrow(() ->
				new EbeguEntityNotFoundException(
					"getInstitutionStammdatenByInstitution_institution",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					institutionId)
			);

		return criteriaQueryHelper.getEntityByUniqueAttribute(
			InstitutionStammdaten.class,
			institution,
			InstitutionStammdaten_.institution
		).orElseThrow(() -> new EbeguEntityNotFoundException
			("getInstitutionStammdatenByInstitution_institutionStammdaten", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, institutionId));
	}

	@Nullable
	@Override
	@PermitAll
	public InstitutionStammdaten fetchInstitutionStammdatenByInstitution(String institutionId) {
		Institution institution = institutionService.findInstitution(institutionId).orElseThrow(() -> new EbeguEntityNotFoundException
			("fetchInstitutionStammdatenByInstitution", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, institutionId));

		return criteriaQueryHelper.getEntityByUniqueAttribute(
			InstitutionStammdaten.class,
			institution,
			InstitutionStammdaten_.institution
		).orElse(null);
	}

	@Override
	@PermitAll
	public Collection<BetreuungsangebotTyp> getBetreuungsangeboteForInstitutionenOfCurrentBenutzer() {
		UserRole role = principalBean.discoverMostPrivilegedRoleOrThrowExceptionIfNone();
		if (role.isRoleSchulamt()) { // fuer Schulamt muessen wir nichts machen. Direkt Schulamttypes zurueckgeben
			return BetreuungsangebotTyp.getSchulamtTypes();
		}
		Collection<Institution> institutionenForCurrentBenutzer = institutionService.getAllowedInstitutionenForCurrentBenutzer(false);
		if (institutionenForCurrentBenutzer.isEmpty()) {
			return new ArrayList<>();
		}

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<BetreuungsangebotTyp> query = cb.createQuery(BetreuungsangebotTyp.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);
		query.select(root.get(InstitutionStammdaten_.betreuungsangebotTyp));
		query.distinct(true);

		ParameterExpression<LocalDate> dateParam = cb.parameter(LocalDate.class, "date");
		Predicate intervalPredicate = cb.between(dateParam,
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis));

		Predicate institutionPredicate = root.get(InstitutionStammdaten_.institution).in(institutionenForCurrentBenutzer);

		query.where(intervalPredicate, institutionPredicate, excludeUnknownInstitutionStammdatenPredicate(root));
		TypedQuery<BetreuungsangebotTyp> q = persistence.getEntityManager().createQuery(query).setParameter(dateParam, LocalDate.now());
		List<BetreuungsangebotTyp> resultList = q.getResultList();
		return resultList;
	}

	private Predicate excludeUnknownInstitutionStammdatenPredicate(Root root) {
		return root.get(AbstractEntity_.id)
			.in(Arrays.asList(ID_UNKNOWN_INSTITUTION_STAMMDATEN_KITA, ID_UNKNOWN_INSTITUTION_STAMMDATEN_TAGESFAMILIE))
			.not();
	}

}
