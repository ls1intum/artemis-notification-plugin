package de.tum.in.ase.notification;

import com.google.gson.Gson;
import de.tum.in.ase.notification.model.TestResults;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;

public class HttpHelper {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void postTestResults(TestResults results, String url, String secret) throws IOException, HttpException {
        final String body = new Gson().toJson(results);

        LOGGER.debug("Posting test results to {}: {}", url, body);

        final HttpResponse response = Request.Post(url)
                .addHeader("Authorization", secret)
                .addHeader("Accept", ContentType.APPLICATION_JSON.getMimeType())
                .bodyString(body, ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new HttpException(String.format("Sending test results failed (%d) with response: %s",
                    response.getStatusLine().getStatusCode(), IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset())));
        }
    }

}
