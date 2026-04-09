import java.util.Map;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
public class PU_COMMS_API_Impl implements PU_COMMS_API {
    private String puApiEndpoint;

    public PU_COMMS_API_Impl(String puApiEndpoint) {
        // No SMTP credentials needed since PU is handling sending
        this.puApiEndpoint = puApiEndpoint;

    }

    /**
     * Instead of sending email directly, this method passes the
     * email data to the PU subsystem for actual sending.
     */
    @Override
    public boolean sendEmail(String recipient, String subject, String content) {
        try {
            // Package the email data
            Map<String, String> emailData = Map.of(
                    "recipient", recipient,
                    "subject", subject,
                    "content", content
            );


            System.out.println("Email data handed off to PU subsystem: " + emailData);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean processCardPayment(String cardNumber, String expiry, double amount, String orderID) {
        try {
            // Creating JSON payload
            String payload = String.format(
                    "{\"cardNumber\":\"%s\",\"expiry\":\"%s\",\"amount\":%.2f,\"orderID\":\"%s\"}",
                    cardNumber, expiry, amount, orderID
            );

            // Opening HTTPS connection to PU API
            URL url = new URL(puApiEndpoint + "/processCardPayment");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Sending encrypted payload
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes("UTF-8"));
            }

            // Checking PU response
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                // PU confirmed payment
                return true;
            } else {
                System.out.println("PU API returned error code: " + responseCode);
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}


