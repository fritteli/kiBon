/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.outbox.gemeindekennzahlen;

import java.util.List;
import java.util.Map;

import javax.annotation.security.RunAs;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.gemeindeantrag.gemeindekennzahlen.GemeindeKennzahlen;
import ch.dvbern.ebegu.entities.gemeindeantrag.gemeindekennzahlen.GemeindeKennzahlen_;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.lib.cdipersistence.Persistence;

@Stateless
@RunAs(UserRoleName.SUPER_ADMIN)
public class GemeindeKennzahlenEventGenerator {

	@Inject
	private Persistence persistence;

	@Inject
	private Event<ExportedEvent> event;

	@Inject
	private GemeindeKennzahlenEventConverter gemeindeKennzahlenEventConverter;

	@Inject
	private EinstellungService einstellungService;

	@Schedule(info = "Migration-aid, pushes already existing Gemeinden to outbox",
		hour = "5",
		minute = "15",
		persistent = true)
	public void publishExistingGemeinden() {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<GemeindeKennzahlen> query = cb.createQuery(GemeindeKennzahlen.class);
		Root<GemeindeKennzahlen> root = query.from(GemeindeKennzahlen.class);

		Predicate isNotPublished = cb.isFalse(root.get(GemeindeKennzahlen_.eventPublished));

		query.where(isNotPublished);

		List<GemeindeKennzahlen> gemeinden = persistence.getEntityManager().createQuery(query)
			.getResultList();

		gemeinden.forEach(gemeindeKennzahlen -> {

			Map<EinstellungKey, Einstellung> gemeindeKonfigurationMap = einstellungService
				.getGemeindeEinstellungenOnlyAsMap(
					gemeindeKennzahlen.getGemeinde(),
					gemeindeKennzahlen.getGesuchsperiode());

			Einstellung einstellungBgAusstellenBisStufe =
				gemeindeKonfigurationMap.get(EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE);
			EinschulungTyp bgAusstellenBisUndMitStufe =
				EinschulungTyp.valueOf(einstellungBgAusstellenBisStufe.getValue());

			Einstellung einstellungErwerbspensumZuschlag =
				gemeindeKonfigurationMap.get(EinstellungKey.ERWERBSPENSUM_ZUSCHLAG);

			event.fire(gemeindeKennzahlenEventConverter.of(
				gemeindeKennzahlen,
				bgAusstellenBisUndMitStufe,
				einstellungErwerbspensumZuschlag.getValueAsBigDecimal()));
			gemeindeKennzahlen.setEventPublished(true);
			persistence.merge(gemeindeKennzahlen);

		});
	}
}
