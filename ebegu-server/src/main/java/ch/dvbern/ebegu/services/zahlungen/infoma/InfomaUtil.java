package ch.dvbern.ebegu.services.zahlungen.infoma;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

public final class InfomaUtil {

	private InfomaUtil() {
	}

	@Nonnull
	public static String normalizeAndAbbreviate(@Nullable String text, int maxLength) {
		text = normalize(text);
		// Kuerzen auf die maximal zugelassene Laenge
		text = StringUtils.abbreviate(text, maxLength);
		return text;
	}

	@SuppressWarnings({ "DynamicRegexReplaceableByCompiledPattern" })
	@Nonnull
	public static String normalize(@Nullable String text) {
		if (text == null) {
			return "";
		}
		// Konvertierung nach ASCII. Muss VOR dem eventuellen Kuerzen auf die MaxLaenge passieren.
		// TODO Wie genau??? Warte auf Antwort.
		text = AsciiHelper.transformToAsciiString(text);
		// Der Separator (|) darf im Text nicht vorkommen. Es gibt kein Escaping
		text = text.replaceAll("\\|", "");
		// Kuerzen auf die maximal zugelassene Laenge
		return text;
	}
}