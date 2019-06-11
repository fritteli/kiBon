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

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.BenutzerPredicateObjectDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.BenutzerTableFilterDTO;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Benutzer_;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.BerechtigungHistory;
import ch.dvbern.ebegu.entities.BerechtigungHistory_;
import ch.dvbern.ebegu.entities.Berechtigung_;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.GemeindeStammdaten_;
import ch.dvbern.ebegu.entities.Gemeinde_;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Institution_;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.entities.Traegerschaft_;
import ch.dvbern.ebegu.enums.BenutzerStatus;
import ch.dvbern.ebegu.enums.EinladungTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.SearchMode;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.EntityExistsException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.errors.MailException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.util.SearchUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EnumUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRole.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRole.getBgAndGemeindeRoles;
import static ch.dvbern.ebegu.enums.UserRole.getJugendamtRoles;
import static ch.dvbern.ebegu.enums.UserRole.getSchulamtRoles;
import static ch.dvbern.ebegu.enums.UserRole.getTsAndGemeindeRoles;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setGemeindeFilterForCurrentUser;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setInstitutionFilterForCurrentUser;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setMandantFilterForCurrentUser;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setRoleFilterForCurrentUser;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setSuperAdminFilterForCurrentUser;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setTraegerschaftFilterForCurrentUser;
import static ch.dvbern.ebegu.services.util.PredicateHelper.NEW;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static javax.persistence.LockModeType.PESSIMISTIC_WRITE;

/**
 * Service fuer Benutzer
 */
@PermitAll
@Stateless
@Local(BenutzerService.class)
public class BenutzerServiceBean extends AbstractBaseService implements BenutzerService {

	private static final Logger LOG = LoggerFactory.getLogger(BenutzerServiceBean.class.getSimpleName());

	public static final String ID_SUPER_ADMIN = "22222222-2222-2222-2222-222222222222";

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private AuthService authService;

	@Inject
	private MailService mailService;

	@Inject
	private Authorizer authorizer;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Inject
	private FallService fallService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private MitteilungService mitteilungService;

	@Nonnull
	@Override
	@PermitAll
	public Benutzer saveBenutzerBerechtigungen(@Nonnull Benutzer benutzer, boolean currentBerechtigungChanged) {
		requireNonNull(benutzer);
		prepareBenutzerForSave(benutzer, currentBerechtigungChanged);
		authorizer.checkWriteAuthorization(benutzer);
		checkSuperuserRoleZuteilung(benutzer);
		return persistence.merge(benutzer);
	}

