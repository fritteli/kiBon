/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.resource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxGemeinde;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxTraegerschaft;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static java.util.Objects.requireNonNull;

/**
 * Resource fuer Gemeinde
 */
@Path("gemeinde")
@Stateless
@Api(description = "Resource für Gemeinden")
public class GemeindeResource {

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private JaxBConverter converter;


	@ApiOperation(value = "Erstellt eine neue Gemeinde in der Datenbank", response = JaxTraegerschaft.class)
	@Nullable
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxGemeinde createGemeinde(
		@Nonnull @NotNull @Valid JaxGemeinde gemeindeJAXP,
		@Nonnull @NotNull @QueryParam("date") String stringDateBeguBietenAb,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		requireNonNull(gemeindeJAXP);
		requireNonNull(stringDateBeguBietenAb);

		Gemeinde convertedGemeinde = converter.gemeindeToEntity(gemeindeJAXP, new Gemeinde());
		LocalDate eingangsdatum = DateUtil.parseStringToDate(stringDateBeguBietenAb);

		Gemeinde persistedGemeinde = this.gemeindeService.createGemeinde(convertedGemeinde);

		einstellungService.createBeguBietenAbEinstellung(eingangsdatum, persistedGemeinde);

		// todo KIBON-211 the given user must be informed

		JaxGemeinde jaxGemeinde = converter.gemeindeToJAX(persistedGemeinde);
		return jaxGemeinde;
	}


	@ApiOperation(value = "Speichert eine Gemeinde in der Datenbank", response = JaxTraegerschaft.class)
	@Nullable
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxGemeinde saveGemeinde(
		@Nonnull @NotNull @Valid JaxGemeinde gemeindeJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Gemeinde gemeinde = new Gemeinde();
		if (gemeindeJAXP.getId() != null) {
			Optional<Gemeinde> optional = gemeindeService.findGemeinde(gemeindeJAXP.getId());
			gemeinde = optional.orElse(new Gemeinde());
		}
		Gemeinde convertedGemeinde = converter.gemeindeToEntity(gemeindeJAXP, gemeinde);
		Gemeinde persistedGemeinde = this.gemeindeService.saveGemeinde(convertedGemeinde);
		JaxGemeinde jaxGemeinde = converter.gemeindeToJAX(persistedGemeinde);
		return jaxGemeinde;
	}

	@ApiOperation(value = "Returns all Gemeinden", responseContainer = "Collection", response = JaxGemeinde.class)
	@Nullable
	@GET
	@Path("/all")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxGemeinde> getAllGemeinden() {
		return gemeindeService.getAllGemeinden().stream()
			.map(gemeinde -> converter.gemeindeToJAX(gemeinde))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Returns the Gemeinde with the given Id.", response = JaxGemeinde.class)
	@Nullable
	@GET
	@Path("/id/{gemeindeId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxGemeinde findGemeinde(
		@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJAXPId) {

		requireNonNull(gemeindeJAXPId.getId());
		String gemeindeId = converter.toEntityId(gemeindeJAXPId);
		Optional<Gemeinde> gemeindeOptional = gemeindeService.findGemeinde(gemeindeId);

		return gemeindeOptional.map(gemeinde -> converter.gemeindeToJAX(gemeinde)).orElse(null);
	}

	@ApiOperation(value = "Returns the Gemeinde with the given name.", response = JaxGemeinde.class)
	@Nullable
	@GET
	@Path("/name/{gemeindeName}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxGemeinde findGemeindeByName(
		@Nonnull @NotNull @PathParam("gemeindeName") String name) {
		Optional<Gemeinde> gemeindeOptional = gemeindeService.findGemeindeByName(name);
		return gemeindeOptional.map(gemeinde -> converter.gemeindeToJAX(gemeinde)).orElse(null);
	}

}
