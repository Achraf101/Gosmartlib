package be.ap.backend.service;

import be.ap.backend.entity.School;
import be.ap.backend.model.OneRosterCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmartschoolTokenServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SchoolService schoolService;

    @InjectMocks
    private SmartschoolTokenService tokenService;

    private School school;
    private OneRosterCredentials credentials;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "scope", "openid");

        school = new School();
        ReflectionTestUtils.setField(school, "id", 1L);
        ReflectionTestUtils.setField(school, "ssSubdomain", "testschool");

        credentials = Mockito.mock(OneRosterCredentials.class);
    }

    // ---------------------------------------------------------------------------
    // Helpers to read internal caches without per-site unchecked-cast warnings
    // ---------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<Long, String> tokenCache() {
        return (ConcurrentHashMap<Long, String>) ReflectionTestUtils.getField(tokenService, "tokenCache");
    }

    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<Long, Instant> expiryCache() {
        return (ConcurrentHashMap<Long, Instant>) ReflectionTestUtils.getField(tokenService, "expiryCache");
    }

    // ---------------------------------------------------------------------------
    // getAccessToken – cache miss: fetches a new token
    // ---------------------------------------------------------------------------

    @Test
    void getAccessToken_whenNoCachedToken_fetchesNewToken() {
        mockSuccessfulTokenResponse("new-token-abc", 3600);

        String token = tokenService.getAccessToken(school);

        assertThat(token).isEqualTo("new-token-abc");
        verifyOneHttpCall();
    }

    // ---------------------------------------------------------------------------
    // getAccessToken – cache hit: returns cached token without HTTP call
    // ---------------------------------------------------------------------------

    @Test
    void getAccessToken_whenValidCachedToken_returnsCachedTokenWithoutHttpCall() {
        tokenCache().put(1L, "cached-token");
        expiryCache().put(1L, Instant.now().plusSeconds(300));

        String token = tokenService.getAccessToken(school);

        assertThat(token).isEqualTo("cached-token");
        verifyNoInteractions(restTemplate);
    }

    // ---------------------------------------------------------------------------
    // getAccessToken – cache expired: fetches a fresh token
    // ---------------------------------------------------------------------------

    @Test
    void getAccessToken_whenCachedTokenExpired_fetchesNewToken() {
        tokenCache().put(1L, "stale-token");
        expiryCache().put(1L, Instant.now().minusSeconds(600));

        mockSuccessfulTokenResponse("fresh-token", 3600);

        assertThat(tokenService.getAccessToken(school)).isEqualTo("fresh-token");
        verifyOneHttpCall();
    }

    // ---------------------------------------------------------------------------
    // getAccessToken – within the 60-second safety margin: refreshes proactively
    // ---------------------------------------------------------------------------

    @Test
    void getAccessToken_whenTokenWithin60SecondMargin_refreshesToken() {
        tokenCache().put(1L, "soon-expiring-token");
        expiryCache().put(1L, Instant.now().plusSeconds(30));

        mockSuccessfulTokenResponse("refreshed-token", 3600);

        assertThat(tokenService.getAccessToken(school)).isEqualTo("refreshed-token");
        verifyOneHttpCall();
    }

    // ---------------------------------------------------------------------------
    // fetchNewToken – correct token URL is built from subdomain
    // ---------------------------------------------------------------------------

    @Test
    void fetchNewToken_buildsCorrectTokenUrl() {
        mockSuccessfulTokenResponse("token", 3600);
        tokenService.getAccessToken(school);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), any(), eq(Map.class));

        assertThat(urlCaptor.getValue())
                .isEqualTo("https://testschool.smartschool.be/ims/oneroster/token");
    }

    // ---------------------------------------------------------------------------
    // fetchNewToken – request body contains expected OAuth2 fields
    // ---------------------------------------------------------------------------

    @Test
    void fetchNewToken_sendsCorrectRequestBody() {
        mockSuccessfulTokenResponse("token", 3600);
        tokenService.getAccessToken(school);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> requestCaptor = ArgumentCaptor
                .forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(anyString(), requestCaptor.capture(), eq(Map.class));

        MultiValueMap<String, String> body = requestCaptor.getValue().getBody();

        assertThat(body).isNotNull();
        assertThat(body.getFirst("grant_type")).isEqualTo("client_credentials");
        assertThat(body.getFirst("client_id")).isEqualTo("client-id-123");
        assertThat(body.getFirst("client_secret")).isEqualTo("client-secret-xyz");
        assertThat(body.getFirst("scope")).isEqualTo("openid");
    }

    // ---------------------------------------------------------------------------
    // fetchNewToken – Content-Type header is APPLICATION_FORM_URLENCODED
    // ---------------------------------------------------------------------------

    @Test
    void fetchNewToken_setsFormUrlencodedContentType() {
        mockSuccessfulTokenResponse("token", 3600);
        tokenService.getAccessToken(school);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> requestCaptor = ArgumentCaptor
                .forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(anyString(), requestCaptor.capture(), eq(Map.class));

        assertThat(requestCaptor.getValue().getHeaders().getContentType())
                .isEqualTo(MediaType.APPLICATION_FORM_URLENCODED);
    }

    // ---------------------------------------------------------------------------
    // fetchNewToken – token and expiry are stored in the caches
    // ---------------------------------------------------------------------------

    @Test
    void fetchNewToken_storesTokenAndExpiryInCache() {
        mockSuccessfulTokenResponse("cached-after-fetch", 7200);
        tokenService.getAccessToken(school);

        assertThat(tokenCache().get(1L)).isEqualTo("cached-after-fetch");
        assertThat(expiryCache().get(1L)).isAfter(Instant.now().plusSeconds(7100));
    }

    // ---------------------------------------------------------------------------
    // fetchNewToken – non-2xx response throws RuntimeException
    // ---------------------------------------------------------------------------

    @Test
    void fetchNewToken_whenNon2xxResponse_throwsRuntimeException() {
        when(schoolService.getCredentials(1L)).thenReturn(credentials);
        stubPostForEntity(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());

        assertThatThrownBy(() -> tokenService.getAccessToken(school))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("testschool");
    }

    // ---------------------------------------------------------------------------
    // fetchNewToken – null response body throws RuntimeException
    // ---------------------------------------------------------------------------

    @Test
    void fetchNewToken_whenNullResponseBody_throwsRuntimeException() {
        when(schoolService.getCredentials(1L)).thenReturn(credentials);
        stubPostForEntity(ResponseEntity.ok(null));

        assertThatThrownBy(() -> tokenService.getAccessToken(school))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("testschool");
    }

    // ---------------------------------------------------------------------------
    // fetchNewToken – second call for same school reuses cache (1 HTTP call total)
    // ---------------------------------------------------------------------------

    @Test
    void getAccessToken_calledTwiceForSameSchool_makesOnlyOneHttpRequest() {
        mockSuccessfulTokenResponse("shared-token", 3600);

        tokenService.getAccessToken(school);
        tokenService.getAccessToken(school);

        verifyOneHttpCall();
    }

    // ---------------------------------------------------------------------------
    // fetchNewToken – different schools get separate cache entries
    // ---------------------------------------------------------------------------

    @Test
    void getAccessToken_differentSchools_fetchSeparateTokens() {
        School school2 = new School();
        ReflectionTestUtils.setField(school2, "id", 2L);
        ReflectionTestUtils.setField(school2, "ssSubdomain", "otherschool");

        OneRosterCredentials creds2 = Mockito.mock(OneRosterCredentials.class);
        when(creds2.getClientId()).thenReturn("other-client");
        when(creds2.getClientSecret()).thenReturn("other-secret");

        when(schoolService.getCredentials(1L)).thenReturn(credentials);
        when(schoolService.getCredentials(2L)).thenReturn(creds2);

        stubPostForEntity("testschool",
                ResponseEntity.ok(Map.<String, Object>of("access_token", "token-1", "expires_in", 3600)));
        stubPostForEntity("otherschool",
                ResponseEntity.ok(Map.<String, Object>of("access_token", "token-2", "expires_in", 3600)));

        assertThat(tokenService.getAccessToken(school)).isEqualTo("token-1");
        assertThat(tokenService.getAccessToken(school2)).isEqualTo("token-2");

        verify(restTemplate, times(2)).postForEntity(anyString(), any(), eq(Map.class));
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private void mockSuccessfulTokenResponse(String token, int expiresIn) {
        when(schoolService.getCredentials(1L)).thenReturn(credentials);
        when(credentials.getClientId()).thenReturn("client-id-123"); // moved here
        when(credentials.getClientSecret()).thenReturn("client-secret-xyz"); // moved here
        stubPostForEntity(ResponseEntity.ok(
                Map.<String, Object>of("access_token", token, "expires_in", expiresIn)));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void stubPostForEntity(ResponseEntity<Map<String, Object>> response) {
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn((ResponseEntity) response);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void stubPostForEntity(String subdomainFragment,
            ResponseEntity<Map<String, Object>> response) {
        when(restTemplate.postForEntity(contains(subdomainFragment), any(), eq(Map.class)))
                .thenReturn((ResponseEntity) response);
    }

    private void verifyOneHttpCall() {
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(Map.class));
    }
}