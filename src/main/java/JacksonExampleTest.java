import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Complete;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.RouteAdapter;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonProperty;

import javax.swing.text.Segment;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    Writer fileWriter;
    String fileLocation = "Database";
    private static int uuid = 0;

    public JacksonExampleTest() throws IOException {

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
    private CompletionStage<Done> saveOrder(final String order, String uuid) throws IOException{
        Path path = Paths.get(fileLocation + "/" + uuid);
        Files.write(path, order.getBytes());
        return CompletableFuture.completedFuture(Done.getInstance());
    }

    private RouteAdapter getResult(Path path){
        RouteAdapter complete = null;
        try {
            complete = complete(StatusCodes.ACCEPTED, "" + Files.readAllLines(path));
        } catch (IOException e) {
            complete = complete(StatusCodes.BAD_REQUEST, "There is no file at this location");
        }
        return complete;
    }



    private Route createRoute() throws IOException{
        return concat(
                get(() ->
                        pathPrefix("Akka", () ->
                                path(Segment -> {
                                    Path path = Paths.get("Database/" + Segment);
                                    return getResult(path);
                                }))),
                post(() ->
                        pathPrefix("Akka" , () ->
                        path(Segment ->
                                entity(Jackson.unmarshaller(String.class), order -> {
                                    CompletionStage<Done> futureSaved = null;
                                    try {
                                        futureSaved = saveOrder(order, Segment);
                                    }catch(IOException e){

                                    }
                                    return onSuccess(futureSaved, done ->
                                            complete("order created")
                                    );
                                }
                        )
                )
                        )
            ));
        }
}
