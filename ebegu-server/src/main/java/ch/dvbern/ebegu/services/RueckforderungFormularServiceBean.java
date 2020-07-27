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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.entities.RueckforderungFormular_;
import ch.dvbern.ebegu.entities.RueckforderungMitteilung;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.RueckforderungInstitutionTyp;
import ch.dvbern.ebegu.enums.RueckforderungStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.MailException;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.interceptors.UpdateRueckfordFormStatusInterceptor;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.lang.StringUtils;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Stateless
@Local(RueckforderungFormularService.class)
public class RueckforderungFormularServiceBean extends AbstractBaseService implements RueckforderungFormularService {

	@Inject
	private Persistence persistence;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Inject
	private MailService mailService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private Authorizer authorizer;

	@Nonnull
	@Override
	@RolesAllowed(SUPER_ADMIN)
	public List<RueckforderungFormular> initializeRueckforderungFormulare() {

		Collection<InstitutionStammdaten> institutionenStammdatenCollection = institutionStammdatenService.getAllInstitutionStammdaten();
		Collection<RueckforderungFormular> rueckforderungFormularCollection = getAllRueckforderungFormulare();

		List<RueckforderungFormular> rueckforderungFormulare = new ArrayList<>();
		for (InstitutionStammdaten institutionStammdaten : institutionenStammdatenCollection) {
			// neues Formular erstellen falls es sich un eine kita oder TFO handelt und noch kein Formular existiert
			if ((institutionStammdaten.getBetreuungsangebotTyp() == BetreuungsangebotTyp.KITA ||
				institutionStammdaten.getBetreuungsangebotTyp() == BetreuungsangebotTyp.TAGESFAMILIEN) &&
				!isFormularExisting(institutionStammdaten, rueckforderungFormularCollection)
				&& institutionStammdaten.getInstitutionStammdatenBetreuungsgutscheine() != null
				&& institutionStammdaten.getInstitutionStammdatenBetreuungsgutscheine().getIban() != null) {

				RueckforderungFormular formular = new RueckforderungFormular();
				formular.setInstitutionStammdaten(institutionStammdaten);
				formular.setStatus(RueckforderungStatus.NEU);
				rueckforderungFormulare.add(createRueckforderungFormular(formular));
			}
		}
		return rueckforderungFormulare;
	}

	/**
	 * Falls in der Liste der Rückforderungsformulare die Institution bereits existiert, wird true zurückgegeben
	 */
	private boolean isFormularExisting(@Nonnull InstitutionStammdaten stammdaten,
		@Nonnull Collection<RueckforderungFormular> rueckforderungFormularCollection
	) {
		List<RueckforderungFormular> filteredFormulare = rueckforderungFormularCollection
			.stream()
			.filter(formular -> formular.getInstitutionStammdaten().getId().equals(stammdaten.getId()))
			.collect(Collectors.toList());
		return !filteredFormulare.isEmpty();
	}

	@Nonnull
	@Override
	@RolesAllowed(SUPER_ADMIN)
	public RueckforderungFormular createRueckforderungFormular(@Nonnull RueckforderungFormular rueckforderungFormular) {
		authorizer.checkWriteAuthorization(rueckforderungFormular);
		return persistence.persist(rueckforderungFormular);
	}

