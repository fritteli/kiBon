ALTER TABLE adresse ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE adresse_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE antrag_status_history ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE antrag_status_history_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE application_property ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE application_property_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE authorisierter_benutzer ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE benutzer ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE benutzer_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE betreuung ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE betreuung_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE betreuungspensum ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE betreuungspensum_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE betreuungspensum_container ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE betreuungspensum_container_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE dokument ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE dokument_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE dokument_grund ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE dokument_grund_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE download_file ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE ebegu_parameter ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE ebegu_parameter_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE ebegu_vorlage ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE ebegu_vorlage_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE einkommensverschlechterung ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE einkommensverschlechterung_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE einkommensverschlechterung_container ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE einkommensverschlechterung_container_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE einkommensverschlechterung_info ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE einkommensverschlechterung_info_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE erwerbspensum ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE erwerbspensum_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE erwerbspensum_container ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE erwerbspensum_container_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE fachstelle ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE fachstelle_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE fall ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE fall_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE familiensituation ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE familiensituation_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE finanzielle_situation ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE finanzielle_situation_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE finanzielle_situation_container ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE finanzielle_situation_container_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE generated_dokument ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE generated_dokument_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE gesuch ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE gesuch_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE gesuchsperiode ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE gesuchsperiode_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE gesuchsteller ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE gesuchsteller_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE gesuchsteller_adresse ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE gesuchsteller_adresse_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE institution ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE institution_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE institution_stammdaten ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE institution_stammdaten_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE kind ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE kind_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE kind_container ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE kind_container_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE mandant ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE mandant_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE mutationsdaten ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE mutationsdaten_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE pensum_fachstelle ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE pensum_fachstelle_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE sequence ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE traegerschaft ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE traegerschaft_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE verfuegung ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE verfuegung_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE verfuegung_zeitabschnitt ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE verfuegung_zeitabschnitt_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE vorlage ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE vorlage_aud ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE wizard_step ADD COLUMN vorgaenger_id varchar(36);
ALTER TABLE wizard_step_aud ADD COLUMN vorgaenger_id varchar(36);