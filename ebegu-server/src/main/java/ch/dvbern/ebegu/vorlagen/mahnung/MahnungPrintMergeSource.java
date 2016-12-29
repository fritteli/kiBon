package ch.dvbern.ebegu.vorlagen.mahnung;

import ch.dvbern.ebegu.EBEGUMergeSource;
import ch.dvbern.lib.doctemplate.common.BeanMergeSource;
import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.common.MergeContext;
import ch.dvbern.lib.doctemplate.common.MergeSource;

import java.util.List;

/**
 * Copyright (c) 2016 DV Bern AG, Switzerland
 * <p>
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschuetzt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulaessig. Dies gilt
 * insbesondere fuer Vervielfaeltigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht uebergeben ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 * <p>
 * Created by medu on 28/11/2016.
 */
public class MahnungPrintMergeSource implements EBEGUMergeSource {

	private ManhungPrint mahnung;
	private boolean isPDFLongerThanExpected = false;

	public MahnungPrintMergeSource(ManhungPrint mahnung) {
		this.mahnung = mahnung;
	}

	@Override
	public Object getData(MergeContext mergeContext, String key) throws DocTemplateException {
		if (key.startsWith("mahnung")) {
			return new BeanMergeSource(mahnung, "mahnung.").getData(mergeContext, key);
		}
		return null;
	}

	@Override
	public Boolean ifStatement(MergeContext mergeContext, String key) throws DocTemplateException {
		if (key.equals("mahnung.PDFLongerThanExpected")) {
			return isPDFLongerThanExpected;
		}
		return new BeanMergeSource(mahnung, "mahnung.").ifStatement(mergeContext, key);
	}

	@Override
	public List<MergeSource> whileStatement(MergeContext mergeContext, String key) throws DocTemplateException {
		if (key.startsWith("mahnung")) {
			return new BeanMergeSource(mahnung, "mahnung.").whileStatement(mergeContext, key);
		}
		return null;
	}

	@Override
	public void setPDFLongerThanExpected(boolean isPDFLongerThanExpected) {
		this.isPDFLongerThanExpected = isPDFLongerThanExpected;
	}
}
