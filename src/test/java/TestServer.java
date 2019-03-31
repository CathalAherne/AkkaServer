import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import org.junit.Test;

import java.util.concurrent.CompletionStage;

// TODO Need to check body of httpResponse and also check statusCode

public class TestServer {

    @Test
    public void testGet(){
        final ActorSystem system = ActorSystem.create();

        final CompletionStage<HttpResponse> responseFuture =
                Http.get(system)
                        .singleRequest(HttpRequest.create("http://localhost:8080/hello"));


    }

}

