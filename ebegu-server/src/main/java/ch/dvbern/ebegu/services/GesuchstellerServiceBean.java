package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Service fuer Gesuchsteller
 */
@Stateless
@Local(GesuchstellerService.class)
public class GesuchstellerServiceBean extends AbstractBaseService implements GesuchstellerService {

	@Inject
	private Persistence<Gesuchsteller> persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;


	@Nonnull
	@Override
	public Gesuchsteller createGesuchsteller(@Nonnull Gesuchsteller gesuchsteller) {
		Objects.requireNonNull(gesuchsteller);
		return persistence.persist(gesuchsteller);
	}

	@Nonnull
	@Override
	public Gesuchsteller updateGesuchsteller(@Nonnull Gesuchsteller gesuchsteller) {
		Objects.requireNonNull(gesuchsteller);
		return persistence.merge(gesuchsteller);
	}

	@Nonnull
	@Override
	public Optional<Gesuchsteller> findGesuchsteller(@Nonnull final String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		Gesuchsteller a =  persistence.find(Gesuchsteller.class, id);
		return Optional.ofNullable(a);
	}

	@Override
	@Nonnull
	public Collection<Gesuchsteller> getAllGesuchsteller() {
		return new ArrayList<>(criteriaQueryHelper.getAll(Gesuchsteller.class));
	}

	@Override
	public void removeGesuchsteller(@Nonnull Gesuchsteller gesuchsteller) {
		Validate.notNull(gesuchsteller);
		Optional<Gesuchsteller> propertyToRemove = findGesuchsteller(gesuchsteller.getId());
		propertyToRemove.orElseThrow(() -> new EbeguEntityNotFoundException("removeGesuchsteller", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuchsteller));
		persistence.remove(propertyToRemove.get());
	}
}