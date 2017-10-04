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

package ch.dvbern.ebegu.rest.test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.resource.PendenzResource;
import ch.dvbern.ebegu.dto.JaxAntragDTO;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.tets.TestDataUtil;
import ch.dvbern.ebegu.util.AntragStatusConverterUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testet PendenzResource
 */
@RunWith(Arquillian.class)
@UsingDataSet("datasets/empty.xml")
@Transactional(TransactionMode.DISABLED)
public class PendenzResourceTest extends AbstractEbeguRestLoginTest {

	@Inject
	private PendenzResource pendenzResource;
	@Inject
	private Persistence persistence;
	@Inject
	private JaxBConverter converter;


	@Test
	public void getAllPendenzenJATest() {
		Gesuch gesuch1 = TestDataUtil.createDefaultGesuch();
		TestDataUtil.persistEntities(gesuch1, persistence);
		Gesuch gesuch2 = TestDataUtil.createDefaultGesuch();
		TestDataUtil.persistEntities(gesuch2, persistence);

		List<JaxAntragDTO> pendenzenList = pendenzResource.getAllPendenzenJA();
		// Die Antraege muessen sortiert werden, damit der Test immer gleich ablaeuft
		Collections.sort(pendenzenList, (o1, o2) -> o1.getFallNummer() > o2.getFallNummer() ? 1 : -1);

		Assert.assertNotNull(pendenzenList);
		Assert.assertEquals(2, pendenzenList.size());

		assertGesuchDaten(gesuch1, pendenzenList.get(0));
		assertGesuchDaten(gesuch2, pendenzenList.get(1));

		Set<BetreuungsangebotTyp> angeboteList = new LinkedHashSet<>();
		angeboteList.add(BetreuungsangebotTyp.KITA);
		Assert.assertEquals(angeboteList, pendenzenList.get(0).getAngebote());

		Assert.assertEquals(AntragTyp.ERSTGESUCH, pendenzenList.get(0).getAntragTyp());

		Set<String> institutionen = new LinkedHashSet<>();
		institutionen.add("Institution1");
		Assert.assertEquals(institutionen, pendenzenList.get(0).getInstitutionen());
		Assert.assertEquals(gesuch1.getGesuchsperiode().getGueltigkeit().getGueltigAb(), pendenzenList.get(0).getGesuchsperiodeGueltigAb());
		Assert.assertEquals(gesuch1.getGesuchsperiode().getGueltigkeit().getGueltigBis(), pendenzenList.get(0).getGesuchsperiodeGueltigBis());
	}


	// HELP METHOD

	private void assertGesuchDaten(Gesuch gesuch1, JaxAntragDTO pendenzenList) {
		Assert.assertEquals(gesuch1.getFall().getFallNummer(), pendenzenList.getFallNummer());
		Assert.assertEquals(gesuch1.getGesuchsteller1().extractNachname(), pendenzenList.getFamilienName());
		Assert.assertEquals(gesuch1.getEingangsdatum(), pendenzenList.getEingangsdatum());
		Assert.assertEquals(gesuch1.getStatus(), AntragStatusConverterUtil.convertStatusToEntity(pendenzenList.getStatus()));
	}
}
