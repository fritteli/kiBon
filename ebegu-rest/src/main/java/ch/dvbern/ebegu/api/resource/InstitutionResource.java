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

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxInstitution;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.InstitutionStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static java.util.Objects.requireNonNull;

/**
 * REST Resource fuer Institution
 */
@Path("institutionen")
@Stateless
@Api(description = "Resource für Institutionen (Anbieter eines Betreuungsangebotes)")
public class InstitutionResource {

	@Inject
	private InstitutionService institutionService;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private JaxBConverter converter;

	@ApiOperation(value = "Creates a new Institution in the database.", response = JaxInstitution.class)
	@Nullable
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createInstitution(
		@Nonnull @NotNull JaxInstitution institutionJAXP,
		@Nonnull @NotNull @Valid @QueryParam("date") String stringDateBeguStart,
		@Nonnull @NotNull @Valid @QueryParam("betreuung") String betreuungsangebot,
		@Nonnull @NotNull @Valid @QueryParam("adminMail") String adminMail,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		requireNonNull(adminMail);
		Institution convertedInstitution = converter.institutionToEntity(institutionJAXP, new Institution());
		Institution persistedInstitution = this.institutionService.createInstitution(convertedInstitution);

		initInstitutionStammdaten(stringDateBeguStart, betreuungsangebot, persistedInstitution, adminMail);

		Benutzer benutzer = benutzerService.findBenutzerByEmail(adminMail)
			.map(b -> {
				if (b.getRole() != UserRole.ADMIN_TRAEGERSCHAFT ||
					!Objects.equals(b.getTraegerschaft(), persistedInstitution.getTraegerschaft())) {
					// an existing user cannot be used to create a new Institution
					throw new EbeguRuntimeException(
						KibonLogLevel.INFO,
						"createInstitution",
						ErrorCodeEnum.EXISTING_USER_MAIL,
						adminMail);
				}

				return b;
			})
			.orElseGet(() -> benutzerService.createAdminInstitutionByEmail(adminMail, persistedInstitution));

		benutzerService.einladen(Einladung.forInstitution(benutzer, persistedInstitution));

		URI uri = uriInfo.getBaseUriBuilder()
			.path(InstitutionResource.class)
			.path('/' + persistedInstitution.getId())
			.build();

		JaxInstitution jaxInstitution = converter.institutionToJAX(persistedInstitution);
		return Response.created(uri).entity(jaxInstitution).build();
	}

	private void initInstitutionStammdaten(
		@Nonnull String stringDateBeguStart,
		@Nonnull String betreuungsangebot,
		@Nonnull Institution persistedInstitution,
		@Nonnull String adminMail
	) {
		InstitutionStammdaten institutionStammdaten = new InstitutionStammdaten();
		Adresse adresse = new Adresse();
		adresse.setStrasse("");
		adresse.setPlz("");
		adresse.setOrt("");
		institutionStammdaten.setAdresse(adresse);
		institutionStammdaten.setBetreuungsangebotTyp(BetreuungsangebotTyp.valueOf(betreuungsangebot));
		if (!(institutionStammdaten.getBetreuungsangebotTyp().equals(BetreuungsangebotTyp.TAGESFAMILIEN))) {
			institutionStammdaten.setAnzahlPlaetze(BigDecimal.ZERO);
		}
		institutionStammdaten.setInstitution(persistedInstitution);
		institutionStammdaten.setMail(adminMail);
		LocalDate beguStart = LocalDate.parse(stringDateBeguStart, Constants.SQL_DATE_FORMAT);
		DateRange gueltigkeit = new DateRange(beguStart, Constants.END_OF_TIME);
		institutionStammdaten.setGueltigkeit(gueltigkeit);
		institutionStammdatenService.saveInstitutionStammdaten(institutionStammdaten);
	}

