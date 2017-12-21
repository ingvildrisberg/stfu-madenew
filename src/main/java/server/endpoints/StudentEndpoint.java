package server.endpoints;


import com.google.gson.Gson;
import server.controllers.StudentController;
import server.controllers.TokenController;
import server.models.Event;
import server.models.Student;
import server.resources.Log;
import server.utility.CurrentStudentContext;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.ArrayList;

import server.utility.Crypter;

@Path("/students")
public class StudentEndpoint {

    private StudentController studentController = new StudentController();
    private TokenController tokenController = new TokenController();
    private Gson gson = new Gson();

    /**
     *
     * @param token
     * @param idStudent
     * @return Responses
     * @throws SQLException
     * @throws IllegalAccessException
     */
    @GET
    @Path("{idStudent}/events")
    public Response getAttendingEvents(@HeaderParam("Authorization") String token, @PathParam("idStudent") int idStudent) throws SQLException, IllegalAccessException {
        CurrentStudentContext student = tokenController.getStudentFromTokens(token);
        Student currentStudent = student.getCurrentStudent();

        if (currentStudent != null) {
            if (currentStudent.getIdStudent() != idStudent) {
                return Response
                        .status(403)
                        .type("plain/text")
                        .entity("You're not allowed to view this persons events. You can only view your own events.")
                        .build();
            } else {
                ArrayList<Event> foundAttendingEvents;
                foundAttendingEvents = studentController.getAttendingEvents(idStudent);
                // if event not found
                if (foundAttendingEvents.isEmpty()) {
                    Log.writeLog(getClass().getName(), this, "Student has no attending events", 2);
                    return Response
                            .status(200)
                            .type("plain/text")
                            .entity("You are not attending any events")
                            .build();
                } else {
                    String json = new Gson().toJson(foundAttendingEvents);
                    Log.writeLog(getClass().getName(), this, "Attending events fetched", 0);
                    return Response
                            .status(200)
                            .type("application/json")
                            .entity(Crypter.encrypt(json))
                            .build();
                }
            }
        } else {
            return Response
                    .status(403)
                    .type("plain/text")
                    .entity("You are not logged in - please log in before attempting to view events you are attending")
                    .build();
        }
    }

    /**
     *
     * @param token
     * @return Responses
     * @throws SQLException
     */
    @POST
    @Path("/logout")
    public Response logout(@HeaderParam("Authorization") String token) throws SQLException {
        CurrentStudentContext student = tokenController.getStudentFromTokens(token);
        Student currentStudent = student.getCurrentStudent();
        if (currentStudent != null) {
            if (tokenController.deleteToken(token)) {
                Log.writeLog(getClass().getName(), this, "User logged out", 0);
                return Response
                        .status(200)
                        .type("plain/text")
                        .entity("You are now logged out")
                        .build();
            } else {
                return Response
                        .status(404)
                        .type("plain/text")
                        .entity("There was an error, we couldn't log you out.")
                        .build();
            }
        } else {
            return Response
                    .status(403)
                    .type("plain/text")
                    .entity("{You are not logged in - please log in before attempting to log out}")
                    .build();
        }
    }

    /**
     *
     * @param token
     * @return Responses
     * @throws SQLException
     */
    @GET
    @Path("/profile")
    public Response get(@HeaderParam("Authorization") String token) throws SQLException {

        CurrentStudentContext student = tokenController.getStudentFromTokens(token);
        Student currentStudent = student.getCurrentStudent();
        if (currentStudent != null) {
            String json = new Gson().toJson(currentStudent);
            //String crypted = Crypter.encryptDecrypt(json);
            Log.writeLog(getClass().getName(), this, "Current student found: " + currentStudent, 0);
            return Response
                    .status(200)
                    .type("application/json")
                    .entity(Crypter.encrypt(json))
                    .build();
        } else {
            Log.writeLog(getClass().getName(), this, "Current student not found - 403", 2);
            return Response
                    .status(403)
                    .type("plain/text")
                    .entity("You aren't logged in, please log in before trying to view your profile")
                    .build();
        }
    }

}