	@Nonnull
	@Override
	@PermitAll
	public Benutzer saveBenutzer(@Nonnull Benutzer benutzer) {
		requireNonNull(benutzer);
		authorizer.checkWriteAuthorization(benutzer);
		if (benutzer.isNew()) {
			return persistence.persist(benutzer);
		}
		return persistence.merge(benutzer);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Benutzer createAdminGemeindeByEmail(@Nonnull String adminMail, @Nonnull Gemeinde gemeinde) {
		requireNonNull(gemeinde);

		return createBenutzerFromEmail(
			adminMail,
			UserRole.ADMIN_BG,
			gemeinde,
			b -> b.getGemeindeList().add(gemeinde));
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Benutzer createAdminInstitutionByEmail(@Nonnull String adminMail, @Nonnull Institution institution) {
		requireNonNull(institution);

		return createBenutzerFromEmail(
			adminMail,
			UserRole.ADMIN_INSTITUTION,
			institution,
			b -> b.setInstitution(institution));
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Benutzer createAdminTraegerschaftByEmail(@Nonnull String adminMail, @Nonnull Traegerschaft traegerschaft) {
		requireNonNull(traegerschaft);

		Benutzer admin = createBenutzerFromEmail(
			adminMail,
			UserRole.ADMIN_TRAEGERSCHAFT,
			traegerschaft,
			b -> b.setTraegerschaft(traegerschaft));

		return admin;
	}

	@Nonnull
	private <T extends AbstractEntity> Benutzer createBenutzerFromEmail(
		@Nonnull String adminMail,
		@Nonnull UserRole role,
		@Nullable T associatedEntity,
		@Nonnull Consumer<Berechtigung> appender
	) {
		requireNonNull(adminMail);
		requireNonNull(principalBean.getMandant());

		checkArgument(role.getRollenAbhaengigkeit().getAssociatedEntityClass()
			.map(clazz -> clazz.isInstance(associatedEntity))
			.orElseGet(() -> associatedEntity == null)
		);

		final Benutzer benutzer = new Benutzer();
		benutzer.setEmail(adminMail);
		benutzer.setNachname(Constants.UNKNOWN);
		benutzer.setVorname(Constants.UNKNOWN);
		benutzer.setUsername(adminMail);
		benutzer.setStatus(BenutzerStatus.EINGELADEN);
		benutzer.setMandant(principalBean.getMandant());

		final Berechtigung berechtigung = new Berechtigung();
		berechtigung.setRole(role);
		berechtigung.setBenutzer(benutzer);
		berechtigung.setGueltigkeit(new DateRange(LocalDate.now(), Constants.END_OF_TIME));
		benutzer.getBerechtigungen().add(berechtigung);

		appender.accept(berechtigung);

		return saveBenutzer(benutzer);
	}

	@Nonnull
	@Override
	@RolesAllowed({
		SUPER_ADMIN,
		ADMIN_BG,
		ADMIN_GEMEINDE,
		ADMIN_TS,
		ADMIN_MANDANT,
		ADMIN_INSTITUTION,
		ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_MANDANT,
	})
	public Benutzer einladen(@Nonnull Einladung einladung) {
		requireNonNull(einladung);

		checkEinladung(einladung);

		Benutzer persistedBenutzer = saveBenutzer(einladung.getEingeladener());

		try {
			mailService.sendBenutzerEinladung(principalBean.getBenutzer(), einladung);

		} catch (MailException e) {
			String message =
				String.format("Es konnte keine Email Einladung an %s geschickt werden", persistedBenutzer.getEmail());
			KibonLogLevel logLevel = ebeguConfiguration.getIsDevmode() ? KibonLogLevel.INFO : KibonLogLevel.ERROR;
			throw new EbeguRuntimeException(logLevel, "sendEinladung", message, ErrorCodeEnum.ERROR_MAIL, e);
		}
		return persistedBenutzer;
	}

	@Nonnull
	@Override
	@RolesAllowed(SUPER_ADMIN)
	public void erneutEinladen(@Nonnull Benutzer eingeladener) {
		try {
			if (eingeladener.getStatus() != BenutzerStatus.EINGELADEN) {
				throw new EbeguRuntimeException(
					KibonLogLevel.INFO,
					eingeladener.getUsername(),
					ErrorCodeEnum.ERROR_BENUTZER_STATUS_NOT_EINGELADEN);
			}
			Einladung einladung = Einladung.forRolle(eingeladener);
			mailService.sendBenutzerEinladung(principalBean.getBenutzer(), einladung);
		} catch (MailException e) {
			String message =
				String.format("Es konnte keine Email Einladung an %s geschickt werden", eingeladener.getEmail());
			KibonLogLevel logLevel = ebeguConfiguration.getIsDevmode() ? KibonLogLevel.INFO : KibonLogLevel.ERROR;
			throw new EbeguRuntimeException(logLevel, "sendEinladung", message, ErrorCodeEnum.ERROR_MAIL, e);
		}
	}

	/**
	 * According to the type of Einladung it checks that the given benutzer meets the conditions required.
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private void checkEinladung(@Nonnull Einladung einladung) {
		Benutzer benutzer = einladung.getEingeladener();
		checkSuperuserRoleZuteilung(einladung.getEingeladener());
		EinladungTyp einladungTyp = einladung.getEinladungTyp();
		checkArgument(Objects.equals(benutzer.getMandant(), principalBean.getMandant()));

		if (einladungTyp == EinladungTyp.MITARBEITER && (!benutzer.isNew()
			|| findBenutzer(benutzer.getUsername()).isPresent())) {
			// when inviting a new Mitarbeiter the user cannot exist.
			// For any other invitation the user may exist already
			throw new EntityExistsException(
				KibonLogLevel.INFO,
				Benutzer.class,
				"email",
				benutzer.getUsername(),
				ErrorCodeEnum.ERROR_BENUTZER_EXISTS);
		}

		if (benutzer.isNew() && benutzer.getStatus() != BenutzerStatus.EINGELADEN) {
			throw new EbeguRuntimeException(
				KibonLogLevel.INFO,
				benutzer.getUsername(),
				ErrorCodeEnum.ERROR_BENUTZER_STATUS_NOT_EINGELADEN);
		}
	}

	private void checkSuperuserRoleZuteilung(@Nonnull Benutzer benutzer) {
		// Nur ein Superadmin kann Superadmin-Rechte vergeben!
		if (benutzer.getRole() == UserRole.SUPER_ADMIN && !principalBean.isCallerInRole(UserRoleName.SUPER_ADMIN)) {
			throw new IllegalStateException(
				"Nur ein Superadmin kann Superadmin-Rechte vergeben. Dies wurde aber versucht durch: "
					+ principalBean.getBenutzer().getUsername());
		}
	}

	@Nonnull
	@Override
	@PermitAll
	public Optional<Benutzer> findBenutzer(@Nonnull String username) {
		requireNonNull(username, "username muss gesetzt sein");
		return criteriaQueryHelper.getEntityByUniqueAttribute(Benutzer.class, username, Benutzer_.username);
	}

	@Nonnull
	@Override
	@PermitAll
	public Optional<Benutzer> findAndLockBenutzer(@Nonnull String username) {
		requireNonNull(username, "username muss gesetzt sein");

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);
		Predicate predicateUsername = cb.equal(root.get(Benutzer_.username), username);

		query.where(predicateUsername);
		query.distinct(true);

		try {
			Optional<Benutzer> benutzer = Optional.of(persistence.getEntityManager()
				.createQuery(query)
				.setLockMode(PESSIMISTIC_WRITE)
				.getSingleResult());

			return benutzer;
		} catch (NoResultException nre) {
			return Optional.empty();
		}
	}

	@Nonnull
	@Override
	public Optional<Benutzer> findBenutzerById(@Nonnull String id) {
		requireNonNull(id, "id muss gesetzt sein");

		return Optional.ofNullable(persistence.find(Benutzer.class, id));
	}

	@Nonnull
	@Override
	@PermitAll
	public Optional<Benutzer> findBenutzerByEmail(@Nonnull String email) {
		Objects.requireNonNull(email, "email muss gesetzt sein");
		return criteriaQueryHelper.getEntityByUniqueAttribute(Benutzer.class, email, Benutzer_.email);
	}

	@Nonnull
	@Override
	@PermitAll
	public Optional<Benutzer> findBenutzerByExternalUUID(@Nonnull String externalUUID) {
		requireNonNull(externalUUID, "externalUUID muss gesetzt sein");
		return criteriaQueryHelper.getEntityByUniqueAttribute(Benutzer.class, externalUUID, Benutzer_.externalUUID);
	}

	@Nonnull
	@Override
	@PermitAll
	public Collection<Benutzer> getAllBenutzer() {
		return new ArrayList<>(criteriaQueryHelper.getAll(Benutzer.class));
	}

	@Nonnull
	@Override
	@PermitAll
	public Collection<Benutzer> getGemeindeAdministratoren(Gemeinde gemeinde) {
		return getBenutzersOfRoles(UserRole.getAllGemeindeAdminRoles(), gemeinde);
	}

	@Nonnull
	@Override
	@PermitAll
	public Collection<Benutzer> getGemeindeSachbearbeiter(Gemeinde gemeinde) {
		return getBenutzersOfRoles(UserRole.getAllGemeindeSachbearbeiterRoles(), gemeinde);
	}

	@Nonnull
	@Override
	public Collection<Benutzer> getInstitutionAdministratoren(Institution institution) {
		return getBenutzersOfRoles(UserRole.getAllInstitutionAdminRoles(), institution);
	}

	@Nonnull
	@Override
	public Collection<Benutzer> getInstitutionSachbearbeiter(Institution institution) {
		return getBenutzersOfRoles(UserRole.getAllInstitutionSachbearbeiterRoles(), institution);
	}

	@Nonnull
	@Override
	@PermitAll
	public Collection<Benutzer> getBenutzerBgOrGemeinde(Gemeinde gemeinde) {
		return getBenutzersOfRoles(getBgAndGemeindeRoles(), gemeinde);
	}

	@Nonnull
	@Override
	@PermitAll
	public Collection<Benutzer> getBenutzerTsOrGemeinde(Gemeinde gemeinde) {
		return getBenutzersOfRoles(getTsAndGemeindeRoles(), gemeinde);
	}

	@Nonnull
	@Override
	@PermitAll
	public Collection<Benutzer> getBenutzerBgOrGemeinde() {
		return getBenutzersOfRoles(getBgAndGemeindeRoles());
	}

	@Nonnull
	@Override
	@PermitAll
	public Collection<Benutzer> getBenutzerBGorAdmin() {
		return getBenutzersOfRoles(getJugendamtRoles());
	}

	@Nonnull
	@Override
	@PermitAll
	public Collection<Benutzer> getBenutzerSCHorAdminSCH() {
		return getBenutzersOfRoles(getSchulamtRoles());
	}

	/**
	 * Gibt alle existierenden Benutzer mit den gewünschten Rollen zurueck.
	 * ¡Diese Methode filtert die Gemeinde über den angemeldeten Benutzer!
	 *
	 * @param roles Die besagten Rollen
	 * @return Liste aller Benutzern mit entsprechender Rolle aus der DB
	 */
	private Collection<Benutzer> getBenutzersOfRoles(List<UserRole> roles) {
		Benutzer currentBenutzer = getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"getBenutzersOfRole", "Non logged in user should never reach this"));

		List<Predicate> predicates = new ArrayList<>();

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);
		Join<Benutzer, Berechtigung> joinBerechtigungen = root.join(Benutzer_.berechtigungen);
		SetJoin<Berechtigung, Gemeinde> joinGemeinde =
			joinBerechtigungen.join(Berechtigung_.gemeindeList, JoinType.LEFT);
		query.select(root);

		predicates.add(cb.between(
			cb.literal(LocalDate.now()),
			joinBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			joinBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis)));
		predicates.add(joinBerechtigungen.get(Berechtigung_.role).in(roles));