	@ApiOperation(value = "Update a Institution in the database.", response = JaxInstitution.class)
	@Nullable
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxInstitution updateInstitution(
		@Nonnull @NotNull @Valid JaxInstitution institutionJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		requireNonNull(institutionJAXP.getId());
		Optional<Institution> optInstitution = institutionService.findInstitution(institutionJAXP.getId());
		Institution institutionFromDB = optInstitution
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"update",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				institutionJAXP.getId()));

		Institution institutionToMerge = converter.institutionToEntity(institutionJAXP, institutionFromDB);
		Institution modifiedInstitution = this.institutionService.updateInstitution(institutionToMerge);
		return converter.institutionToJAX(modifiedInstitution);
	}

	@ApiOperation(value = "Find and return an Institution by his institution id as parameter",
		response = JaxInstitution.class)
	@Nullable
	@GET
	@Path("/id/{institutionId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxInstitution findInstitution(
		@Nonnull @NotNull @PathParam("institutionId") JaxId institutionJAXPId) {

		requireNonNull(institutionJAXPId.getId());
		String institutionID = converter.toEntityId(institutionJAXPId);
		Optional<Institution> optional = institutionService.findInstitution(institutionID);

		return optional.map(institution -> converter.institutionToJAX(institution)).orElse(null);
	}

	@ApiOperation("Remove an Institution logically by his institution-id as parameter")
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@Nullable
	@DELETE
	@Path("/{institutionId}")
	@Consumes(MediaType.WILDCARD)
	public Response removeInstitution(
		@Nonnull @NotNull @PathParam("institutionId") JaxId institutionJAXPId,
		@Context HttpServletResponse response) {

		requireNonNull(institutionJAXPId.getId());
		institutionService.setInstitutionInactive(converter.toEntityId(institutionJAXPId));
		return Response.ok().build();
	}

	@ApiOperation(value = "Find and return a list of Institution by the Traegerschaft as parameter",
		responseContainer = "List", response = JaxInstitution.class)
	@Nonnull
	@GET
	@Path("/traegerschaft/{traegerschaftId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxInstitution> getAllInstitutionenFromTraegerschaft(
		@Nonnull @NotNull @PathParam("traegerschaftId") JaxId traegerschaftJAXPId) {

		requireNonNull(traegerschaftJAXPId.getId());
		String traegerschaftId = converter.toEntityId(traegerschaftJAXPId);
		return institutionService.getAllInstitutionenFromTraegerschaft(traegerschaftId).stream()
			.map(institution -> converter.institutionToJAX(institution))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Find and return a list of all Institutionen",
		responseContainer = "List", response = JaxInstitution.class)
	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxInstitution> getAllInstitutionen() {
		return institutionService.getAllInstitutionen().stream()
			.map(inst -> converter.institutionToJAX(inst))
			.collect(Collectors.toList());
	}

	@ApiOperation(
		value = "Find and return a list of all active Institutionen. An active Institution is a Institution where the "
			+ "active flag is true",
		responseContainer = "List",
		response = JaxInstitution.class)
	@Nonnull
	@GET
	@Path("/active")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxInstitution> getAllActiveInstitutionen() {
		return institutionService.getAllActiveInstitutionen().stream()
			.map(inst -> converter.institutionToJAX(inst))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Find and return a list of all Institutionen of the currently logged in Benutzer. Retruns " +
		"all for admins", responseContainer = "List", response = JaxInstitution.class)
	@Nonnull
	@GET
	@Path("/currentuser")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxInstitution> getAllowedInstitutionenForCurrentBenutzer() {
		return institutionService.getAllowedInstitutionenForCurrentBenutzer(true).stream()
			.map(inst -> converter.institutionToJAX(inst))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Returns true, if the currently logged in Benutzer has any Institutionen in Status EINGELADEN", response = Boolean.class)
	@Nonnull
	@GET
	@Path("/hasEinladungen/currentuser")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response hasInstitutionenInStatusEingeladenForCurrentBenutzer() {
		long anzahl = institutionService.getAllowedInstitutionenForCurrentBenutzer(true).stream()
			.filter(inst -> inst.getStatus() == InstitutionStatus.EINGELADEN)
			.count();
		return Response.ok(anzahl > 0).build();
	}
}
