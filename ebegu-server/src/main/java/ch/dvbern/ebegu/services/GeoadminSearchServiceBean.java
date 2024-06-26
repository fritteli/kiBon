/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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
package ch.dvbern.ebegu.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.dto.geoadmin.JaxGeoadminFeature;
import ch.dvbern.ebegu.dto.geoadmin.JaxGeoadminFeatureAttributes;
import ch.dvbern.ebegu.dto.geoadmin.JaxGeoadminFeatureResult;
import ch.dvbern.ebegu.dto.geoadmin.JaxGeoadminSearchResult;
import ch.dvbern.ebegu.dto.geoadmin.JaxGeoadminSearchResultEntry;
import ch.dvbern.ebegu.dto.geoadmin.JaxWohnadresse;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Uses some of the functions of the
 * <a href="http://api3.geo.admin.ch/services/sdiservices.html">GeoAdmin API REST Services</a>.
 */
@Stateless
@Local(GeoadminSearchService.class)
public class GeoadminSearchServiceBean extends AbstractBaseService implements GeoadminSearchService {

	private static final Logger LOG = LoggerFactory.getLogger(GeoadminSearchServiceBean.class);

	@Nonnull
	private static final Comparator<JaxGeoadminSearchResultEntry> RESULTS_COMPARATOR = Comparator
		.comparing(JaxGeoadminSearchResultEntry::getWeight);

	private static final String API_LAYERBODID_WOHNUNGSREGISTER = "ch.bfs.gebaeude_wohnungs_register";

	// Seit dem Release vom 24.02.21 gibt die GeoAdmin API einem exact match die weight 100
	private static final Long EXACT_MATCH = (long) 100;    // highest weight
	private static final Long EXACT_MATCH_LEGACY = (long) 14;    // legacy highest weight from experiments
	private static final int SEARCHSERVICE_MAX_RESULTS = 50; // Max fuer die Search API

	private static final Pattern ILLEGAL_SEARCH_CHARS = Pattern.compile("[.]");
	private static final String EMPTY_STRING = "";

	private Client client = null;

	@Inject
	private EbeguConfiguration config;

	@PostConstruct
	public void postConstruct() {
		client = ClientBuilder.newClient();
	}

	@PreDestroy
	protected void preDestroy() {
		client.close();
	}

	/**
	 * Sucht in der GeoAdmin API Wohnadressen, die mit strasse, nr und plz übereinstimmen. Liefert die Resultate
	 * absteigend nach Relevanz geordnet.
	 */
	@Override
	@Nonnull
	public List<JaxWohnadresse> findWohnadressenByStrasseAndPlz(
		@Nonnull String strasse,
		@Nullable String nr,
		@Nonnull String plz) {
		String nrStr = "";
		if (nr != null) {
			nrStr = nr + " ";
		}
		return this.findWohnadressenBySearchText(strasse + " " + nrStr + plz);
	}

