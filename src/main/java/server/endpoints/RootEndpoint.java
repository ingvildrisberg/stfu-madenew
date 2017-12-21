package server.endpoints;

import com.google.gson.Gson;
import server.resources.Log;
import server.utility.Crypter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

// denne metode er blevet brugt til test af hashing.
@Path("/")
public class RootEndpoint {

    /**
     *
     * @return Response
     */
    @GET
    public Response defaultGetMethod(){

        String json = new Gson().toJson("Foo - test");

        Log.writeLog(getClass().getName(), this, "TEST", 0);
        return Response
                .status(200)
                .type("text/plain")
                .entity(Crypter.encrypt(json))
                .build();
    }
}
