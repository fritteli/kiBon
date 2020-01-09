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

package ch.dvbern.ebegu.reporting.lastenausgleich;

import java.math.BigDecimal;

import org.jetbrains.annotations.NotNull;

/**
 * DTO für den Lastenausgleich von KiBon
 */
public class LastenausgleichBerechnungCSVDataRow extends LastenausgleichBerechnungDataRow implements Comparable<LastenausgleichBerechnungCSVDataRow> {

	private BigDecimal totalRevision;

	public LastenausgleichBerechnungCSVDataRow() {};

	public LastenausgleichBerechnungCSVDataRow(LastenausgleichBerechnungDataRow parent) {
		super(parent);
	}

	public BigDecimal getTotalRevision() {
		return totalRevision;
	}

	public void setTotalRevision(BigDecimal totalRevision) {
		this.totalRevision = totalRevision;
	}

	public int compareTo(@NotNull LastenausgleichBerechnungCSVDataRow compare) {
		return this.getBfsNummer().compareTo(compare.getBfsNummer());
	}

	@Override
	public boolean equals(@NotNull Object o) {
		if (getClass() != o.getClass()) {
			return false;
		}
		return compareTo((LastenausgleichBerechnungCSVDataRow) o) == 0;
	}
}
