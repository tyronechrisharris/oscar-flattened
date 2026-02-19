package org.sensorhub.impl.service.sos;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;


public class TestConcurrentGetCaps
{

    public static void main(String[] args) throws Exception
    {
        HttpClient client = HttpClient.newHttpClient();
        URI getCapsUrl = new URI("http://localhost:8181/sensorhub/sos?service=SOS&version=2.0&request=GetCapabilities");
        for (int i=0; i<100; i++)
        {
            client.sendAsync(
                HttpRequest.newBuilder().uri(getCapsUrl).GET().build(),
                BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenAccept(System.out::println);
        }
        
        Thread.sleep(10000);
    }

}
