package com.arbit.app.auth.service;

import com.arbit.app.common.exception.BusinessException;
import com.arbit.app.common.exception.ErrorCode;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class DefaultResidentialLocationResolver implements ResidentialLocationResolver {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String KAKAO_AUTH_PREFIX = "KakaoAK ";
    private static final Logger log = LoggerFactory.getLogger(DefaultResidentialLocationResolver.class);

    private final RestClient restClient;
    private final KakaoLocalProperties kakaoLocalProperties;

    public DefaultResidentialLocationResolver(RestClient.Builder restClientBuilder,
                                              KakaoLocalProperties kakaoLocalProperties) {
        this.kakaoLocalProperties = kakaoLocalProperties;
        this.restClient = restClientBuilder
                .baseUrl(kakaoLocalProperties.baseUrl())
                .build();
    }

    @Override
    public ResidentialCoordinates resolve(String residentialArea) {
        if (!StringUtils.hasText(residentialArea)) {
            log.info("Signup address lookup skipped because residentialArea was blank.");
            return ResidentialCoordinates.empty();
        }

        log.info("Signup road address received: {}", residentialArea);

        if (!StringUtils.hasText(kakaoLocalProperties.restApiKey())) {
            log.error("Kakao map API key is missing. Unable to resolve road address: {}", residentialArea);
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "Failed to convert residentialArea because the Kakao map API key is not configured."
            );
        }

        try {
            KakaoAddressSearchResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/address.json")
                            .queryParam("query", residentialArea)
                            .build())
                    .header(AUTHORIZATION_HEADER, KAKAO_AUTH_PREFIX + kakaoLocalProperties.restApiKey())
                    .retrieve()
                    .body(KakaoAddressSearchResponse.class);

            log.info("Kakao map API connected successfully for road address: {}", residentialArea);

            if (response == null || response.documents() == null || response.documents().isEmpty()) {
                log.error("Kakao map API returned no address match for road address: {}", residentialArea);
                throw new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        "Failed to convert residentialArea because the address could not be resolved by the map API."
                );
            }

            KakaoAddressDocument document = response.documents().get(0);
            ResidentialCoordinates coordinates =
                    new ResidentialCoordinates(parseCoordinate(document.y()), parseCoordinate(document.x()));
            log.info(
                    "Imported residential coordinates from Kakao map API. address={}, latitude={}, longitude={}",
                    residentialArea,
                    coordinates.latitude(),
                    coordinates.longitude()
            );
            return coordinates;
        } catch (RestClientException | IllegalArgumentException exception) {
            log.error("Kakao map API address conversion failed for road address: {}", residentialArea, exception);
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "Failed to convert residentialArea because the map API response could not be processed."
            );
        }
    }

    private Double parseCoordinate(String coordinate) {
        if (!StringUtils.hasText(coordinate)) {
            return null;
        }
        return Double.parseDouble(coordinate);
    }

    private record KakaoAddressSearchResponse(List<KakaoAddressDocument> documents) {
    }

    private record KakaoAddressDocument(String x, String y) {
    }
}
