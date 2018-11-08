/*
 *
 *  * Copyright (C) 2018 DV Bern AG, Switzerland
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Affero General Public License as
 *  * published by the Free Software Foundation, either version 3 of the
 *  * License, or (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU Affero General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Affero General Public License
 *  * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package ch.dvbern.ebegu.validators;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.IntegrationTyp;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import org.apache.commons.lang3.Range;

/**
 * Validator for PensumFachstelle, checks that the entered betreuungspensum is greather than the minimum
 * that is allowed and lesser than the max that is allowed for the selected IntegrationTyp
 *
 * IMPORTANT! This validator is validating the Kind and not the PensumFachstelle itself. This is because in order
 * to validate a PensumFachstell we need a context with a gesuchsperiode for which the required Einstellungen have
 * been defined. This context is only available if we check the KindContainer and not directly the PensumFachstelle
 */
public class CheckPensumFachstelleValidator implements ConstraintValidator<CheckPensumFachstelle, KindContainer> {

	@SuppressWarnings("CdiInjectionPointsInspection")
	@Inject
	private EinstellungService einstellungService;

	// We need to pass to EinstellungService a new EntityManager to avoid errors like ConcurrentModificatinoException.
	// So we create it here and pass it to the methods of EinstellungService we need to call.
	// http://stackoverflow.com/questions/18267269/correct-way-to-do-an-entitymanager-query-during-hibernate-validation
	@PersistenceUnit(unitName = "ebeguPersistenceUnit")
	private EntityManagerFactory entityManagerFactory;

	public CheckPensumFachstelleValidator() {
	}

	/**
	 * Constructor fuer tests damit service reingegeben werden kann
	 *
	 * @param service service zum testen
	 */
	public CheckPensumFachstelleValidator(
		@Nonnull EinstellungService service,
		@Nonnull EntityManagerFactory entityManagerFactory
	) {
		this.einstellungService = service;
		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public void initialize(CheckPensumFachstelle constraintAnnotation) {
		// nop
	}

	@Override
	public boolean isValid(@Nonnull KindContainer kindContainer, ConstraintValidatorContext context) {

		if (kindContainer.getKindJA() == null
			|| kindContainer.getKindJA().getPensumFachstelle() == null
			|| kindContainer.getKindJA().getPensumFachstelle().getFachstelle() == null
		) {
			// Kein PensumFachstelle
			return true;
		}

		final EntityManager em = createEntityManager();

		final Gemeinde gemeinde = kindContainer.getGesuch().extractGemeinde();
		final Gesuchsperiode gesuchsperiode = kindContainer.getGesuch().getGesuchsperiode();
		final PensumFachstelle pensumFachstelle = kindContainer.getKindJA().getPensumFachstelle();

		Integer minValueAllowed = getValueAsInteger(
			getMinValueParamFromIntegrationTyp(pensumFachstelle.getIntegrationTyp()),
			gemeinde, gesuchsperiode, em);

		Integer maxValueAllowed = getValueAsInteger(
			getMaxValueParamFromIntegrationTyp(pensumFachstelle.getIntegrationTyp()),
			gemeinde, gesuchsperiode, em);

		closeEntityManager(em);

		if (!Range.between(minValueAllowed, maxValueAllowed).contains(pensumFachstelle.getPensum())) {
			createConstraintViolation(minValueAllowed, maxValueAllowed, pensumFachstelle.getIntegrationTyp(), context);
			return false;
		}

		return true;
	}

	private EinstellungKey getMinValueParamFromIntegrationTyp(@Nonnull IntegrationTyp integrationTyp) {
		switch (integrationTyp) {
		case SOZIALE_INTEGRATION:
			return EinstellungKey.FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION;
		case SPRACHLICHE_INTEGRATION:
			return EinstellungKey.FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION;
		}
		return null;
	}

	private EinstellungKey getMaxValueParamFromIntegrationTyp(@Nonnull IntegrationTyp integrationTyp) {
		switch (integrationTyp) {
		case SOZIALE_INTEGRATION:
			return EinstellungKey.FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION;
		case SPRACHLICHE_INTEGRATION:
			return EinstellungKey.FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION;
		}
		return null;
	}

	private Integer getValueAsInteger(
		EinstellungKey key,
		Gemeinde gemeinde,
		Gesuchsperiode gesuchsperiode,
		EntityManager em
	) {
		Einstellung minParam = einstellungService
			.findEinstellung(key, gemeinde, gesuchsperiode); //, em);
		return minParam.getValueAsInteger();
	}

	@Nullable
	private EntityManager createEntityManager() {
		if (entityManagerFactory != null) {
			return entityManagerFactory.createEntityManager();
		}
		return null;
	}

	private void closeEntityManager(@Nullable EntityManager em) {
		if (em != null) {
			em.close();
		}
	}

	/**
	 * Creates a ConstraintViolation with the given parameters. A customized message will be created.
	 */
	private void createConstraintViolation(
		@Nonnull Integer minValueAllowed,
		@Nonnull Integer maxValueAllowed,
		@Nonnull IntegrationTyp integrationTyp,
		ConstraintValidatorContext context
	) {
		ResourceBundle rb = ResourceBundle.getBundle("ValidationMessages");
		String message = rb.getString("invalid_pensumfachstelle");
		String integrationTypTranslated = ServerMessageUtil.translateEnumValue(integrationTyp);
		message = MessageFormat.format(message, minValueAllowed, maxValueAllowed, integrationTypTranslated);

		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(message)
			.addPropertyNode("pensumFachstelle.pensum")
			.addConstraintViolation();
	}
}
