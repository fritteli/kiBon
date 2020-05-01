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

package ch.dvbern.ebegu.api.resource;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxRueckforderungFormular;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.services.RueckforderungFormularService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Path("notrecht")
@Stateless
@Api(description = "Resource zum Verwalten von Rueckforderungsformularen für das Notrecht")
public class RueckforderungFormularResource {

	@Inject
	private RueckforderungFormularService rueckforderungFormularService;

	@Inject
	private JaxBConverter converter;

	@ApiOperation(value = "Erstellt leere Rückforderungsformulare für alle Kitas & TFOs die in kiBon existieren "
		+ "und bisher kein Rückforderungsformular haben", responseContainer = "List", response = JaxRueckforderungFormular.class)
	@Nullable
	@POST
	@Path("/initialize")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN })
	public List<JaxRueckforderungFormular> initializeRueckforderungFormulare() {

		List<RueckforderungFormular> createdFormulare =
			rueckforderungFormularService.initializeRueckforderungFormulare();

		return converter.rueckforderungFormularListToJax(createdFormulare);
	}

}
