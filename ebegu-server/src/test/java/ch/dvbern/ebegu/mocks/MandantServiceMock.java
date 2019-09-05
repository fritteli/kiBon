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

package ch.dvbern.ebegu.mocks;

import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.services.MandantServiceBean;
import ch.dvbern.ebegu.test.TestDataUtil;

public class MandantServiceMock extends MandantServiceBean {

	private static final Mandant MANDANT = TestDataUtil.createDefaultMandant();

	@Nonnull
	@Override
	public Optional<Mandant> findMandant(@Nonnull String id) {
		return Optional.of(MANDANT);
	}

	@Nonnull
	@Override
	public Mandant getFirst() {
		return MANDANT;
	}
}
