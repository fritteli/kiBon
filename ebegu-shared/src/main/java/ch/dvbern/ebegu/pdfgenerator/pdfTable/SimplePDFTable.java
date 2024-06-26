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

import java.awt.Color;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.pdfgenerator.PdfUtil;
import ch.dvbern.lib.invoicegenerator.dto.PageConfiguration;
import ch.dvbern.lib.invoicegenerator.pdf.PdfElementGenerator;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimplePDFTable {

	private final float[] columnWidths = { 14, 4 };
	private final PageConfiguration pageConfiguration;
	private final boolean lastLineBold;
	private final int[] alignement = { Element.ALIGN_LEFT,Element.ALIGN_RIGHT};
	private List<SimplePDFTableRow> rows = new ArrayList<>();

	private static final Logger LOG = LoggerFactory.getLogger(SimplePDFTable.class);

	public SimplePDFTable(PageConfiguration pageConfiguration, boolean lastLineBold) {
		this.pageConfiguration = pageConfiguration;
		this.lastLineBold = lastLineBold;
	}


	@Nonnull
	public PdfPTable createTable() {
		PdfPTable table = new PdfPTable(columnWidths.length);
		try {
			table.setWidths(columnWidths);
		} catch (DocumentException e) {
			LOG.error("settin column widths failed", e);
		}

		table.setWidthPercentage(PdfElementGenerator.FULL_WIDTH);
		for (int i = 0; i < rows.size(); i++) {
			SimplePDFTableRow row = rows.get(i);
			SimplePDFTableRowConfiguration rowConfiguration = row.getConfiguration();

			boolean isFooter = lastLineBold && i == rows.size() - 1;
			Font font = isFooter ? pageConfiguration.getFonts().getFontBold() : pageConfiguration.getFonts().getFont();
			rowConfiguration.setFont(font);

			addRow(table, row, rowConfiguration);
		}
		return table;
	}

	private void addRow(@Nonnull PdfPTable table, @Nonnull SimplePDFTableRow row, SimplePDFTableRowConfiguration rowConfiguration) {
		addCell(table, row.getLabel(), row.getSupertext(), rowConfiguration.getFont(), rowConfiguration.getBgColor(), alignement[0], rowConfiguration.getIdent());
		addCell(table, row.getValue(), null, rowConfiguration.getFont(), rowConfiguration.getBgColor(), alignement[1], rowConfiguration.getIdent());
	}

	private void addCell(@Nonnull PdfPTable table, @Nullable String value, @Nullable String supertext, @Nonnull Font font, @Nonnull Color bgColor, int alignment,
			int ident) {
		final Phrase phrase = new Phrase(value, font);
		if (supertext != null) {
			phrase.add(PdfUtil.createSuperTextInText(supertext));
		}
		PdfPCell cell = new PdfPCell(phrase);
		cell.setBackgroundColor(bgColor);
		cell.setHorizontalAlignment(alignment);
		cell.setLeading(0.0F, PdfUtil.DEFAULT_CELL_LEADING);
		cell.setIndent(ident);
		table.addCell(cell);
	}

	public void addRow(@Nonnull SimplePDFTableRow row) {
		this.rows.add(row);
	}

	public void addRow(@Nonnull String label, @Nullable String value) {
		this.addRow(new SimplePDFTableRow(label, value != null ? value : ""));
	}

	public void addHeaderRow(@Nonnull String label, @Nullable String value) {
		this.addRow(new SimplePDFTableRow(label, value != null ? value : "", true));
	}

	public void addRow(@Nonnull String label, @Nullable BigDecimal value) {
		this.addRow(new SimplePDFTableRow(label, value));
	}

	public void addRow(@Nonnull String label, @Nullable Integer value) {
		this.addRow(new SimplePDFTableRow(label, value));
	}

	public void addRow(@Nonnull String label, @Nullable String value, int ident) {
		this.addRow(new SimplePDFTableRow(label, value != null ? value : "", ident));
	}

	public void addHeaderRow(@Nonnull String label, @Nullable String value, int ident) {
		this.addRow(new SimplePDFTableRow(label, value != null ? value : "", true, ident));
	}

	public void addRow(@Nonnull String label, @Nullable BigDecimal value, int ident) {
		this.addRow(new SimplePDFTableRow(label, value, ident));
	}

	public void addRow(@Nonnull String label, @Nullable Integer value, int ident) {
		this.addRow(new SimplePDFTableRow(label, value, ident));
	}
}
