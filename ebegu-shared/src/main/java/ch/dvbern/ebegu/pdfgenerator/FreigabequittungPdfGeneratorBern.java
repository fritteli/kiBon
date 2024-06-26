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

package ch.dvbern.ebegu.pdfgenerator;

import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import com.lowagie.text.Document;
import com.lowagie.text.Element;

public class FreigabequittungPdfGeneratorBern extends AbstractFreigabequittungPdfGenerator{

	public FreigabequittungPdfGeneratorBern(
		@Nonnull Gesuch gesuch,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull List<DokumentGrund> benoetigteUnterlagen) {
		super(gesuch, stammdaten, benoetigteUnterlagen);
	}

	@Override
	protected void createParagraphBitteAusdrucken(Document document) {
		//no-op dieser Paragraph soll für Bern nicht angezeigt werden
	}

	@Override
	protected void createParagraphBenoetigteUnterlagenInfo(Document document) {
		//no-op dieser Paragraph soll für Bern nicht angezeigt werden
	}

	@Override
	protected void createParagraphSofortEinrichten(List<Element> paragraphlist) {
		//no-op dieser Paragraph soll für Bern nicht angezeigt werden
	}
}
