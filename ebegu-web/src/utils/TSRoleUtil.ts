/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {Permission} from '../app/authorisation/Permission';
import {PERMISSIONS} from '../app/authorisation/Permissions';
import {getTSRoleValues, getTSRoleValuesWithoutSuperAdmin, rolePrefix, TSRole} from '../models/enums/TSRole';

/**
 * Hier findet man unterschiedliche Hilfsmethoden, um die Rollen von TSRole zu holen
 */
export class TSRoleUtil {

    public static getAllRolesButGesuchsteller(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getAllRolesButAnonymous().filter(element => element !== TSRole.GESUCHSTELLER);
    }

    // noinspection JSUnusedGlobalSymbols Es wird doch benutzt
    public static getAllRolesButSchulamtAndSuperAdmin(): ReadonlyArray<TSRole> {
        return getTSRoleValuesWithoutSuperAdmin()
            .filter(role => !TSRoleUtil.getSchulamtOnlyRoles().includes(role));
    }

    // noinspection JSUnusedGlobalSymbols Es wird doch benutzt
    public static getAllRolesForMenuAlleVerfuegungen(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getAllRolesButAnonymous()
            .filter(element => element !== TSRole.SACHBEARBEITER_TS
                && element !== TSRole.ADMIN_TS
                && element !== TSRole.STEUERAMT);
    }

    public static getAllRolesForMenuAlleFaelle(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getAllRolesButAnonymous()
            .filter(element => element !== TSRole.GESUCHSTELLER && element !== TSRole.STEUERAMT);
    }

    public static getAllRolesForZahlungen(): ReadonlyArray<TSRole> {
        return [
            TSRole.ADMIN_BG,
            TSRole.SUPER_ADMIN,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.ADMIN_INSTITUTION,
            TSRole.SACHBEARBEITER_INSTITUTION,
            TSRole.ADMIN_TRAEGERSCHAFT,
            TSRole.SACHBEARBEITER_TRAEGERSCHAFT,
            TSRole.REVISOR,
            TSRole.JURIST,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
        ];
    }

    public static getAllRolesForStatistik(): ReadonlyArray<TSRole> {
        return [
            TSRole.ADMIN_BG,
            TSRole.SUPER_ADMIN,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.ADMIN_TRAEGERSCHAFT,
            TSRole.SACHBEARBEITER_TRAEGERSCHAFT,
            TSRole.ADMIN_INSTITUTION,
            TSRole.SACHBEARBEITER_INSTITUTION,
            TSRole.ADMIN_TS,
            TSRole.SACHBEARBEITER_TS,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
        ];
    }

    public static getAllRoles(): ReadonlyArray<TSRole> {
        return getTSRoleValues();
    }

    public static getAllRolesButAnonymous(): ReadonlyArray<TSRole> {
        return getTSRoleValues().filter(role => role !== TSRole.ANONYMOUS);
    }

    public static getSuperAdminRoles(): ReadonlyArray<TSRole> {
        return [TSRole.SUPER_ADMIN];
    }

    public static getAdministratorRoles(): ReadonlyArray<TSRole> {
        return [TSRole.SUPER_ADMIN, TSRole.ADMIN_BG, TSRole.ADMIN_GEMEINDE, TSRole.ADMIN_TS, TSRole.ADMIN_MANDANT];
    }

    // noinspection JSUnusedGlobalSymbols Es wird doch benutzt
    public static getSchulamtAdministratorRoles(): ReadonlyArray<TSRole> {
        return [TSRole.SUPER_ADMIN, TSRole.ADMIN_TS, TSRole.ADMIN_GEMEINDE];
    }

    public static getJAAdministratorRoles(): ReadonlyArray<TSRole> {
        return [TSRole.SUPER_ADMIN, TSRole.ADMIN_BG, TSRole.ADMIN_GEMEINDE];
    }

