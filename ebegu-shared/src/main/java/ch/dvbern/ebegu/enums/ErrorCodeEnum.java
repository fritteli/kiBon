/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.enums;

/**
 * Enum fuer moegliche Errors
 */
public enum ErrorCodeEnum {

	ERROR_ENTITY_NOT_FOUND,
	ERROR_ENTITY_EXISTS,
	ERROR_BENUTZER_EXISTS,
	ERROR_BENUTZER_STATUS_NOT_EINGELADEN,
	ERROR_PARAMETER_NOT_FOUND,
	ERROR_TOO_MANY_RESULTS,
	ERROR_PRINT_PDF,
	ERROR_MAIL,
	ERROR_EXISTING_ONLINE_MUTATION,
	ERROR_EXISTING_MUTATION_VERANLAGUNGSMITTEILUNG,
	ERROR_EXISTING_ERNEUERUNGSGESUCH,
	ERROR_ZAHLUNG_ERSTELLEN,
	ERROR_ONLY_VERFUEGT_OR_SCHULAMT_ALLOWED,
	ERROR_ONLY_SCHULAMT_NOT_ALLOWED,
	ERROR_ONLY_IN_BEARBEITUNG_STV_ALLOWED,
	ERROR_ONLY_IN_GEPRUEFT_STV_ALLOWED,
	ERROR_ONLY_IN_PRUEFUNG_GEPRUEFT_STV_ALLOWED,
	ERROR_ONLY_IN_GEPRUEFT_ALLOWED,
	ERROR_ONLY_IF_NO_BETERUUNG,
	ERROR_PERSONENSUCHE_BUSINESS,
	ERROR_PERSONENSUCHE_TECHNICAL,
	ERROR_NOT_FROM_STATUS_BESCHWERDE,
	ERROR_LINKED_BERECHTIGUNGEN,
	ERROR_GESUCHSPERIODE_INVALID_STATUSUEBERGANG,
	ERROR_GESUCHSPERIODE_CANNOT_BE_REMOVED,
	ERROR_GESUCHSPERIODE_CANNOT_BE_CLOSED,
	ERROR_GESUCHSPERIODE_MUST_EXIST,
	ERROR_MUTATIONSMELDUNG_FALL_GESPERRT,
	ERROR_MUTATIONSMELDUNG_GESUCH_NICHT_FREIGEGEBEN_INBEARBEITUNG,
	ERROR_MUTATIONSMELDUNG_STATUS_VERFUEGEN,
	ERROR_VORGAENGER_MISSING,
	ERROR_ANTRAG_NOT_COMPLETE,
	ERROR_EXISTING_MAHNUNG,
	ERROR_INVALID_EBEGUSTATE,
	ERROR_FREIGABEQUITTUNG_PAPIER,
	ERROR_DELETION_NOT_ALLOWED_FOR_JA,
	ERROR_DELETION_NOT_ALLOWED_FOR_GS,
	ERROR_DELETION_ANTRAG_NOT_ALLOWED,
	ERROR_UPLOAD_INVALID_FILETYPE,
	ERROR_FIN_SIT_IS_NOT_REQUIRED,
	ERROR_JOB_ALREADY_EXISTS,
	ERROR_DUPLICATE_BETREUUNG,
	ERROR_VERANTWORTLICHER_NOT_FOUND,
	ERROR_EMPFAENGER_NOT_FOUND,
	ERROR_TESTFAELLE_DISABLED,
	ERROR_EINSTELLUNG_NOT_FOUND,
	ERROR_DUPLICATE_GEMEINDE_NAME,
	ERROR_DUPLICATE_GEMEINDE_BSF,
	ERROR_WRONG_EXISTING_ROLE,
	EXISTING_USER_MAIL,
	ERROR_EMAIL_MISMATCH,
	ERROR_PENDING_INVITATION,
	ERROR_INVALID_CONFIGURATION,
	ERROR_NO_GEMEINDE_STAMMDATEN,
	ERROR_ZAHLUNGSINFORMATIONEN_GEMEINDE_INCOMPLETE,
	ERROR_ZAHLUNGSINFORMATIONEN_INSTITUTION_INCOMPLETE,
	ERROR_ZAHLUNGSINFORMATIONEN_ANTRAGSTELLER_INCOMPLETE,
	ERROR_INFOMA_BELEGNUMMER_MAX_REACHED,
	ERROR_GESUCH_DURCH_GS_ZURUECKGEZOGEN,
	ERROR_GESUCHSTELLER_EXIST_NO_GESUCH,
	ERROR_GESUCHSTELLER_EXIST_WITH_GESUCH,
	ERROR_GESUCHSTELLER_EXIST_WITH_FREGEGEBENE_GESUCH,
	ERROR_ANMELDUNGEN_EXISTS,
	ERROR_ANMELDUNG_KEINE_MODULE,
	ERROR_LASTENAUSGLEICH_GRUNDLAGEN_EXISTS,
	ERROR_LASTENAUSGLEICH_EXISTS,
	ERROR_OBJECT_IS_IMMUTABLE,
	ERROR_NOT_SUPPORTED_IMAGE,
	ERROR_ERSTGESUCH_ALREADY_EXISTS,
	ERROR_OEFFNUNGSZEITEN_NOT_FOUND,
	ERROR_KORREKTURMODUS_FI_ONLY,
	ERROR_MALICIOUS_CONTENT,
	ERROR_INVALID_EXTERNAL_CLIENT_DATERANGE,
	ERROR_WRONG_FORMAT_ZEMIS,
	ERROR_DUPLICATE_SOZIALDIENST_NAME,
	ERROR_MASSENVERSAND_VERANTWORTLICHKEIT_FEHLT,
	ERROR_GEMEINDE_ANTRAG_NEU,
	ERROR_UNBEKANNTE_DB_CONSTRAINT,
	ERROR_UK_TRAEGERSCHAFT_NAME,
	ERROR_UK_SOZIALDIENST_NAME,
	ERROR_NOCH_NICHT_FREIGEGEBENE_ANTRAG,
	ERROR_UD_FALL_ENTZOGEN_DELETION_NOT_ALLOWED,
	ERROR_LATS_VERFUEGUNG_TEMPLATE_NOT_FOUND,
	ERROR_VERFUEGUNG_PLACEHOLDER_NOT_FOUND,
	ERROR_FERIENBETREUUNG_VERFUEGUNG_TEMPLATE_NOT_FOUND,
	ERROR_LATS_ANGABEN_INCOMPLETE,
	ERROR_FERIENBETREUUNG_ALREADY_EXISTS,
	ERROR_ZAHLUNGSAUFTRAG_GENERIERT_BEFORE_LAST_ZAHLUNGSAUFTRAG_GENERIERT,
	ERROR_UK_FREMD_ID_GESUSCHPERIODE_INSTITUTION,
	ERROR_MANDANT_COOKIE_IS_NULL,
	ERROR_BETREUUNG_STORNIERT_UND_UNGUELTIG,
	ERROR_KIBON_ANFRAGE_TECHNICAL,
	ERROR_FIN_SIT_GEMEINSAM_NEUE_VERANLAGUNG_ALLEIN,
	ERROR_FIN_SIT_ALLEIN_NEUE_VERANLAGUNG_GEMEINSAM,
	ERROR_NICHT_IGNORIERTER_VORGAENGER_NOT_FOUND,
	ERROR_FIN_SIT_MANUELLE_EINGABE;
}
