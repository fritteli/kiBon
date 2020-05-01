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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.enums.RueckforderungStatus;
import ch.dvbern.lib.cdipersistence.Persistence;

import ch.dvbern.ebegu.entities.RueckforderungFormular;

import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@RolesAllowed({ SUPER_ADMIN })
@Stateless
@Local(RueckforderungFormularService.class)
public class RueckforderungFormularServiceBean extends AbstractBaseService implements RueckforderungFormularService {

	@Inject
	private Persistence persistence;

	@Inject
	private InstitutionService institutionService;

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN })
	public List<RueckforderungFormular> initializeRueckforderungFormulare() {

		Collection<Institution> institutionen = institutionService.getAllInstitutionen();

		List<RueckforderungFormular> rueckforderungFormulare = new ArrayList<>();
		for (Institution institution : institutionen) {
			RueckforderungFormular formular = new RueckforderungFormular();
			formular.setInstitution(institution);
			formular.setStatus(RueckforderungStatus.NEU);
			rueckforderungFormulare.add(createRueckforderungFormular(formular));
		}
		return rueckforderungFormulare;
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN })
	public RueckforderungFormular createRueckforderungFormular(@Nonnull RueckforderungFormular rueckforderungFormular) {
		return persistence.persist(rueckforderungFormular);
	}
}
