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

import javax.inject.Inject;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxEinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.api.dtos.JaxFall;
import ch.dvbern.ebegu.api.dtos.JaxGesuch;
import ch.dvbern.ebegu.api.resource.EinkommensverschlechterungInfoResource;
import ch.dvbern.ebegu.api.resource.FallResource;
import ch.dvbern.ebegu.api.resource.GesuchResource;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.errors.EbeguException;
import ch.dvbern.ebegu.rest.test.util.TestJaxDataUtil;
import ch.dvbern.ebegu.tets.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testet die EinkommensverschlechterungsInfo Resource
 */
@RunWith(Arquillian.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class EinkommensverschlechterungInfoResourceTest extends AbstractEbeguRestLoginTest {

	@Inject
	private EinkommensverschlechterungInfoResource einkommensverschlechterungInfoResource;

	@Inject
	private GesuchResource gesuchResource;

	@Inject
	private FallResource fallResource;

	@Inject
	private JaxBConverter converter;

	@Inject
	private Persistence persistence;


	@Test
	public void createEinkommensverschlechterungInfoTest() throws EbeguException {
		JaxGesuch returnedGesuch = crateJaxGesuch();

		JaxGesuch gesuch = gesuchResource.findGesuch(converter.toJaxId(returnedGesuch));
		Assert.assertNotNull(gesuch);
		Assert.assertNull(gesuch.getEinkommensverschlechterungInfoContainer());

		final JaxEinkommensverschlechterungInfoContainer testJaxEinkommensverschlechterungInfo = TestJaxDataUtil.createTestJaxEinkommensverschlechterungInfoContainer();

		einkommensverschlechterungInfoResource.saveEinkommensverschlechterungInfo(converter.toJaxId(returnedGesuch), testJaxEinkommensverschlechterungInfo, DUMMY_URIINFO, DUMMY_RESPONSE);

		gesuch = gesuchResource.findGesuch(converter.toJaxId(returnedGesuch));
		Assert.assertNotNull(gesuch);
		Assert.assertNotNull(gesuch.getEinkommensverschlechterungInfoContainer());
	}

	private JaxGesuch crateJaxGesuch() {
		Benutzer verantwortlicher = TestDataUtil.createDefaultBenutzer();
		persistence.persist(verantwortlicher.getMandant());
		verantwortlicher = persistence.persist(verantwortlicher);

		JaxGesuch testJaxGesuch = TestJaxDataUtil.createTestJaxGesuch();
		testJaxGesuch.getDossier().setVerantwortlicherBG(converter.benutzerToAuthLoginElement(verantwortlicher));

		JaxFall returnedFall = fallResource.saveFall(testJaxGesuch.getDossier().getFall(), DUMMY_URIINFO, DUMMY_RESPONSE);
		Assert.assertNotNull(returnedFall);
		testJaxGesuch.setGesuchsperiode(saveGesuchsperiodeInStatusAktiv(testJaxGesuch.getGesuchsperiode()));
		testJaxGesuch.getDossier().setFall(returnedFall);
		return (JaxGesuch) gesuchResource.create(testJaxGesuch, DUMMY_URIINFO, DUMMY_RESPONSE).getEntity();
	}
}
