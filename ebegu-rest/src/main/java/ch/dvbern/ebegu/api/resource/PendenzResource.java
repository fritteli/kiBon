package ch.dvbern.ebegu.api.resource;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxPendenzInstitution;
import ch.dvbern.ebegu.dto.JaxAntragDTO;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.GesuchService;
import io.swagger.annotations.Api;

import javax.annotation.Nonnull;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * REST Resource fuer Pendenzen
 */
@Path("pendenzen")
@Stateless
@Api
public class PendenzResource {

	@Inject
	private JaxBConverter converter;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private BetreuungService betreuungService;


	/**
	 * Gibt eine Liste mit allen Pendenzen des Jugendamtes zurueck. Sollte keine Pendenze gefunden werden oder ein Fehler passieren, wird eine leere Liste zurueckgegeben.
	 */
	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxAntragDTO> getAllPendenzenJA() {
		Collection<Gesuch> gesucheList = gesuchService.getAllActiveGesuche();

		List<JaxAntragDTO> pendenzenList = new ArrayList<>();
		// todo team fuer Mutationen musst dieser wert AntragTyp.MUTATION sein
		gesucheList.stream().filter(gesuch -> gesuch.getFall() != null)
			.forEach(gesuch -> pendenzenList.add(converter.gesuchToAntragDTO(gesuch)));
		return pendenzenList;
	}

	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/institution")
	public List<JaxPendenzInstitution> getAllPendenzenInstitution() {
		Collection<Betreuung> betreuungenInStatus = betreuungService.getPendenzenForInstitutionsOrTraegerschaftUser();
		List<JaxPendenzInstitution> pendenzenList = new ArrayList<>();
		for (Betreuung betreuung : betreuungenInStatus) {
			JaxPendenzInstitution pendenz = new JaxPendenzInstitution();
			pendenz.setBetreuungsNummer(betreuung.getBGNummer());
			pendenz.setBetreuungsId(betreuung.getId());
			pendenz.setGesuchId(betreuung.extractGesuch().getId());
			pendenz.setKindId(betreuung.getKind().getId());
			pendenz.setName(betreuung.getKind().getKindJA().getNachname());
			pendenz.setVorname(betreuung.getKind().getKindJA().getVorname());
			pendenz.setGeburtsdatum(betreuung.getKind().getKindJA().getGeburtsdatum());
			pendenz.setEingangsdatum(betreuung.extractGesuch().getEingangsdatum());
			pendenz.setGesuchsperiode(converter.gesuchsperiodeToJAX(betreuung.extractGesuchsperiode()));
			pendenz.setBetreuungsangebotTyp(betreuung.getBetreuungsangebotTyp());
			pendenz.setInstitution(converter.institutionToJAX(betreuung.getInstitutionStammdaten().getInstitution()));

			if (betreuung.getVorgaengerId() == null) {
				pendenz.setTyp("PLATZBESTAETIGUNG");
			}else{
				//Wenn die Betreung eine VorgängerID hat ist sie mutiert
				pendenz.setTyp("PLATZBESTAETIGUNG_MUTATION");
			}

			pendenzenList.add(pendenz);
		}
		return pendenzenList;
	}


}
