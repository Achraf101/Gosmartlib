// package be.ap.backend.service;

// import be.ap.backend.entity.School;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.http.*;
// import org.springframework.web.client.RestClientException;
// import org.springframework.web.client.RestTemplate;

// import java.util.Map;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class SmartschoolLookupServiceTest {

// @Mock
// private SmartschoolTokenService tokenService;

// @Mock
// private RestTemplate restTemplate;

// @InjectMocks
// private SmartschoolLookupService service;

// private School school;

// @BeforeEach
// void setUp() {
// school = new School();
// school.setSsSubdomain("testschool");
// }

// // -------------------------------------------------------------------------
// // getUser — student
// // -------------------------------------------------------------------------

// @Test
// void getUser_student_returnsUserBlock() {
// String token = "tok-student";
// Map<String, Object> innerUser = Map.of("sourcedId", "s1", "givenName",
// "Alice");
// Map<String, Object> body = Map.of("user", innerUser);

// when(tokenService.getAccessToken(school)).thenReturn(token);
// when(restTemplate.exchange(
// eq("https://testschool.smartschool.be/ims/oneroster/v1p1/students/s1"),
// eq(HttpMethod.GET),
// any(HttpEntity.class),
// eq(Map.class))).thenReturn(ResponseEntity.ok(body));

// Map<String, Object> result = service.getUser(school, "s1", "student");

// assertThat(result).isEqualTo(innerUser);
// }

// @Test
// void getUser_teacher_usesTeachersEndpoint() {
// String token = "tok-teacher";
// Map<String, Object> innerUser = Map.of("sourcedId", "t1", "givenName",
// "Bob");
// Map<String, Object> body = Map.of("user", innerUser);

// when(tokenService.getAccessToken(school)).thenReturn(token);
// when(restTemplate.exchange(
// eq("https://testschool.smartschool.be/ims/oneroster/v1p1/teachers/t1"),
// eq(HttpMethod.GET),
// any(HttpEntity.class),
// eq(Map.class))).thenReturn(ResponseEntity.ok(body));

// Map<String, Object> result = service.getUser(school, "t1", "teacher");

// assertThat(result).isEqualTo(innerUser);
// }

// @Test
// void getUser_noUserKey_returnsFlatBody() {
// // When the response has no "user" key, getOrDefault falls back to the whole
// map
// Map<String, Object> body = Map.of("sourcedId", "s2", "givenName", "Carol");

// when(tokenService.getAccessToken(school)).thenReturn("tok");
// when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
// any(HttpEntity.class), eq(Map.class)))
// .thenReturn(ResponseEntity.ok(body));

// Map<String, Object> result = service.getUser(school, "s2", "student");

// assertThat(result).isEqualTo(body);
// }

// @Test
// void getUser_restTemplateThrows_returnsNull() {
// when(tokenService.getAccessToken(school)).thenReturn("tok");
// when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
// any(HttpEntity.class), eq(Map.class)))
// .thenThrow(new RestClientException("connection refused"));

// Map<String, Object> result = service.getUser(school, "s3", "student");

// assertThat(result).isNull();
// }

// @Test
// void getUser_nullBody_returnsNull() {
// when(tokenService.getAccessToken(school)).thenReturn("tok");
// when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
// any(HttpEntity.class), eq(Map.class)))
// .thenReturn(ResponseEntity.ok(null));

// Map<String, Object> result = service.getUser(school, "s4", "student");

// assertThat(result).isNull();
// }

// // -------------------------------------------------------------------------
// // getClassroom
// // -------------------------------------------------------------------------

// @Test
// void getClassroom_returnsClassBlock() {
// Map<String, Object> innerClass = Map.of("sourcedId", "cls1", "title", "Math
// 101");
// Map<String, Object> body = Map.of("class", innerClass);

// when(tokenService.getAccessToken(school)).thenReturn("tok");
// when(restTemplate.exchange(
// eq("https://testschool.smartschool.be/ims/oneroster/v1p1/classes/cls1"),
// eq(HttpMethod.GET),
// any(HttpEntity.class),
// eq(Map.class))).thenReturn(ResponseEntity.ok(body));

// Map<String, Object> result = service.getClassroom(school, "cls1");

// assertThat(result).isEqualTo(innerClass);
// }

// @Test
// void getClassroom_noClassKey_returnsNull() {
// // Body exists but has no "class" key → Map.get returns null
// Map<String, Object> body = Map.of("something", "else");

// when(tokenService.getAccessToken(school)).thenReturn("tok");
// when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
// any(HttpEntity.class), eq(Map.class)))
// .thenReturn(ResponseEntity.ok(body));

// Map<String, Object> result = service.getClassroom(school, "cls2");

// assertThat(result).isNull();
// }

// @Test
// void getClassroom_restTemplateThrows_returnsNull() {
// when(tokenService.getAccessToken(school)).thenReturn("tok");
// when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
// any(HttpEntity.class), eq(Map.class)))
// .thenThrow(new RestClientException("timeout"));

// Map<String, Object> result = service.getClassroom(school, "cls3");

// assertThat(result).isNull();
// }

// @Test
// void getClassroom_nullBody_returnsNull() {
// when(tokenService.getAccessToken(school)).thenReturn("tok");
// when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
// any(HttpEntity.class), eq(Map.class)))
// .thenReturn(ResponseEntity.ok(null));

// Map<String, Object> result = service.getClassroom(school, "cls4");

// assertThat(result).isNull();
// }

// // -------------------------------------------------------------------------
// // Request construction — Bearer token is forwarded correctly
// // -------------------------------------------------------------------------

// @Test
// void getUser_setsAuthorizationHeaderWithToken() {
// when(tokenService.getAccessToken(school)).thenReturn("my-secret-token");
// when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
// any(HttpEntity.class), eq(Map.class)))
// .thenAnswer(invocation -> {
// HttpEntity<?> entity = invocation.getArgument(2);
// assertThat(entity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
// .isEqualTo("Bearer my-secret-token");
// return ResponseEntity.ok(Map.of());
// });

// service.getUser(school, "s5", "student");

// verify(tokenService).getAccessToken(school);
// }
// }