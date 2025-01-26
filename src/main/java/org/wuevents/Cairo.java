package org.wuevents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.logging.Logger;

class Cairo {

    static String venue_name    = "Cairo";
    static String venue_url     = "https://cairo.wue.de";
    static String venue_address = "Fred-Joseph-Platz 3, 97082 Würzburg";

    Logger logger = Logger.getLogger("org.wuevents");
    Connection connection;
    ObjectMapper objectMapper;

    public Cairo() {
        this.connection = Jsoup.connect("https://cairo.wue.de/programm");
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Extract, transform, load (ETL)
     */
    void fetch() throws IOException, InterruptedException {

        var input  = connection.get();
        var output = objectMapper.createObjectNode();

        output.put("created", Instant.now().toString());

        var venue = objectMapper.createObjectNode();
        venue.put("name", venue_name);
        venue.put("url", venue_url);
        venue.put("address", venue_address);
        output.put("venue", venue);

        var events = objectMapper.createArrayNode();
        var items = input.select("h2.event-name > a");
        Consumer<Element> consumer = element -> events.add(event((element)));
        items.forEach(consumer);

        output.put("events", events);
        objectMapper.writeValue(System.out, output);

        logger.info("Cairo...................................................................[DONE]");
    }

    JsonNode event(Element link) {
        var event = objectMapper.createObjectNode();
        try {
            var document = connection.newRequest(venue_url + link.attr("href")).get();
            var title = document.title();
            var url   = venue_url + link.attr("href");
            var date  = document.select(".event-date > .data-date").text();
            var begin = document.select(".event-start > .data-start").text();
            var doors = document.select(".event-entrance > .data-entrance").text();
            var description = document.select(".event-info-right").remove().text();

            event.put("title", title);
            event.put("url", url);
            event.put("description", description);
            event.put("date", date);
            event.put("doors", doors);
            event.put("begin", begin);
            return event;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