    public static getInstitutionProfilRoles(): ReadonlyArray<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_INSTITUTION,
            TSRole.ADMIN_TRAEGERSCHAFT,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.ADMIN_BG,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_TS,
            TSRole.SACHBEARBEITER_TS,
        ];
    }

    public static getInstitutionProfilEditRoles(): ReadonlyArray<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_INSTITUTION,
            TSRole.ADMIN_TRAEGERSCHAFT,
        ];
    }

    public static getTraegerschaftInstitutionRoles(): ReadonlyArray<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_INSTITUTION,
            TSRole.SACHBEARBEITER_INSTITUTION,
            TSRole.ADMIN_TRAEGERSCHAFT,
            TSRole.SACHBEARBEITER_TRAEGERSCHAFT,
        ];
    }

    // noinspection JSUnusedGlobalSymbols Es wird doch benutzt
    public static getTraegerschaftInstitutionSchulamtRoles(): ReadonlyArray<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_INSTITUTION,
            TSRole.SACHBEARBEITER_INSTITUTION,
            TSRole.ADMIN_TRAEGERSCHAFT,
            TSRole.SACHBEARBEITER_TRAEGERSCHAFT,
            TSRole.SACHBEARBEITER_TS,
            TSRole.ADMIN_TS,
            TSRole.ADMIN_GEMEINDE,
        ];
    }

    // noinspection JSUnusedGlobalSymbols Es wird doch benutzt
    public static getTraegerschaftRoles(): ReadonlyArray<TSRole> {
        return [TSRole.SUPER_ADMIN, TSRole.ADMIN_TRAEGERSCHAFT, TSRole.SACHBEARBEITER_TRAEGERSCHAFT];
    }

    // noinspection JSUnusedGlobalSymbols Es wird doch benutzt
    public static getTraegerschaftInstitutionSteueramtOnlyRoles(): ReadonlyArray<TSRole> {
        return [
            TSRole.ADMIN_INSTITUTION,
            TSRole.SACHBEARBEITER_INSTITUTION,
            TSRole.ADMIN_TRAEGERSCHAFT,
            TSRole.SACHBEARBEITER_TRAEGERSCHAFT,
            TSRole.STEUERAMT,
        ];
    }

    public static getGesuchstellerJugendamtRoles(): ReadonlyArray<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.GESUCHSTELLER,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
        ];
    }

    public static getGesuchstellerJugendamtSchulamtRoles(): ReadonlyArray<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.GESUCHSTELLER,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.SACHBEARBEITER_TS,
            TSRole.ADMIN_TS,
        ];
    }

    public static getAdministratorJugendamtRole(): ReadonlyArray<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
        ];
    }

    public static getAdministratorBgTsGemeindeRole(): ReadonlyArray<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.ADMIN_TS,
            TSRole.ADMIN_GEMEINDE,
        ];
    }

    public static getAdministratorOrAmtRole(): ReadonlyArray<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.ADMIN_TS,
            TSRole.SACHBEARBEITER_TS,
        ];
    }

    public static getAdministratorJugendamtSchulamtRoles(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getAdministratorOrAmtRole();
    }

    public static getAdministratorRevisorRole(): ReadonlyArray<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.ADMIN_TS,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
        ];
    }

    public static getAdministratorMandantRevisorRole(): ReadonlyArray<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.ADMIN_TS,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
        ];
    }

    public static getAllAdministratorRevisorRole(): ReadonlyArray<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.ADMIN_TS,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
            TSRole.ADMIN_INSTITUTION,
            TSRole.ADMIN_TRAEGERSCHAFT,
        ];
    }

    public static getAllAdminRoles(): ReadonlyArray<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.ADMIN_TS,
            TSRole.ADMIN_MANDANT,
            TSRole.ADMIN_INSTITUTION,
            TSRole.ADMIN_TRAEGERSCHAFT,
        ];
    }

    public static getGesuchstellerJugendamtSchulamtOtherAmtRoles(): ReadonlyArray<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.JURIST,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
            TSRole.GESUCHSTELLER,
            TSRole.ADMIN_TS,
            TSRole.SACHBEARBEITER_TS,
        ];
    }

    // noinspection JSUnusedGlobalSymbols Es wird doch benutzt
    public static getGesuchstellerJugendamtOtherAmtRoles(): ReadonlyArray<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.JURIST,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
            TSRole.GESUCHSTELLER,
        ];
    }

    public static getMandantRoles(): ReadonlyArray<TSRole> {
        return [TSRole.SUPER_ADMIN, TSRole.ADMIN_MANDANT, TSRole.SACHBEARBEITER_MANDANT];
    }

    public static getJugendamtAndSchulamtRole(): ReadonlyArray<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.JURIST,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
            TSRole.ADMIN_TS,
            TSRole.SACHBEARBEITER_TS,
        ];
    }

    // noinspection JSUnusedGlobalSymbols Es wird doch benutzt
    public static getAdministratorJugendamtSchulamtSteueramtRoles(): ReadonlyArray<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.ADMIN_TS,
            TSRole.SACHBEARBEITER_TS,
            TSRole.STEUERAMT,
        ];
    }

    public static getAdministratorJugendamtSchulamtGesuchstellerRoles(): ReadonlyArray<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.ADMIN_TS,
            TSRole.SACHBEARBEITER_TS,
            TSRole.GESUCHSTELLER,
        ];
    }

    public static getAllButAdministratorJugendamtRole(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getAllRolesButAnonymous()
            .filter(element =>
                element !== TSRole.SACHBEARBEITER_BG &&
                element !== TSRole.ADMIN_BG &&
                element !== TSRole.SACHBEARBEITER_GEMEINDE &&
                element !== TSRole.ADMIN_GEMEINDE &&
                element !== TSRole.SUPER_ADMIN,
            );
    }

    // noinspection JSUnusedGlobalSymbols Es wird doch benutzt
    public static getAllButAdministratorAmtRole(): ReadonlyArray<TSRole> {
        return getTSRoleValues()
            .filter(element =>
                element !== TSRole.SACHBEARBEITER_BG &&
                element !== TSRole.ADMIN_BG &&
                element !== TSRole.SACHBEARBEITER_GEMEINDE &&
                element !== TSRole.ADMIN_GEMEINDE &&
                element !== TSRole.SACHBEARBEITER_TS &&
                element !== TSRole.ADMIN_TS &&
                element !== TSRole.SUPER_ADMIN,
            );
    }

    public static getAllRolesButTraegerschaftInstitution(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getAllRolesButAnonymous()
            .filter(element =>
                element !== TSRole.ADMIN_INSTITUTION
                && element !== TSRole.SACHBEARBEITER_INSTITUTION
                && element !== TSRole.ADMIN_TRAEGERSCHAFT
                && element !== TSRole.SACHBEARBEITER_TRAEGERSCHAFT,
            );
    }

    public static getAllRolesButTraegerschaftInstitutionSteueramt(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getAllRolesButAnonymous()
            .filter(element =>
                element !== TSRole.ADMIN_INSTITUTION
                && element !== TSRole.SACHBEARBEITER_INSTITUTION
                && element !== TSRole.ADMIN_TRAEGERSCHAFT
                && element !== TSRole.SACHBEARBEITER_TRAEGERSCHAFT
                && element !== TSRole.STEUERAMT,
            );
    }

    public static getAllRolesButSteueramt(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getAllRolesButAnonymous().filter(element => element !== TSRole.STEUERAMT);
    }

    public static getSchulamtRoles(): ReadonlyArray<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.SACHBEARBEITER_TS,
            TSRole.ADMIN_TS,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
        ];
    }

    public static getSchulamtOnlyRoles(): ReadonlyArray<TSRole> {
        return [TSRole.SACHBEARBEITER_TS, TSRole.ADMIN_TS];
    }

    public static getGesuchstellerRoles(): ReadonlyArray<TSRole> {
        return [TSRole.SUPER_ADMIN, TSRole.GESUCHSTELLER];
    }

    public static getGesuchstellerOnlyRoles(): ReadonlyArray<TSRole> {
        return [TSRole.GESUCHSTELLER];
    }

    public static getSteueramtOnlyRoles(): ReadonlyArray<TSRole> {
        return [TSRole.STEUERAMT];
    }

    public static getSteueramtRoles(): ReadonlyArray<TSRole> {
        return [TSRole.SUPER_ADMIN, TSRole.STEUERAMT];
    }

    public static getReadOnlyRoles(): ReadonlyArray<TSRole> {
        return [TSRole.REVISOR, TSRole.JURIST, TSRole.STEUERAMT, TSRole.ADMIN_MANDANT, TSRole.SACHBEARBEITER_MANDANT];
    }

    // noinspection JSUnusedGlobalSymbols Es wird doch benutzt
    public static getAllRolesForKommentarSpalte(): ReadonlyArray<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.STEUERAMT,
            TSRole.ADMIN_TS,
            TSRole.SACHBEARBEITER_TS,
            TSRole.JURIST,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
        ];
    }

    // noinspection JSUnusedGlobalSymbols Es wird doch benutzt
    public static getGemeindeRoles(): ReadonlyArray<TSRole> {
        return PERMISSIONS[Permission.ROLE_GEMEINDE].concat(TSRole.SUPER_ADMIN);
    }

    public static getTraegerschaftInstitutionOnlyRoles(): ReadonlyArray<TSRole> {
        return PERMISSIONS[Permission.ROLE_INSTITUTION].concat(PERMISSIONS[Permission.ROLE_TRAEGERSCHAFT]);
    }

    public static getInstitutionRoles(): ReadonlyArray<TSRole> {
        return PERMISSIONS[Permission.ROLE_INSTITUTION].concat(TSRole.SUPER_ADMIN);
    }

    public static isGemeindeRole(role: TSRole): boolean {
        return PERMISSIONS[Permission.ROLE_GEMEINDE].includes(role);
    }

    public static isInstitutionRole(role: TSRole): boolean {
        return PERMISSIONS[Permission.ROLE_INSTITUTION].includes(role);
    }

    public static isTraegerschaftRole(role: TSRole): boolean {
        return PERMISSIONS[Permission.ROLE_TRAEGERSCHAFT].includes(role);
    }

    public static translationKeyForRole(role: TSRole, gesuchstellerNone: boolean = false): string {
        return role === TSRole.GESUCHSTELLER && gesuchstellerNone ? rolePrefix() + 'NONE' : rolePrefix() + role;
    }
}