	@Nonnull
	private Collection<RueckforderungFormular> getAllRueckforderungFormulare(){
		return criteriaQueryHelper.getAll(RueckforderungFormular.class);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, ADMIN_INSTITUTION, SACHBEARBEITER_MANDANT, SACHBEARBEITER_INSTITUTION ,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT })
	public List<RueckforderungFormular> getRueckforderungFormulareForCurrentBenutzer() {
		Collection<RueckforderungFormular> allRueckforderungFormulare = getAllRueckforderungFormulare();
		Benutzer currentBenutzer = principalBean.getBenutzer();
		if (currentBenutzer.getRole().isRoleMandant() || currentBenutzer.getRole().isSuperadmin()){
			return new ArrayList<>(allRueckforderungFormulare);
		}
		Collection<Institution> institutionenCurrentBenutzer =
			institutionService.getInstitutionenEditableForCurrentBenutzer(false);

		return allRueckforderungFormulare.stream().filter(formular -> {
			for (Institution institution : institutionenCurrentBenutzer) {
				if (institution.getId().equals(formular.getInstitutionStammdaten().getInstitution().getId())) {
					return true;
				}
			}
			return false;
		}).collect(Collectors.toList());
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, ADMIN_INSTITUTION, SACHBEARBEITER_MANDANT, SACHBEARBEITER_INSTITUTION,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT })
	@Interceptors(UpdateRueckfordFormStatusInterceptor.class)
	public Optional<RueckforderungFormular> findRueckforderungFormular(@Nonnull String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		RueckforderungFormular rueckforderungFormular = persistence.find(RueckforderungFormular.class, id);
		authorizer.checkReadAuthorization(rueckforderungFormular);
		return Optional.ofNullable(rueckforderungFormular);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, ADMIN_INSTITUTION, SACHBEARBEITER_MANDANT, SACHBEARBEITER_INSTITUTION,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT })
	public RueckforderungFormular save(@Nonnull RueckforderungFormular rueckforderungFormular) {
		Objects.requireNonNull(rueckforderungFormular);
		authorizer.checkWriteAuthorization(rueckforderungFormular);
		final RueckforderungFormular mergedRueckforderungFormular = persistence.merge(rueckforderungFormular);
		return mergedRueckforderungFormular;
	}

	private RueckforderungFormular saveWithoutAuthCheck(@Nonnull RueckforderungFormular rueckforderungFormular) {
		Objects.requireNonNull(rueckforderungFormular);
		final RueckforderungFormular mergedRueckforderungFormular = persistence.merge(rueckforderungFormular);
		return mergedRueckforderungFormular;
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, ADMIN_INSTITUTION, SACHBEARBEITER_MANDANT, SACHBEARBEITER_INSTITUTION,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT })
	public RueckforderungFormular saveAndChangeStatusIfNecessary(@Nonnull RueckforderungFormular rueckforderungFormular) {
		Objects.requireNonNull(rueckforderungFormular);
		authorizer.checkWriteAuthorization(rueckforderungFormular);
		changeStatusAndCopyFields(rueckforderungFormular);
		return saveWithoutAuthCheck(rueckforderungFormular);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, ADMIN_INSTITUTION, SACHBEARBEITER_MANDANT, SACHBEARBEITER_INSTITUTION})
	public Collection<RueckforderungFormular> getRueckforderungFormulareByStatus(@Nonnull List<RueckforderungStatus> status) {
		Objects.requireNonNull(status.get(0), "Mindestens ein Status muss angegeben werden");
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<RueckforderungFormular> query = cb.createQuery(RueckforderungFormular.class);

		final Root<RueckforderungFormular> root = query.from(RueckforderungFormular.class);

		Predicate predicateStatus = root.get(RueckforderungFormular_.status).in(status);
		query.where(predicateStatus);
		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public RueckforderungFormular addMitteilung(
		@Nonnull RueckforderungFormular formular,
		@Nonnull RueckforderungMitteilung mitteilung
	) {
		authorizer.checkReadAuthorization(formular);
		formular.addRueckforderungMitteilung(mitteilung);
		return persistence.persist(formular);
	}

	@Override
	@RolesAllowed(SUPER_ADMIN)
	public void initializePhase2() {
		//set Application Properties zu true
		applicationPropertyService.saveOrUpdateApplicationProperty(ApplicationPropertyKey.KANTON_NOTVERORDNUNG_PHASE_2_AKTIV, "true");
		//get alle Ruckforderungsformular, check status and changed if needed
		ArrayList<RueckforderungStatus> statusGeprueftStufe1 = new ArrayList<>();
		statusGeprueftStufe1.add(RueckforderungStatus.GEPRUEFT_STUFE_1);
		Collection<RueckforderungFormular> formulareWithStatusGeprueftStufe1 =
			getRueckforderungFormulareByStatus(statusGeprueftStufe1);
		for (RueckforderungFormular formular : formulareWithStatusGeprueftStufe1) {
			authorizer.checkWriteAuthorization(formular);
			saveAndChangeStatusIfNecessary(formular);
		}
	}

	@Nonnull
	@Override
	@RolesAllowed({SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public RueckforderungFormular resetStatusToInBearbeitungInstitutionPhase2(@Nonnull String id) {
		final RueckforderungFormular rueckforderungFormular = findRueckforderungFormular(id)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"resetStatusToInBearbeitungInstitutionPhase2",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				"Rueckfordungsformular invalid: " + id));
		authorizer.checkWriteAuthorization(rueckforderungFormular);
		rueckforderungFormular.setStatus(RueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_2);
		return saveWithoutAuthCheck(rueckforderungFormular);
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	private void changeStatusAndCopyFields(@Nonnull RueckforderungFormular rueckforderungFormular) {
		authorizer.checkWriteAuthorization(rueckforderungFormular);
		switch (rueckforderungFormular.getStatus()) {
		case EINGELADEN: {
			if (principalBean.isCallerInAnyOfRole(UserRole.getInstitutionTraegerschaftRoles())) {
				rueckforderungFormular.setStatus(RueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_1);
			}
			break;
		}
		case IN_BEARBEITUNG_INSTITUTION_STUFE_1: {
			if (principalBean.isCallerInAnyOfRole(UserRole.getInstitutionTraegerschaftRoles())) {
				rueckforderungFormular.setStatus(RueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_1);
				rueckforderungFormular.setStufe1KantonKostenuebernahmeAnzahlStunden(rueckforderungFormular.getStufe1InstitutionKostenuebernahmeAnzahlStunden());
				rueckforderungFormular.setStufe1KantonKostenuebernahmeAnzahlTage(rueckforderungFormular.getStufe1InstitutionKostenuebernahmeAnzahlTage());
				rueckforderungFormular.setStufe1KantonKostenuebernahmeBetreuung(rueckforderungFormular.getStufe1InstitutionKostenuebernahmeBetreuung());
			}
			break;
		}
		case IN_BEARBEITUNG_INSTITUTION_STUFE_2: {
			if (principalBean.isCallerInAnyOfRole(UserRole.getInstitutionTraegerschaftRoles())) {
				RueckforderungStatus nextStatus = rueckforderungFormular.getStatus();
				if (rueckforderungFormular.getInstitutionTyp() == RueckforderungInstitutionTyp.OEFFENTLICH) {
					nextStatus = RueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2;
				} else {
					// Definitiv ist es, wenn es entweder gar nicht beantragt wurde, oder schon verfuegt ist
					if (rueckforderungFormular.isKurzarbeitProzessBeendet() && rueckforderungFormular.isCoronaErwerbsersatzProzessBeendet()) {
						nextStatus = RueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2;
					} else {
						nextStatus = RueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2_PROVISORISCH;
						// Wir muessen uns merken, dass das Formular hier nochmals geprueft werden musste, damit wir
						// spaeter die richtige Confirmation Message anzeigen koennen
						rueckforderungFormular.setHasBeenProvisorisch(true);
					}
				}
				rueckforderungFormular.setStatus(nextStatus);
				rueckforderungFormular.setStufe2KantonKostenuebernahmeAnzahlStunden(rueckforderungFormular.getStufe2InstitutionKostenuebernahmeAnzahlStunden());
				rueckforderungFormular.setStufe2KantonKostenuebernahmeAnzahlTage(rueckforderungFormular.getStufe2InstitutionKostenuebernahmeAnzahlTage());
				rueckforderungFormular.setStufe2KantonKostenuebernahmeBetreuung(rueckforderungFormular.getStufe2InstitutionKostenuebernahmeBetreuung());
			}
			break;
		}
		case IN_PRUEFUNG_KANTON_STUFE_1: {
			if (principalBean.isCallerInAnyOfRole(UserRole.getMandantSuperadminRoles())) {
				rueckforderungFormular.setStatus(RueckforderungStatus.GEPRUEFT_STUFE_1);
				// Zahlungen ausloesen
				rueckforderungFormular.setStufe1FreigabeBetrag(rueckforderungFormular.calculateFreigabeBetragStufe1());
				rueckforderungFormular.setStufe1FreigabeDatum(LocalDateTime.now());
				// Bestaetigung schicken
				createBestaetigungStufe1Geprueft(rueckforderungFormular);

				// Falls unterdessen die Phase zwei bereits aktiviert wurde, wollen wir mit "geprueft" der Phase zwei direkt in die Bearbeitung
				// Institution Phase 2 wechseln, da wir sonst auf "geprueft" blockiert bleiben
				if (applicationPropertyService.isKantonNotverordnungPhase2Aktiviert()
					&& principalBean.isCallerInAnyOfRole(UserRole.getMandantSuperadminRoles())) {
					// Direkt zum naechsten Status wechseln. In der Audit-Tabelle wird nur der neue Status sein
					// Finde ich aber okay, da es auch nur 1 Benutzeraktion war, die von Status IN_PRUEFUNG_KANTON_STUFE_1
					// zu IN_BEARBEITUNG_INSTITUTION_STUFE_2 gefuehrt hat.
					rueckforderungFormular.setStatus(RueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_2);
					rueckforderungFormular.setStufe2InstitutionKostenuebernahmeAnzahlStunden(rueckforderungFormular.getStufe1KantonKostenuebernahmeAnzahlStunden());
					rueckforderungFormular.setStufe2InstitutionKostenuebernahmeAnzahlTage(rueckforderungFormular.getStufe1KantonKostenuebernahmeAnzahlTage());
					rueckforderungFormular.setStufe2InstitutionKostenuebernahmeBetreuung(rueckforderungFormular.getStufe1KantonKostenuebernahmeBetreuung());
				}
			}
			break;
		}
		case GEPRUEFT_STUFE_1: {
			if (applicationPropertyService.isKantonNotverordnungPhase2Aktiviert()
					&& principalBean.isCallerInAnyOfRole(UserRole.getMandantSuperadminRoles())) {
				rueckforderungFormular.setStatus(RueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_2);
				rueckforderungFormular.setStufe2InstitutionKostenuebernahmeAnzahlStunden(rueckforderungFormular.getStufe1KantonKostenuebernahmeAnzahlStunden());
				rueckforderungFormular.setStufe2InstitutionKostenuebernahmeAnzahlTage(rueckforderungFormular.getStufe1KantonKostenuebernahmeAnzahlTage());
				rueckforderungFormular.setStufe2InstitutionKostenuebernahmeBetreuung(rueckforderungFormular.getStufe1KantonKostenuebernahmeBetreuung());
			}
			break;
		}
		case IN_PRUEFUNG_KANTON_STUFE_2_PROVISORISCH: {
			if (principalBean.isCallerInAnyOfRole(UserRole.getMandantSuperadminRoles())) {
				rueckforderungFormular.setStatus(RueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_2_DEFINITIV);
			}
			break;
		}
		case IN_BEARBEITUNG_INSTITUTION_STUFE_2_DEFINITIV: {
			if (principalBean.isCallerInAnyOfRole(UserRole.getInstitutionTraegerschaftRoles())) {
				rueckforderungFormular.setStatus(RueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2);
			}
			break;
		}
		default:
			break;
		}
	}

	private void createBestaetigungStufe1Geprueft(@Nonnull RueckforderungFormular modifiedRueckforderungFormular) {
		try {
			// Als Hack, weil im Nachhinein die Anforderung kam, das Mail auch noch als RueckforderungsMitteilung zu
			// speichern, wird hier der generierte HTML-Inhalt des Mails zurueckgegeben
			final String mailText = mailService.sendNotrechtBestaetigungPruefungStufe1(modifiedRueckforderungFormular);
			if (mailText != null) {
				// Wir wollen nur den body speichern
				String content = StringUtils.substringBetween(mailText, "<body>", "</body>");
				// remove any newlines or tabs (leading or trailing whitespace doesn't matter)
				content = content.replaceAll("(\\t|\\n)", "");
				// boil down remaining whitespace to a single space
				content = content.replaceAll("\\s+", " ");
				content = content.trim();

				final String betreff = "Corona-Finanzierung für Kitas und  TFO: Zahlung freigegeben / "
					+ "Corona - financement pour les crèches et les parents de jour: Versement libéré";
				RueckforderungMitteilung mitteilung = new RueckforderungMitteilung();
				mitteilung.setBetreff(betreff);
				mitteilung.setInhalt(content);
				mitteilung.setAbsender(principalBean.getBenutzer());
				mitteilung.setSendeDatum(LocalDateTime.now());
				mitteilung = persistence.persist(mitteilung);
				addMitteilung(modifiedRueckforderungFormular, mitteilung);
			}
		} catch (MailException e) {
			throw new EbeguRuntimeException("update",
				"BestaetigungEmail koennte nicht geschickt werden fuer RueckforderungFormular: " + modifiedRueckforderungFormular.getId(), e);
		}
	}
}