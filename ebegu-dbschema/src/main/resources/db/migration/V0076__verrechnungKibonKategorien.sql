alter table verrechnung_kibon_detail add total_bg bigint not null;
alter table verrechnung_kibon_detail add total_bg_ts bigint not null;
alter table verrechnung_kibon_detail add total_fi bigint not null;
alter table verrechnung_kibon_detail add total_fi_tagi bigint not null;
alter table verrechnung_kibon_detail add total_kein_angebot bigint not null;
alter table verrechnung_kibon_detail add total_kinder_verrechnet bigint not null;
alter table verrechnung_kibon_detail add total_tagi bigint not null;
alter table verrechnung_kibon_detail add total_ts bigint not null;

update verrechnung_kibon_detail set total_bg = total_kinder_verrechnet;

alter table verrechnung_kibon_detail drop total_kinder_verrechnet;