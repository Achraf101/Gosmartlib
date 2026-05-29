package be.ap.backend.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

// import java.net.URI;
// import java.net.http.HttpClient;
import java.net.http.HttpRequest;
// import java.net.http.HttpResponse;
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

import be.ap.backend.entity.Book;
import be.ap.backend.entity.Loan;
import be.ap.backend.repository.LoanBookRepository;
import be.ap.backend.repository.LoanRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final LoanBookRepository loanBookRepository;
    private final LoanRepository loanRepository;
    private final EmailTemplateService emailTemplateService;

    private final HttpClient client = HttpClient.newHttpClient();

    @Value("${app.smartschool.client-id}")
    private String clientId; // needed for images in mail

    @Value("${app.smartschool.client-secret}")
    private String clientSecret; // needed for images in mail

    private final String notificationTitle = "Overzicht van ontleende boeken";
    private final String reminderTitle = "Vergeet je boeken niet binnen te brengen!";

    public Boolean sendReminderNotification(Long loanId) {
        // get user tokens with refresh token (also subdomain)

        Optional<Loan> loan = loanRepository.findById(loanId);
        if(loan.isEmpty()) return false;

        Loan l = loan.get();

        String subdomain = l.getUser().getSchool().getSsSubdomain();

        Optional<String> accessToken = getAccessToken(l.getUser().getSsRefresh(), subdomain);

        if(accessToken.isEmpty()) return false;

        // fill in template with all books from loan
        List<Book> books = loanBookRepository.getBooksByLoanId(loanId);
        
        // call emailtemplateservice
        System.out.println("######################");
        System.out.println(l.getEnd());
        System.out.println(l.getUser().getSsRefresh());
        String populatedTemplate = emailTemplateService.buildReminderEmail(books, l.getEnd());
        System.out.println(populatedTemplate);
        System.out.println("######################");

        // send mail to smartschool async
        // (https://{subdomain}.smartschool.be/Api/V1/sendmsg)
        String url = "https://" + subdomain + ".smartschool.be/Api/V1/sendmsg?access_token=" + accessToken.get() + "&messageTitle="
                + reminderTitle + "&messageBody=" + populatedTemplate;

        Boolean state = sendMail(url);

        return state;

    }

    public Boolean sendNotification(Long userId, Long loanId) {
        return true;
    }

    // get access token from smartschool
    private Optional<String> getAccessToken(String refreshToken, String subdomain) {

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
                return Optional.empty();
            }

            String accessToken = response.toSuccessResponse()
                    .getTokens()
                    .getAccessToken()
                    .getValue();

            return Optional.of(accessToken);

        } catch (URISyntaxException e) {
            return Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    private Boolean sendMail(String url) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        CompletableFuture<HttpResponse<String>> responseFuture = client.sendAsync(request,
                HttpResponse.BodyHandlers.ofString());


        responseFuture.thenAccept(response -> {
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());
        }).exceptionally(ex -> {
            System.err.println("Error occurred: " + ex.getMessage());
            return null;
        });

        // The program continues here immediately while the request is in flight
        System.out.println("Request sent! Doing other things...");

        // Optional: Block if you need to wait for the result before the program exits
        System.out.println(responseFuture.join());

        return true;
    }

}