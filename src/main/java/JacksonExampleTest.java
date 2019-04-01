import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

// TODO: This command should work : curl -H "Content-Type:text/xml" -X POST -d "this is raw data" http://localhost:8080/create-order
//       but this error is thrown: The request's Content-Type is not supported. Expected: application/json
//       Apparently importing this: FailFastCirceSupport._ , solves it

import static akka.http.javadsl.server.PathMatchers.longSegment;

public class JacksonExampleTest extends AllDirectives {

    Map<Integer, String> postedFiles = new HashMap<>();
    BufferedWriter fileWriter;

    public JacksonExampleTest() throws IOException{
        fileWriter = new BufferedWriter(new FileWriter("Database/File.txt"));
        fileWriter.write("wdewdwedewdewdedwedewdwd");
    }

    public static void main(String[] args) throws Exception {

        // boot up server using the route as defined below
        ActorSystem system = ActorSystem.create("routes");

        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        //In order to access all directives we need an instance where the routes are define.
        JacksonExampleTest app = new JacksonExampleTest();

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost("localhost", 8080), materializer);

        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read(); // let it run until user presses return

        binding
                .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
                .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }

    // (fake) async database query api
    private CompletionStage<Optional<String>> fetchItem(long itemId) {
        return CompletableFuture.completedFuture(Optional.of(postedFiles.get(itemId)));
    }

    // (fake) async database query api
    private CompletionStage<Done> saveOrder(final String order) {
        postedFiles.put(1, order);
        return CompletableFuture.completedFuture(Done.getInstance());
    }



    private Route createRoute() throws IOException{

        return concat (
                get(() ->
                        pathPrefix("item", () ->
                                path(longSegment(), (Long id) -> {
                                    final CompletionStage<Optional<String>> futureMaybeItem = fetchItem(id);
                                    return onSuccess(futureMaybeItem, maybeItem ->
                                            maybeItem.map(item -> completeOK(item, Jackson.marshaller()))
                                                    .orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found"))
                                    );
                                }))),
                post(() ->
                        path("create-order", () ->
                                entity(Jackson.unmarshaller(String.class), order -> {
                                    try{
                                        fileWriter.write("IN POST PATH");
                                    }catch(IOException e){
                                        complete(e.getMessage());

                                    }CompletionStage<Done> futureSaved = saveOrder(order);
                                    postedFiles.put(1, order);
                                    return complete("order created");
                                })))
        );
    }
/*
    private static class Item {

        final String name;
        final long id;

        @JsonCreator
        Item(@JsonProperty("name") String name,
             @JsonProperty("id") long id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public long getId() {
            return id;
        }
    }

    private static class Order {

        final List<Item> items;

        @JsonCreator
        Order(@JsonProperty("items") List<Item> items) {
            this.items = items;
        }

        public List<Item> getItems() {
            return items;
        }
    }
    */
}