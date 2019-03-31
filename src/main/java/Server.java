//import akka.NotUsed;
//import akka.actor.ActorSystem;
//import akka.http.javadsl.ConnectHttp;
//import akka.http.javadsl.Http;
//import akka.http.javadsl.ServerBinding;
//import akka.http.javadsl.model.ContentTypes;
//import akka.http.javadsl.model.HttpEntities;
//import akka.http.javadsl.model.HttpRequest;
//import akka.http.javadsl.model.HttpResponse;
//import akka.http.javadsl.server.AllDirectives;
//import akka.http.javadsl.server.Route;
//import akka.stream.ActorMaterializer;
//import akka.stream.javadsl.Flow;
//import static akka.http.javadsl.unmarshalling.StringUnmarshallers.INTEGER;
//
//
////TODO Need to add post
////     Need to add the ability store documents
////     Add jwtToken authentication
//
//import java.util.concurrent.CompletionStage;
//
//public class Server extends AllDirectives {
//
//    public static void main(String[] args) throws Exception {
//        // boot up server using the route as defined below
//        ActorSystem system = ActorSystem.create("routes");
//
//        final Http http = Http.get(system);
//        final ActorMaterializer materializer = ActorMaterializer.create(system);
//
//        //In order to access all directives we need an instance where the routes are define.
//        Server app = new Server();
//
//        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);
//        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
//                ConnectHttp.toHost("localhost", 8080), materializer);
//
//        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
//        System.in.read(); // let it run until user presses return
//
//        binding
//                .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
//                .thenAccept(unbound -> system.terminate()); // and shutdown when done
//    }
//
//
//    public Route createRoute() {
//        // This handler generates responses to `/hello?name=XXX` requests
//        Route helloRoute =
//                parameterOptional("name", optName -> {
//                    String name = optName.orElse("Mister X");
//                    return complete("Hello " + name + "!");
//                });
//
//        concat(
//            path("", () ->
//                    getFromResource("web/index.html")
//            ),
//            pathPrefix("pet", () ->
//                    path(""(
//                            // demonstrates different ways of handling requests:
//
//                            // 1. using a Function
//                            get(() -> complete("This is the pet ID")),
//
//                            // 2. using a method
//                            put(() ->
//                                    complete("Put complete"))
//                            ),
//                            // 2.1. using a method, and internally handling a Future value
//                            path("alternate", () ->
//                                    put(() -> complete("alternate")
//                                    )
//                            )
//                    ));
//    }
//}