	@Override
	@Nonnull
	public List<JaxWohnadresse> findWohnadressenBySearchText(@Nonnull String searchText) {
		checkNotNull(searchText);

		JaxGeoadminSearchResult searchResult = searchAddress(searchText, SEARCHSERVICE_MAX_RESULTS);
		boolean fuzzy = searchResult.isFuzzy();

		Optional<JaxGeoadminSearchResultEntry> exactGeoadminAddress = searchResult.getResults().stream()
			.filter(a -> EXACT_MATCH.equals(a.getWeight()) || EXACT_MATCH_LEGACY.equals(a.getWeight()))
			.findFirst();

		if (exactGeoadminAddress.isPresent()) {
			String featureId = exactGeoadminAddress.get().getAttrs().getFeatureId();

			return searchAdressFromFeature(fuzzy, featureId)
				.map(Collections::singletonList)
				.orElseGet(Collections::emptyList);
		}

		return searchResult.getResults().stream()
			.map(a -> searchAdressFromFeature(fuzzy, a.getAttrs().getFeatureId()))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());
	}

	/**
	 * Ziel ist die <a href="https://api3.geo.admin.ch/services/sdiservices.html#search">Search Resource</a> vom type
	 * &quot;locations&quot;
	 * Das Ergebnis dieser Resource mit origins=address (i.E.: Adress-Suche) liefert uns ein Suchergebnis aus dem
	 * {@link #API_LAYERBODID_WOHNUNGSREGISTER}.
	 */
	@Nonnull
	private WebTarget getLocationsSearchTarget(
		@Nonnull String escapedSearchString,
		int limit) {

		if (limit > SEARCHSERVICE_MAX_RESULTS) {
			throw new EbeguRuntimeException(
				"getLocationsSearchTarget",
				"limit " + limit + "higher than SEARCH_SERVICE_MAX_RESULTS (" + SEARCHSERVICE_MAX_RESULTS + ")");
		}

		String searchserverURI = config.getEbeguGeoadminSearchServerUrl();

		return client.target(searchserverURI)
			.queryParam("type", "locations")
			.queryParam("origins", "address")
			.queryParam("returnGeometry", false)
			.queryParam("limit", limit)
			.queryParam("searchText", escapedSearchString);
	}

	/**
	 * Liefert alle Results aus dem Wohnungsregister
	 */
	@Nonnull
	private JaxGeoadminSearchResult searchAddress(@Nonnull String searchText, int limit) {
		checkNotNull(searchText);

		// Die GeoAdmin API vergleicht case sensitive für den exakten match
		String escapedSearchString =
			ILLEGAL_SEARCH_CHARS.matcher(searchText).replaceAll(EMPTY_STRING).trim().toLowerCase(Locale.GERMAN);

		if (escapedSearchString.isEmpty()) {
			return new JaxGeoadminSearchResult();
		}

		WebTarget target = getLocationsSearchTarget(escapedSearchString, limit);

		try {
			JaxGeoadminSearchResult geoadminSearchResult = target
				.request(MediaType.APPLICATION_JSON)
				.get(JaxGeoadminSearchResult.class);

			geoadminSearchResult.getResults().sort(RESULTS_COMPARATOR.reversed());

			return geoadminSearchResult;

		} catch (ServiceUnavailableException ex) {
			// web service is unavailable, return empty result
			LOG.error(String.format("Error while requesting the URI: %s", target.getUri()), ex);

			return new JaxGeoadminSearchResult();
		}
	}

	/**
	 * Ziel ist die <a href="https://api3.geo.admin.ch/services/sdiservices.html#feature-resource">Feature Resource</a>
	 */
	@Nonnull
	private WebTarget getFeatureTarget(@Nonnull String featureId) {
		checkNotNull(featureId);

		String mapserverURI = config.getEbeguGeoadminMapServerUrl();

		return client.target(mapserverURI)
			.path(GeoadminSearchServiceBean.API_LAYERBODID_WOHNUNGSREGISTER)
			.path(featureId)
			.queryParam("returnGeometry", false);
	}

	@Nonnull
	private Optional<JaxWohnadresse> searchAdressFromFeature(
		boolean fuzzy,
		@Nonnull String featureId) {
		checkNotNull(featureId);

		WebTarget target = getFeatureTarget(featureId);

		try {
			JaxGeoadminFeatureResult featureResult = target
				.request(MediaType.APPLICATION_JSON)
				.get(JaxGeoadminFeatureResult.class);

			return fromFeature(featureResult.getFeature(), fuzzy);

		} catch (ServiceUnavailableException ex) {
			// web service is unavailable, return empty result
			LOG.error(String.format("Error while requesting the URI: %s", target.getUri()), ex);

			return Optional.empty();
		}
	}

	@Nonnull
	private Optional<JaxWohnadresse> fromFeature(@Nonnull JaxGeoadminFeature feature, boolean fuzzy) {
		JaxGeoadminFeatureAttributes attributes = feature.getAttributes();

		if (StringUtils.isEmpty(attributes.getGgdename())) {
			return Optional.empty();
		}
		if (ArrayUtils.isEmpty(attributes.getStrname())) {
			return Optional.empty();
		}

		JaxWohnadresse adresse = new JaxWohnadresse(
			fuzzy,
			feature.getLayerBodId() + ':' + feature.getFeatureId(),
			attributes.getStrname()[0],
			attributes.getDeinr(),
			attributes.getDplz4(),
			attributes.getDplzname(),
			attributes.getGgdenr(),
			attributes.getGgdename());

		return Optional.of(adresse);
	}
}
