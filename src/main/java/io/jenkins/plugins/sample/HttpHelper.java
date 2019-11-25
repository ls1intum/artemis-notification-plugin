package io.jenkins.plugins.sample;

import com.google.gson.Gson;
import hudson.util.Secret;
import io.jenkins.plugins.sample.model.TestResults;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.io.IOException;

public class HttpHelper {

    public static void postTestResults(TestResults results, String url, Secret token) throws IOException, HttpException {
        final String body = new Gson().toJson(results);
        final HttpResponse response = Request.Post(url)
                .addHeader("Authorization", token.getPlainText())
                .addHeader("Accept", ContentType.APPLICATION_JSON.getMimeType())
                .bodyString(body, ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new HttpException("Sending test results failed with response " + response.getStatusLine().getStatusCode());
        }
    }
}
