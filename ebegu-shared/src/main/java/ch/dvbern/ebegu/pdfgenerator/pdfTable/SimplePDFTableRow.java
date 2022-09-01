/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.pdfgenerator.pdfTable;

import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.pdfgenerator.PdfUtil;

public class SimplePDFTableRow {

	@Nonnull
	private String label;

	@Nullable
	private String supertext;

	@Nonnull
	private String value;

	private boolean isHeader = false;


	public SimplePDFTableRow(@Nonnull String label, @Nonnull String value) {
		this.label = label;
		this.value = value;
	}

	public SimplePDFTableRow(@Nonnull String label, @Nonnull String value, boolean isHeader) {
		this.label = label;
		this.value = value;
		this.isHeader = isHeader;
	}

	public SimplePDFTableRow(@Nonnull String label, @Nullable BigDecimal value) {
		this.label = label;
		this.value = PdfUtil.printBigDecimal(value);
	}

	public SimplePDFTableRow(@Nonnull String label, @Nullable BigDecimal value, boolean isHeader) {
		this.label = label;
		this.value = PdfUtil.printBigDecimal(value);
		this.isHeader = isHeader;
	}

	public SimplePDFTableRow(@Nonnull String label, @Nullable Integer value) {
		this.label = label;
		this.value = value != null? String.valueOf(value) : "";
	}

	@Nonnull
	public String getLabel() {
		return label;
	}

	public void setLabel(@Nonnull String label) {
		this.label = label;
	}

	@Nullable
	public String getSupertext() {
		return supertext;
	}

	public void setSupertext(@Nullable String supertext) {
		this.supertext = supertext;
	}

	@Nonnull
	public String getValue() {
		return value;
	}

	public void setValue(@Nonnull String value) {
		this.value = value;
	}

	public boolean isHeader() {
		return isHeader;
	}

	public void setHeader(boolean header) {
		isHeader = header;
	}
}