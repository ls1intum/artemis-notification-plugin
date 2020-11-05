package de.tum.in.www1.jenkins.notifications;

import com.google.gson.Gson;
import hudson.util.Secret;
import de.tum.in.www1.jenkins.notifications.model.TestResults;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.io.IOException;

public class HttpHelper {

    public static void postTestResults(TestResults results, String url, String secret) throws IOException, HttpException {
        final String body = new Gson().toJson(results);
        final HttpResponse response = Request.Post(url)
                .addHeader("Authorization", secret)
                .addHeader("Accept", ContentType.APPLICATION_JSON.getMimeType())
                .bodyString(body, ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new HttpException(String.format(Messages.SendTestResultsNotificationPostBuildTask_errors_postFailed(),
                    response.getStatusLine().getStatusCode(), IOUtils.toString(response.getEntity().getContent())));
        }
    }
}
