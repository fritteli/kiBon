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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services.util;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;

/**
 * Factory, welche für einen ZahlungslaufTyp den richtigen ZahlungslaufHelper ermittelt.
 */
public final class ZahlungslaufHelperFactory {

	private ZahlungslaufHelperFactory() {
	}

	@Nonnull
	public static ZahlungslaufHelper getZahlungslaufHelper(@Nonnull ZahlungslaufTyp zahlungslaufTyp) {
		if (ZahlungslaufTyp.GEMEINDE_INSTITUTION == zahlungslaufTyp) {
			return new ZahlungslaufInstitutionenHelper();
		}
		if (ZahlungslaufTyp.GEMEINDE_ANTRAGSTELLER == zahlungslaufTyp) {
			return new ZahlungslaufAntragstellerHelper();
		}
		// Unbekannter Typ: Exception werfen, wir koennen diesen Zahlungslauf nicht berechnen
		throw new EbeguRuntimeException(
			"getZahlungslaufHelper",
			"No Implementation defined for ZahlungslaufTyp " + zahlungslaufTyp);
	}
}
