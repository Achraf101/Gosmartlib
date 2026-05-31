package be.ap.backend.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.RefreshTokenGrant;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.RefreshToken;

import be.ap.backend.entity.Loan;
import be.ap.backend.entity.User;
import be.ap.backend.mapper.BookWithAmount;
import be.ap.backend.queue.NotificationTask;
import be.ap.backend.repository.LoanBookRepository;
import be.ap.backend.repository.LoanRepository;
import be.ap.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final LoanBookRepository loanBookRepository;
    private final LoanRepository loanRepository;
    private final EmailTemplateService emailTemplateService;
    private final UserRepository userRepository;

    private final HttpClient client = HttpClient.newHttpClient();

    @Value("${app.smartschool.client-id}")
    private String clientId; // needed for images in mail

    @Value("${app.smartschool.client-secret}")
    private String clientSecret; // needed for images in mail

    private static final String LOAN_TITLE = "Overzicht van je ontleende boeken";
    private static final String REMINDER_TITLE = "Vergeet je boeken niet binnen te brengen!";

    public Boolean sendNotification(Long loanId, NotificationTask.Type type) {
        // get user tokens with refresh token (also subdomain)

        Optional<Loan> loan = loanRepository.findById(loanId);
        if (loan.isEmpty())
            return false;

        Loan l = loan.get();
        User u = l.getUser();
        String subdomain = u.getSchool().getSsSubdomain();

        TokenRecord tokens = getAccessToken(l.getUser().getSsRefresh(), subdomain);

        if (tokens.accessToken() == null)
            return false;

        // save refresh reason: changed
        u.setSsRefresh(tokens.refreshToken());
        userRepository.save(u);

        List<BookWithAmount> books = loanBookRepository.getBooksByLoanId(loanId);

        String populatedTemplate = "";
        String title = "";
        if (type == NotificationTask.Type.REMINDER) {
            title = REMINDER_TITLE;
            populatedTemplate = emailTemplateService.buildReminderEmail(books, l.getEnd());
        } else { // loan (confirmation)
            title = LOAN_TITLE;
            populatedTemplate = emailTemplateService.buildLoanEmail(books, l.getEnd());
        }

        // send mail to smartschool async
        String url = "https://" + subdomain + ".smartschool.be/Api/V1/sendmsg";

        Boolean state = sendMail(url, tokens.accessToken(), title, populatedTemplate);

        if (type == NotificationTask.Type.REMINDER) {
            loanRepository.setNotifiedTrue(loanId);
        }
        return state;
    }

    // get access token from smartschool
    private TokenRecord getAccessToken(String refreshToken, String subdomain) {

        try {
            TokenRequest request = new TokenRequest(
                    new URI("https://" + subdomain + ".smartschool.be/OAuth/index/token"),
                    new ClientSecretBasic(
                            new ClientID(clientId),
                            new Secret(clientSecret)),
                    new RefreshTokenGrant(new RefreshToken(refreshToken)));

            HTTPResponse httpResponse = request.toHTTPRequest().send();
            TokenResponse response = TokenResponse.parse(httpResponse);

            if (!response.indicatesSuccess()) {
                return null;
            }

            String accessToken = response.toSuccessResponse()
                    .getTokens()
                    .getAccessToken()
                    .getValue();

            String newRefreshToken = response.toSuccessResponse()
                    .getTokens()
                    .getRefreshToken()
                    .getValue();

            return new TokenRecord(accessToken, newRefreshToken);

        } catch (URISyntaxException e) {
            log.error("Invalid token endpoint URI for subdomain '{}': {}", subdomain, e.getMessage());
            return null;
        } catch (IOException e) {
            log.error("I/O error during token request for subdomain '{}': {}", subdomain, e.getMessage());
            return null;
        } catch (ParseException e) {
            log.error("Failed to parse token response for subdomain '{}': {}", subdomain, e.getMessage());
            return null;
        }
    }

    private Boolean sendMail(String url, String accessToken, String title, String message) {

        URI uri = UriComponentsBuilder
                .fromHttpUrl(url)
                .queryParam("access_token", accessToken)
                .queryParam("messageTitle", title)
                .queryParam("messageBody", message)
                .encode()
                .build()
                .toUri();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        CompletableFuture<HttpResponse<String>> responseFuture = client.sendAsync(request,
                HttpResponse.BodyHandlers.ofString());

        responseFuture.thenAccept(response -> {
        }).exceptionally(ex -> {
            log.info("{message} Error occurred: " + ex.getMessage());
            return null;
        });

        return true;
    }

    private record TokenRecord(String accessToken, String refreshToken) {
    }

}