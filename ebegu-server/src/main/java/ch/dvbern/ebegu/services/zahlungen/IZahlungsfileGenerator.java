package ch.dvbern.ebegu.services.zahlungen;

import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;

public interface IZahlungsfileGenerator {
	byte[] generateZahlungfile(
		@Nonnull Zahlungsauftrag zahlungsauftrag,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Locale locale
	);
}
