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

package ch.dvbern.ebegu.api.resource;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxMandant;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.services.MandantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST Resource fuer Mandanten
 */
@Path("mandanten")
@Stateless
@Api(description = "Resource für Mandanten")
@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
public class MandantResource {

	@Inject
	private MandantService mandantService;

	@Inject
	private JaxBConverter converter;

	@ApiOperation(value = "Gibt den Mandanten mit der angegebenen id zurueck.", response = JaxMandant.class)
	@Nullable
	@GET
	@Path("/id/{mandantId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxMandant findMandant(@Nonnull @NotNull @PathParam("mandantId") JaxId mandantJAXPId) {
		Objects.requireNonNull(mandantJAXPId.getId());
		String mandantID = converter.toEntityId(mandantJAXPId);
		Optional<Mandant> optional = mandantService.findMandant(mandantID);

		return optional.map(mandant -> converter.mandantToJAX(mandant)).orElse(null);
	}

	@ApiOperation(value = "Gibt alle aktiven Mandanten zurueck.")
	@Nullable
	@GET
	@Path("/all")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<JaxMandant> findAllActive() {
		Collection<Mandant> all = mandantService.getAll();

		return all.stream()
				.filter(Mandant::isActivated)
				.map(mandant -> converter.mandantToJAX(mandant))
				.collect(Collectors.toList());
	}

}
