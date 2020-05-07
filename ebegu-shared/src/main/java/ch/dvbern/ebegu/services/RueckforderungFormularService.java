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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;

import ch.dvbern.ebegu.entities.RueckforderungFormular;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service fuer die Rueckforderungsformulare
 */
public interface RueckforderungFormularService {

	/**
	 * Erstellt leere Rückforderungsformulare für alle Kitas & TFOs die in kiBon existieren
	 * und bisher kein Rückforderungsformular haben
	 */
	@Nonnull
	List<RueckforderungFormular> initializeRueckforderungFormulare();

	@Nonnull
	RueckforderungFormular createRueckforderungFormular(RueckforderungFormular rueckforderungFormular);

	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, ADMIN_INSTITUTION, SACHBEARBEITER_MANDANT, SACHBEARBEITER_INSTITUTION})
	Collection<RueckforderungFormular> getAllRueckforderungFormulare();

	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, ADMIN_INSTITUTION, SACHBEARBEITER_MANDANT, SACHBEARBEITER_INSTITUTION})
	Optional<RueckforderungFormular> findRueckforderungFormular(String id);

	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, ADMIN_INSTITUTION, SACHBEARBEITER_MANDANT, SACHBEARBEITER_INSTITUTION})
	RueckforderungFormular save(RueckforderungFormular rueckforderungFormular);

}
