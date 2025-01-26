package org.wuevents;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.logging.Logger;

class Kellerperle {

    static String venue_name    = "Kellerperle";
    static String venue_url     = "https://www.kellerperle.de";
    static String venue_address = "Am Studentenhaus 1, 97072 Würzburg";

    Logger logger = Logger.getLogger("org.wuevents");

    /**
     * Extract, transform, load (ETL)
     */
    void fetch() throws IOException, InterruptedException {
        var uri = URI.create("https://www.kellerperle.de/programm.json");
        var http = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(uri).build();
        var response = http.send(request, HttpResponse.BodyHandlers.ofInputStream());

        var responseInputStream = response.body();
        var parser = new ObjectMapper();
        var input = parser.readTree(responseInputStream);
        var output = parser.createObjectNode();

        output.put("created", Instant.now().toString());

        var venue = parser.createObjectNode();
        venue.put("name", venue_name);
        venue.put("url", venue_url);
        venue.put("address", venue_address);
        output.put("venue", venue);

        var events = parser.createArrayNode();
        for (var item : input) {
            var event = parser.createObjectNode();
            event.put("title", item.get("title"));
            event.put("url", "https://www.kellerperle.de/#/" + item.get("vaId").asText());
            event.put("description", item.get("text"));
            event.put("date", item.get("date"));
            event.put("doors", item.get("einlass"));
            event.put("begin", item.get("beginn"));
            event.put("price", item.get("ak"));
            events.add(event);
        }
        output.put("events", events);

        parser.writeValue(System.out, output);

        logger.info("Kellerperle.............................................................[DONE]");
    }

}