		setGemeindeFilterForCurrentUser(currentBenutzer, joinGemeinde, predicates);

		query.where(predicates.toArray(NEW));
		query.distinct(true);

		return persistence.getCriteriaResults(query);
	}

	/**
	 * Gibt alle existierenden Benutzer mit den gewünschten Rollen zurueck.
	 * ¡Diese Methode filtert die Gemeinde über den Gemeinde-Parameter!
	 *
	 * @param roles Das Rollen Filter
	 * @param gemeinde Das Gemeinde Filter
	 * @return Liste aller Benutzern mit entsprechender Rolle aus der DB
	 */
	private Collection<Benutzer> getBenutzersOfRoles(@Nonnull List<UserRole> roles, @Nonnull Gemeinde gemeinde) {
		getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"getBenutzersOfRole", "Non logged in user should never reach this"));

		List<Predicate> predicates = new ArrayList<>();

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);

		Join<Benutzer, Berechtigung> joinBerechtigungen = root.join(Benutzer_.berechtigungen);
		Join<Berechtigung, Gemeinde> joinBerechtigungenGemeinde = joinBerechtigungen.join(Berechtigung_.gemeindeList);

		query.select(root);

		predicates.add(cb.between(
			cb.literal(LocalDate.now()),
			joinBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			joinBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis)));
		predicates.add(joinBerechtigungen.get(Berechtigung_.role).in(roles));
		predicates.add(cb.equal(joinBerechtigungenGemeinde.get(AbstractEntity_.id), gemeinde.getId()));

		query.where(predicates.toArray(NEW));
		query.distinct(true);

		return persistence.getCriteriaResults(query);
	}

	/**
	 * Gibt alle existierenden Benutzer einer Institution mit den gewünschten Rollen zurueck.
	 *
	 * @param roles Das Rollen Filter
	 * @param institution zum Filtern nach der Institution
	 * @return Liste aller Benutzern mit entsprechender Rolle aus der DB
	 */
	private Collection<Benutzer> getBenutzersOfRoles(@Nonnull List<UserRole> roles, @Nonnull Institution institution) {
		getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"getBenutzersOfRole", "Non logged in user should never reach this"));

		List<Predicate> predicates = new ArrayList<>();

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);

		Join<Benutzer, Berechtigung> joinBerechtigungen = root.join(Benutzer_.berechtigungen);

		query.select(root);

		predicates.add(cb.between(
			cb.literal(LocalDate.now()),
			joinBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			joinBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis)));
		predicates.add(joinBerechtigungen.get(Berechtigung_.role).in(roles));
		predicates.add(cb.equal(joinBerechtigungen.get(Berechtigung_.institution), institution));

		query.where(predicates.toArray(NEW));
		query.distinct(true);

		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	@Override
	@RolesAllowed(SUPER_ADMIN)
	public Collection<Benutzer> getGesuchsteller() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);
		Join<Benutzer, Berechtigung> joinBerechtigungen = root.join(Benutzer_.berechtigungen);
		query.select(root);

		Predicate predicateActive = cb.between(
			cb.literal(LocalDate.now()),
			joinBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			joinBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis));
		Predicate predicateRole = joinBerechtigungen.get(Berechtigung_.role).in(GESUCHSTELLER);
		query.where(predicateActive, predicateRole);
		query.orderBy(cb.asc(root.get(Benutzer_.vorname)), cb.asc(root.get(Benutzer_.nachname)));
		return persistence.getCriteriaResults(query);
	}

	@Override
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN, ADMIN_TS })
	public void removeBenutzer(@Nonnull String username) {
		requireNonNull(username);
		Benutzer benutzer = findBenutzer(username).orElseThrow(() -> new EbeguEntityNotFoundException(
			"removeBenutzer",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			username));

		authorizer.checkWriteAuthorization(benutzer);

		// Den Benutzer ausloggen und seine AuthBenutzer loeschen
		authService.logoutAndDeleteAuthorisierteBenutzerForUser(username);
		removeBerechtigungHistoryForBenutzer(benutzer);
		persistence.remove(benutzer);
	}

	private void removeBerechtigungHistoryForBenutzer(@Nonnull Benutzer benutzer) {
		Collection<BerechtigungHistory> histories = getBerechtigungHistoriesForBenutzer(benutzer);
		for (BerechtigungHistory history : histories) {
			persistence.remove(history);
		}
	}

	@Nonnull
	@Override
	@PermitAll
	public Optional<Benutzer> getCurrentBenutzer() {
		String username = null;
		if (principalBean != null) {
			final Principal principal = principalBean.getPrincipal();
			username = principal.getName();
		}
		if (StringUtils.isNotEmpty(username)) {
			return findBenutzer(username);
		}
		return Optional.empty();
	}

	@Override
	@PermitAll
	public Benutzer updateOrStoreUserFromIAM(@Nonnull Benutzer benutzer) {
		requireNonNull(benutzer.getExternalUUID());

		Optional<Benutzer> foundUserOptional = this.findBenutzerByExternalUUID(benutzer.getExternalUUID());

		if (foundUserOptional.isPresent()) {
			// Wir kennen den Benutzer schon: Es werden nur die readonly-Attribute neu von IAM uebernommen
			Benutzer foundUser = foundUserOptional.get();
			// Wir ueberpruefen, ob der Username sich geaendert hat
			if (!foundUser.getUsername().equals(benutzer.getUsername())) {
				LOG.warn("External User has new Username: ExternalUUID {}, old username {}, new username {}. "
						+ "Updating and setting Bemerkung!",
					benutzer.getExternalUUID(), foundUser.getUsername(), benutzer.getUsername());
				foundUser.addBemerkung("External User has new Username: ExternalUUID: " + benutzer.getExternalUUID() + ", old username: " + foundUser.getUsername() + ", new username " + benutzer.getUsername());
				foundUser.setUsername(benutzer.getUsername());
			}
			// den username ueberschreiben wir nicht!
			foundUser.setNachname(benutzer.getNachname());
			foundUser.setVorname(benutzer.getVorname());
			foundUser.setEmail(benutzer.getEmail());

			// Wir setzen den konfigurierten User als SUPER_ADMIN
			setSuperAdminRole(foundUser);

			return saveBenutzer(foundUser);
		}

		// Wir kennen den Benutzer noch nicht: Wir uebernehmen alles, setzen aber grundsätzlich die Rolle auf
		// GESUCHSTELLER
		Berechtigung berechtigung = new Berechtigung();
		berechtigung.setRole(GESUCHSTELLER);
		berechtigung.setInstitution(null);
		berechtigung.setTraegerschaft(null);
		berechtigung.setBenutzer(benutzer);
		benutzer.getBerechtigungen().clear();
		benutzer.getBerechtigungen().add(berechtigung);

		// Wir setzen den konfigurierten User als SUPER_ADMIN
		setSuperAdminRole(benutzer);

		return saveBenutzer(benutzer);
	}

	private void setSuperAdminRole(@Nonnull Benutzer benutzer) {
		// Wir setzen den konfigurierten User als SUPER_ADMIN
		String superuserMail = ebeguConfiguration.getSuperuserMail();
		if (superuserMail != null
			&& superuserMail.equalsIgnoreCase(benutzer.getEmail()) && benutzer.getRole() != UserRole.SUPER_ADMIN
		) {
			benutzer.setRole(UserRole.SUPER_ADMIN);
			benutzer.setInstitution(null);
			benutzer.setTraegerschaft(null);
			LOG.warn("Benutzer eingeloggt mit E-Mail {}: {}", benutzer.getEmail(), benutzer);
		}
	}

	@Override
	@PermitAll
	public Optional<Benutzer> findUserWithInvitationByEmail(@Nonnull Benutzer benutzer) {
		return findBenutzerByEmail(benutzer.getEmail())
			.filter(benutzerByEmail ->
				benutzerByEmail.getStatus() == BenutzerStatus.EINGELADEN
					&& benutzerByEmail.getExternalUUID() == null
			);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		ADMIN_MANDANT })
	public Benutzer sperren(@Nonnull String username) {
		Benutzer benutzerFromDB = findBenutzer(username)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"sperren",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				"GesuchId invalid: " + username));

		authorizer.checkWriteAuthorization(benutzerFromDB);

		benutzerFromDB.setStatus(BenutzerStatus.GESPERRT);
		int deletedAuthBenutzer = authService.logoutAndDeleteAuthorisierteBenutzerForUser(username);
		logSperreBenutzer(benutzerFromDB, deletedAuthBenutzer);

		return persistence.merge(benutzerFromDB);
	}

	private void logSperreBenutzer(@Nonnull Benutzer benutzer, int deletedAuthBenutzer) {
		LOG.info(
			"Setze Benutzer auf GESPERRT: {} / Eingeloggt: {} / Lösche {} Eintraege aus der AuthorisierteBenutzer"
				+ " Tabelle",
			benutzer.getUsername(),
			principalBean.getBenutzer().getUsername(),
			deletedAuthBenutzer);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		ADMIN_MANDANT })
	public Benutzer reaktivieren(@Nonnull String username) {
		Benutzer benutzerFromDB = findBenutzer(username)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"reaktivieren",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				"GesuchId invalid: " + username));

		authorizer.checkWriteAuthorization(benutzerFromDB);

		benutzerFromDB.setStatus(findLastNotGesperrtStatus(benutzerFromDB));
		logReaktivierenBenutzer(benutzerFromDB);

		return persistence.merge(benutzerFromDB);
	}

	private void logReaktivierenBenutzer(Benutzer benutzerFromDB) {
		LOG.info(
			"Reaktiviere Benutzer: {} / Eingeloggt: {}",
			benutzerFromDB.getUsername(),
			principalBean.getBenutzer().getUsername());
	}

	private void prepareBenutzerForSave(@Nonnull Benutzer benutzer, boolean currentBerechtigungChanged) {
		List<Berechtigung> allSortedBerechtigungen = new LinkedList<>(benutzer.getBerechtigungen());
		allSortedBerechtigungen.sort(Comparator.comparing(o -> o.getGueltigkeit().getGueltigAb()));

		final Berechtigung currentBerechtigung = allSortedBerechtigungen.get(0);

		handleGueltigkeitCurrentBerechtigung(allSortedBerechtigungen,
			currentBerechtigung, currentBerechtigungChanged);

		for (Berechtigung berechtigung : allSortedBerechtigungen) {
			prepareBerechtigungForSave(berechtigung);
		}

		// Ausloggen nur, wenn die aktuelle Berechtigung geändert hat
		if (currentBerechtigungChanged) {
			LOG.info(
				"Aktuelle Berechtigung des Benutzers {} hat geändert, Benutzer wird ausgeloggt",
				benutzer.getUsername());
			authService.logoutAndDeleteAuthorisierteBenutzerForUser(benutzer.getUsername());
		}
	}

	/**
	 * If there are future Berechtigungen it sets the gueltigBis of the currentBerechtigung to one day before the
	 * gueltigAb of the futureBerechtigung.
	 * For no futureBerechtigungen it sets the gueltigBis of the currentBerechtigung to END_OF_TIME
	 * If the currentBerechtigung changed it sets the gueltigAb of the currentBerechtigung to now()
	 */
	private void handleGueltigkeitCurrentBerechtigung(
		@Nonnull List<Berechtigung> allSortedBerechtigungen,
		@Nonnull Berechtigung currentBerechtigung,
		boolean currentBerechtigungChanged) {

		currentBerechtigung.getGueltigkeit().setGueltigBis(
			allSortedBerechtigungen.size() > 1 ?
				allSortedBerechtigungen.get(1).getGueltigkeit().getGueltigAb().minusDays(1) :
				Constants.END_OF_TIME
		);

		if (currentBerechtigungChanged) {
			currentBerechtigung.getGueltigkeit().setGueltigAb(LocalDate.now());
		}
	}

	private void prepareBerechtigungForSave(@Nonnull Berechtigung berechtigung) {
		// Es darf nur eine Institution gesetzt sein, wenn die Rolle INSTITUTION ist
		if (EnumUtil.isNoneOf(
			berechtigung.getRole(),
			UserRole.ADMIN_INSTITUTION,
			UserRole.SACHBEARBEITER_INSTITUTION)) {
			berechtigung.setInstitution(null);
		}
		// Es darf nur eine Trägerschaft gesetzt sein, wenn die Rolle TRAEGERSCHAFT ist
		if (EnumUtil.isNoneOf(
			berechtigung.getRole(),
			UserRole.ADMIN_TRAEGERSCHAFT,
			UserRole.SACHBEARBEITER_TRAEGERSCHAFT)) {
			berechtigung.setTraegerschaft(null);
		}
	}

	/**
	 * Use this function to retrieve any Superadmin from the DB. It will randomly take the first one it finds
	 */
	private Optional<Benutzer> loadAnySuperAdmin() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);
		Join<Benutzer, Berechtigung> joinBerechtigungen = root.join(Benutzer_.berechtigungen);
		query.select(root);

		Predicate rolePredicate = joinBerechtigungen.get(Berechtigung_.role).in(UserRole.SUPER_ADMIN);
		query.where(rolePredicate);

		return persistence.getCriteriaResults(query).stream().findFirst();
	}

	@Nonnull
	@Override
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN, ADMIN_TS, ADMIN_INSTITUTION, ADMIN_TRAEGERSCHAFT,
		ADMIN_MANDANT, REVISOR })
	public Pair<Long, List<Benutzer>> searchBenutzer(@Nonnull BenutzerTableFilterDTO benutzerTableFilterDto) {
		Long countResult = searchBenutzer(benutzerTableFilterDto, SearchMode.COUNT).getLeft();

		if (countResult.equals(0L)) {    // no result found
			return new ImmutablePair<>(0L, Collections.emptyList());
		}

		Pair<Long, List<Benutzer>> searchResult = searchBenutzer(benutzerTableFilterDto, SearchMode.SEARCH);
		return new ImmutablePair<>(countResult, searchResult.getRight());
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN, ADMIN_TS })
	private Pair<Long, List<Benutzer>> searchBenutzer(
		@Nonnull BenutzerTableFilterDTO benutzerTableFilterDTO,
		@Nonnull SearchMode mode) {

		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		@SuppressWarnings("rawtypes") // Je nach Abfrage ist es String oder Long
			CriteriaQuery query = SearchUtil.getQueryForSearchMode(cb, mode, "searchBenutzer");

		// Construct from-clause
		@SuppressWarnings("unchecked") // Je nach Abfrage ist das Query String oder Long
			Root<Benutzer> root = query.from(Benutzer.class);
		Join<Benutzer, Berechtigung> currentBerechtigungJoin = root.join(Benutzer_.berechtigungen);
		Join<Berechtigung, Institution> institutionJoin =
			currentBerechtigungJoin.join(Berechtigung_.institution, JoinType.LEFT);
		Join<Berechtigung, Traegerschaft> traegerschaftJoin =
			currentBerechtigungJoin.join(Berechtigung_.traegerschaft, JoinType.LEFT);
		SetJoin<Berechtigung, Gemeinde> gemeindeSetJoin =
			currentBerechtigungJoin.join(Berechtigung_.gemeindeList, JoinType.LEFT);

		List<Predicate> predicates = new ArrayList<>();

		// General role based predicates
		Benutzer user =
			getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException("searchBenutzer", "No User is logged "
				+ "in"));

		if (!principalBean.isCallerInRole(UserRole.SUPER_ADMIN)) {
			// Not SuperAdmin users are allowed to see all users of their mandant
			setMandantFilterForCurrentUser(user, root, cb, predicates);

			// They cannot see superadmin users
			setSuperAdminFilterForCurrentUser(user, currentBerechtigungJoin, predicates);

			setGemeindeFilterForCurrentUser(user, gemeindeSetJoin, predicates);

			setRoleFilterForCurrentUser(user, currentBerechtigungJoin, predicates);
		}

		if (principalBean.isCallerInRole(UserRole.ADMIN_INSTITUTION)) {
			setInstitutionFilterForCurrentUser(user, currentBerechtigungJoin, cb, predicates);
		}

		if (principalBean.isCallerInRole(UserRole.ADMIN_TRAEGERSCHAFT)) {
			setTraegerschaftFilterForCurrentUser(user, currentBerechtigungJoin, cb, predicates);
		}

		//prepare predicates from table filters
		if (benutzerTableFilterDTO.getSearch() != null) {
			BenutzerPredicateObjectDTO predicateObjectDto = benutzerTableFilterDTO.getSearch().getPredicateObject();
			// username
			if (predicateObjectDto.getUsername() != null) {
				Expression<String> expression = root.get(Benutzer_.username).as(String.class);
				String value = SearchUtil.withWildcards(predicateObjectDto.getUsername());
				predicates.add(cb.like(expression, value));
			}
			// vorname
			if (predicateObjectDto.getVorname() != null) {
				Expression<String> expression = root.get(Benutzer_.vorname).as(String.class);
				String value = SearchUtil.withWildcards(predicateObjectDto.getVorname());
				predicates.add(cb.like(expression, value));
			}
			// nachname
			if (predicateObjectDto.getNachname() != null) {
				Expression<String> expression = root.get(Benutzer_.nachname).as(String.class);
				String value = SearchUtil.withWildcards(predicateObjectDto.getNachname());
				predicates.add(cb.like(expression, value));
			}
			// email
			if (predicateObjectDto.getEmail() != null) {
				Expression<String> expression = root.get(Benutzer_.email).as(String.class);
				String value = SearchUtil.withWildcards(predicateObjectDto.getEmail());
				predicates.add(cb.like(expression, value));
			}
			// role
			if (predicateObjectDto.getRole() != null) {
				predicates.add(cb.equal(
					currentBerechtigungJoin.get(Berechtigung_.role),
					predicateObjectDto.getRole()));
			}
			// roleGueltigBis
			if (predicateObjectDto.getRoleGueltigBis() != null) {
				try {
					LocalDate searchDate =
						LocalDate.parse(predicateObjectDto.getRoleGueltigBis(), Constants.DATE_FORMATTER);
					predicates.add(cb.equal(currentBerechtigungJoin.get(AbstractDateRangedEntity_.gueltigkeit)
						.get(DateRange_.gueltigBis), searchDate));
				} catch (DateTimeParseException e) {
					// Kein gueltiges Datum. Es kann kein Gesuch geben, welches passt. Wir geben leer zurueck
					return new ImmutablePair<>(0L, Collections.emptyList());
				}
			}
			// gemeinde
			if (predicateObjectDto.getGemeinde() != null) {
				predicates.add(cb.equal(gemeindeSetJoin.get(Gemeinde_.name), predicateObjectDto.getGemeinde()));
			}
			// institution
			if (predicateObjectDto.getInstitution() != null) {
				predicates.add(cb.equal(institutionJoin.get(Institution_.name), predicateObjectDto.getInstitution()));
			}
			// traegerschaft
			if (predicateObjectDto.getTraegerschaft() != null) {
				predicates.add(cb.equal(
					traegerschaftJoin.get(Traegerschaft_.name),
					predicateObjectDto.getTraegerschaft()));
			}
			// gesperrt
			if (predicateObjectDto.getStatus() != null) {
				predicates.add(cb.equal(root.get(Benutzer_.status), predicateObjectDto.getStatus()));
			}
		}
		// Construct the select- and where-clause
		switch (mode) {
		case SEARCH:
			//noinspection unchecked // Je nach Abfrage ist das Query String oder Long
			query.select(root.get(AbstractEntity_.id));
			if (!predicates.isEmpty()) {
				query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
			}
			constructOrderByClause(
				benutzerTableFilterDTO,
				cb,
				query,
				root,
				currentBerechtigungJoin,
				institutionJoin,
				traegerschaftJoin,
				gemeindeSetJoin);
			break;
		case COUNT:
			//noinspection unchecked // Je nach Abfrage ist das Query String oder Long
			query.select(cb.countDistinct(root.get(AbstractEntity_.id)));
			if (!predicates.isEmpty()) {
				query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
			}
			break;
		}

		// Prepare and execute the query and build the result
		Pair<Long, List<Benutzer>> result = null;
		switch (mode) {
		case SEARCH:
			//select all ids in order, may contain duplicates
			List<String> benutzerIds = persistence.getCriteriaResults(query);
			List<Benutzer> pagedResult;
			if (benutzerTableFilterDTO.getPagination() != null) {
				int firstIndex = benutzerTableFilterDTO.getPagination().getStart();
				Integer maxresults = benutzerTableFilterDTO.getPagination().getNumber();
				List<String> orderedIdsToLoad =
					SearchUtil.determineDistinctIdsToLoad(benutzerIds, firstIndex, maxresults);
				pagedResult = findBenutzer(orderedIdsToLoad);
			} else {
				pagedResult = findBenutzer(benutzerIds);
			}
			result = new ImmutablePair<>(null, pagedResult);
			break;
		case COUNT:
			Long count = (Long) persistence.getCriteriaSingleResult(query);
			result = new ImmutablePair<>(count, null);
			break;
		}
		return result;
	}

	private void constructOrderByClause(
		@Nonnull BenutzerTableFilterDTO benutzerTableFilterDto, CriteriaBuilder cb, CriteriaQuery query,
		Root<Benutzer> root,
		Join<Benutzer, Berechtigung> currentBerechtigung,
		Join<Berechtigung, Institution> institution,
		Join<Berechtigung, Traegerschaft> traegerschaft,
		@Nonnull SetJoin<Berechtigung, Gemeinde> gemeindeSetJoin) {
		Expression<?> expression;
		if (benutzerTableFilterDto.getSort() != null && benutzerTableFilterDto.getSort().getPredicate() != null) {
			switch (benutzerTableFilterDto.getSort().getPredicate()) {
			case "username":
				expression = root.get(Benutzer_.username);
				break;
			case "vorname":
				expression = root.get(Benutzer_.vorname);
				break;
			case "nachname":
				expression = root.get(Benutzer_.nachname);
				break;
			case "email":
				expression = root.get(Benutzer_.email);
				break;
			case "role":
				expression = currentBerechtigung.get(Berechtigung_.role);
				break;
			case "roleGueltigBis":
				expression = currentBerechtigung.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis);
				break;
			case "gemeinde":
				// Die Gemeinden sind eine Liste innerhalb der Liste (also des Tabelleneintrages).
				// Berechtigungen ohne Gemeinde sollen egal wie sortiert ist am Schluss kommen!
				if (benutzerTableFilterDto.getSort().getReverse()) {
					expression = cb.selectCase().when(gemeindeSetJoin.isNull(), "ZZZZ")
						.otherwise(gemeindeSetJoin.get(Gemeinde_.name));
				} else {
					expression = cb.selectCase().when(gemeindeSetJoin.isNull(), "0000")
						.otherwise(gemeindeSetJoin.get(Gemeinde_.name));
				}
				break;
			case "institution":
				expression = institution.get(Institution_.name);
				break;
			case "traegerschaft":
				expression = traegerschaft.get(Traegerschaft_.name);
				break;
			case "status":
				expression = root.get(Benutzer_.status);
				break;
			default:
				LOG.warn(
					"Using default sort by Timestamp mutiert because there is no specific clause for predicate {}",
					benutzerTableFilterDto.getSort().getPredicate());
				expression = root.get(AbstractEntity_.timestampMutiert);
				break;
			}
			query.orderBy(benutzerTableFilterDto.getSort().getReverse() ? cb.asc(expression) : cb.desc(expression));
		} else {
			// Default sort when nothing is choosen
			expression = root.get(AbstractEntity_.timestampMutiert);
			query.orderBy(cb.desc(expression));
		}
	}

	private List<Benutzer> findBenutzer(@Nonnull List<String> benutzerIds) {
		if (!benutzerIds.isEmpty()) {
			final CriteriaBuilder cb = persistence.getCriteriaBuilder();
			final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
			Root<Benutzer> root = query.from(Benutzer.class);
			Predicate predicate = root.get(AbstractEntity_.id).in(benutzerIds);
			query.where(predicate);
			//reduce to unique Benutzer
			List<Benutzer> listWithDuplicates = persistence.getCriteriaResults(query);
			LinkedHashSet<Benutzer> setOfBenutzer = new LinkedHashSet<>();
			//richtige reihenfolge beibehalten
			for (String userId : benutzerIds) {
				listWithDuplicates.stream()
					.filter(benutzer -> benutzer.getId().equals(userId))
					.findFirst()
					.ifPresent(setOfBenutzer::add);
			}
			return new ArrayList<>(setOfBenutzer);
		}
		return Collections.emptyList();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed(SUPER_ADMIN)
	public int handleAbgelaufeneRollen(@Nonnull LocalDate stichtag) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);
		Join<Benutzer, Berechtigung> currentBerechtigung = root.join(Benutzer_.berechtigungen);
		Predicate predicateAbgelaufen =
			cb.lessThan(
				currentBerechtigung.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis),
				stichtag);
		query.where(predicateAbgelaufen);
		List<Benutzer> userMitAbgelaufenerRolle = persistence.getCriteriaResults(query);

		for (Benutzer benutzer : userMitAbgelaufenerRolle) {
			List<Berechtigung> abgelaufeneBerechtigungen = new ArrayList<>();
			for (Berechtigung berechtigung : benutzer.getBerechtigungen()) {
				if (berechtigung.isAbgelaufen()) {
					abgelaufeneBerechtigungen.add(berechtigung);
				}
			}
			try {
				Berechtigung aktuelleBerechtigung = getAktuellGueltigeBerechtigungFuerBenutzer(benutzer);
				persistence.merge(aktuelleBerechtigung);
			} catch (NoResultException nre) {
				// Sonderfall: Die letzte Berechtigung ist abgelaufen. Wir erstellen sofort eine neue anschliessende
				// Berechtigung als Gesuchsteller
				Berechtigung futureGesuchstellerBerechtigung =
					createFutureBerechtigungAsGesuchsteller(LocalDate.now(), benutzer);
				persistence.persist(futureGesuchstellerBerechtigung);
			}
			// Die abgelaufene Rolle löschen
			for (Berechtigung abgelaufeneBerechtigung : abgelaufeneBerechtigungen) {
				LOG.info("... Benutzerrolle ist abgelaufen: {}, war: {}, abgelaufen: {}", benutzer.getUsername(),
					abgelaufeneBerechtigung.getRole(), abgelaufeneBerechtigung.getGueltigkeit().getGueltigBis());
				benutzer.getBerechtigungen().remove(abgelaufeneBerechtigung);
				persistence.merge(benutzer);
				removeBerechtigung(abgelaufeneBerechtigung);
			}

		}
		return userMitAbgelaufenerRolle.size();
	}

	private Berechtigung createFutureBerechtigungAsGesuchsteller(LocalDate startDatum, Benutzer benutzer) {
		Berechtigung futureGesuchstellerBerechtigung = new Berechtigung();
		futureGesuchstellerBerechtigung.getGueltigkeit().setGueltigAb(startDatum);
		futureGesuchstellerBerechtigung.getGueltigkeit().setGueltigBis(Constants.END_OF_TIME);
		futureGesuchstellerBerechtigung.setRole(GESUCHSTELLER);
		futureGesuchstellerBerechtigung.setBenutzer(benutzer);
		return futureGesuchstellerBerechtigung;
	}

	@Nonnull
	private Berechtigung getAktuellGueltigeBerechtigungFuerBenutzer(@Nonnull Benutzer benutzer) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Berechtigung> query = cb.createQuery(Berechtigung.class);
		Root<Berechtigung> root = query.from(Berechtigung.class);

		ParameterExpression<Benutzer> benutzerParam = cb.parameter(Benutzer.class, "benutzer");
		ParameterExpression<LocalDate> dateParam = cb.parameter(LocalDate.class, "date");

		Predicate predicateBenutzer = cb.equal(root.get(Berechtigung_.benutzer), benutzerParam);
		Predicate predicateZeitraum = cb.between(
			dateParam,
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis));

		query.where(predicateBenutzer, predicateZeitraum);

		TypedQuery<Berechtigung> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(dateParam, LocalDate.now());
		q.setParameter(benutzerParam, benutzer);
		List<Berechtigung> resultList = q.getResultList();

		if (resultList.isEmpty()) {
			throw new NoResultException("No Berechtigung found for Benutzer" + benutzer.getUsername());
		}
		if (resultList.size() > 1) {
			throw new NonUniqueResultException("More than one Berechtigung found for Benutzer "
				+ benutzer.getUsername());
		}
		return resultList.get(0);
	}

	@Nonnull
	@Override
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN, ADMIN_TS })
	public Optional<Berechtigung> findBerechtigung(@Nonnull String id) {
		requireNonNull(id, "id muss gesetzt sein");
		return Optional.ofNullable(persistence.find(Berechtigung.class, id));
	}

	private void removeBerechtigung(@Nonnull Berechtigung berechtigung) {
		authService.logoutAndDeleteAuthorisierteBenutzerForUser(berechtigung.getBenutzer().getUsername());
		persistence.remove(berechtigung);
	}

	@Override
	@PermitAll
	public void saveBerechtigungHistory(@Nonnull Berechtigung berechtigung, boolean deleted) {
		BerechtigungHistory newBerechtigungsHistory = new BerechtigungHistory(berechtigung, deleted);
		newBerechtigungsHistory.setTimestampErstellt(LocalDateTime.now());
		String userMutiert =
			berechtigung.getUserMutiert() != null ? berechtigung.getUserMutiert() : Constants.SYSTEM_USER_USERNAME;
		newBerechtigungsHistory.setUserErstellt(userMutiert);
		persistence.persist(newBerechtigungsHistory);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		ADMIN_MANDANT, REVISOR })
	public Collection<BerechtigungHistory> getBerechtigungHistoriesForBenutzer(@Nonnull Benutzer benutzer) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<BerechtigungHistory> query = cb.createQuery(BerechtigungHistory.class);
		Root<BerechtigungHistory> root = query.from(BerechtigungHistory.class);

		ParameterExpression<String> benutzerParam = cb.parameter(String.class, "username");
		Predicate predicateBenutzer = cb.equal(root.get(BerechtigungHistory_.username), benutzerParam);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));
		query.where(predicateBenutzer);

		TypedQuery<BerechtigungHistory> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(benutzerParam, benutzer.getUsername());
		return q.getResultList();
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		ADMIN_MANDANT })
	public boolean isBenutzerDefaultBenutzerOfAnyGemeinde(@Nonnull String username) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<GemeindeStammdaten> query = cb.createQuery(GemeindeStammdaten.class);
		Root<GemeindeStammdaten> root = query.from(GemeindeStammdaten.class);

		ParameterExpression<String> benutzerParam = cb.parameter(String.class, "username");
		Predicate predicateDefaultBG =
			cb.equal(root.get(GemeindeStammdaten_.defaultBenutzerBG).get(Benutzer_.username), benutzerParam);
		Predicate predicateDefaultTS =
			cb.equal(root.get(GemeindeStammdaten_.defaultBenutzerTS).get(Benutzer_.username), benutzerParam);
		Predicate anyDefault = cb.or(predicateDefaultBG, predicateDefaultTS);
		query.where(anyDefault);

		TypedQuery<GemeindeStammdaten> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(benutzerParam, username);
		return !q.getResultList().isEmpty();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void deleteExternalUUIDInNewTransaction(@Nonnull String id) {
		Optional<Benutzer> benutzerOptional = findBenutzerById(id);
		if (benutzerOptional.isPresent()) {
			Benutzer benutzer = benutzerOptional.get();
			// Es gelten dieselben Regeln wie beim Loeschen
			authorizer.checkWriteAuthorization(benutzer);
			if (!isBenutzerDeleteable(benutzer)) {
				return;
			}
			LOG.warn("ExternalUUID von Benutzer wird gelöscht: {}", benutzer);
			benutzer.addBemerkung("ExternalUUID " + benutzer.getExternalUUID() + " gelöscht");
			benutzer.setExternalUUID(null);
			persistence.merge(benutzer);
		}
	}

	@Override
	public String createInvitationLink(
		@Nonnull Benutzer eingeladener,
		@Nonnull Einladung einladung
	) {
		return ebeguConfiguration.isClientUsingHTTPS() ? "https://" : "http://"
			+ ebeguConfiguration.getHostname()
			+ "/einladung?typ=" + einladung.getEinladungTyp()
			+ einladung.getEinladungRelatedObjectId().map(entityId -> "&entityid=" + entityId).orElse("")
			+ "&userid=" + eingeladener.getId();
	}

	private boolean isBenutzerDeleteable(@Nonnull Benutzer benutzer) {
		if (benutzer.getRole() == UserRole.GESUCHSTELLER) {
			// Gesuchsteller darf noch kein Dossier haben
			Optional<Fall> fallOptional = fallService.findFallByBesitzer(benutzer);
			if (fallOptional.isPresent()) {
				Fall fall = fallOptional.get();
				if (!gesuchService.getAllGesuchIDsForFall(fall.getId()).isEmpty()) {
					return false;
				}
			}
		} else {
			// Benutzer mit erhöhten Rechten darf die Einladung noch nicht angenommen haben
			if (benutzer.getStatus() != BenutzerStatus.EINGELADEN) {
				return false;
			}
		}
		// Es darf keine Mitteilungen von oder an diesen Benutzer geben
		if (mitteilungService.hasBenutzerAnyMitteilungenAsSenderOrEmpfaenger(benutzer)) {
			return false;
		}
		// Der Benutzer darf nirgends als Default-Benutzer gesetzt sein
		if (isBenutzerDefaultBenutzerOfAnyGemeinde(benutzer.getUsername())) {
			return false;
		}
		return true;
	}

	private BenutzerStatus findLastNotGesperrtStatus(Benutzer benutzer) {
		Collection<BerechtigungHistory> history = getBerechtigungHistoriesForBenutzer(benutzer);
		BerechtigungHistory lastNotGesperrtHistory = history.stream()
			.filter(x -> x.getStatus() != BenutzerStatus.GESPERRT)
			.findFirst()
			.get();

		return lastNotGesperrtHistory.getStatus();
	}
}
