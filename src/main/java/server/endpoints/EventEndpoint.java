package server.endpoints;

import javax.ws.rs.Path;

import com.google.gson.Gson;
import server.controllers.EventController;
import server.controllers.TokenController;
import server.exceptions.ErrorMessage;
import server.exceptions.ResponseException;
import server.models.Event;
import server.models.Student;
import server.resources.Log;
import server.utility.Crypter;
import server.utility.CurrentStudentContext;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.ArrayList;

@Path("/events")
public class EventEndpoint {

    private EventController eventController = new EventController();
    private TokenController tokenController = new TokenController();
    private Gson gson = new Gson();
    private Crypter crypter = new Crypter();

    /**
     *
     * @param token
     * @param eventId
     * @param data
     * @return Responses
     * @throws Exception
     */
    @PUT
    @Path("{idEvent}/update-event")
    public Response updateEvent(@HeaderParam("Authorization") String token, @PathParam("idEvent") int eventId, String data) throws Exception {

        data = gson.fromJson(data, String.class);
        data = crypter.decrypt(data);

        CurrentStudentContext student = tokenController.getStudentFromTokens(token);
        Student currentStudent = student.getCurrentStudent();
        if (currentStudent != null) {


            Event event = gson.fromJson(data, Event.class);
            event.setIdEvent(eventId);

            if (eventController.updateEvent(event, currentStudent)) {
                String json = gson.toJson(event) ;

                Log.writeLog(getClass().getName(), this, "Event was updated", 0);

                return Response
                        .status(200)
                        .type("application/json")
                        .entity(Crypter.encrypt(json))
                        .build();

            } else {
                Log.writeLog(getClass().getName(), this, "Event not found or not owner of the event", 2);
                return Response
                        .status(404)
                        .type("plain/text")
                        .entity("Either the event wasn't found, or you aren't the owner of the event and therefore you cannot update it")
                        .build();
            }
        } else {
            return Response
                    .status(403)
                    .type("plain/text")
                    .entity("You are not logged in - please log in before attempting to update an event")
                    .build();
        }
    }

    /**
     *
     * @param token
     * @param eventData
     * @return Responses
     * @throws SQLException
     */
    @POST
    public Response createEvent(@HeaderParam("Authorization") String token, String eventData) throws SQLException {

        eventData = gson.fromJson(eventData, String.class);
        eventData = crypter.decrypt(eventData);

        CurrentStudentContext student = tokenController.getStudentFromTokens(token);
        Student currentStudent = student.getCurrentStudent();

        if (currentStudent != null) {
            Event event = new Gson().fromJson(eventData, Event.class);
            if (eventController.createEvent(event, currentStudent)) {


                Log.writeLog(getClass().getName(), this, "Event created", 0);

                String json = gson.toJson(event);

                return Response
                        .status(200)
                        .type("application/json")
                        .entity(Crypter.encrypt(json))
                        .build();
            } else {
                Log.writeLog(getClass().getName(), this, "Not able to create event", 2);
                return Response
                        .status(403)
                        .type("plain/text")
                        .entity("Failed! Event couldn't be created")
                        .build();
            }
        } else {
            return Response
                    .status(403)
                    .type("plain/text")
                    .entity("You are not logged in - please log in before attempting to create an event")
                    .build();
        }
    }

    /**
     *
     * @param token
     * @param eventId
     * @param data
     * @return Responses
     * @throws Exception
     */
    @PUT
    @Path("{idEvent}/delete-event")
    public Response deleteEvent(@HeaderParam("Authorization") String token, @PathParam("idEvent") int eventId, String data) throws Exception {

        data = gson.fromJson(data, String.class);
        data = crypter.decrypt(data);

        CurrentStudentContext student = tokenController.getStudentFromTokens(token);
        Student currentStudent = student.getCurrentStudent();
        if (currentStudent != null) {
            Event event = gson.fromJson(data, Event.class);
            event.setIdEvent(eventId);
            if (eventController.deleteEvent(event, currentStudent)) {

                String json = gson.toJson(event);


                Log.writeLog(getClass().getName(), this, "Event deleted", 0);
                return Response
                        .status(200)
                        .type("application/json")
                        .entity(Crypter.encrypt(json))
                        .build();
            } else {
                Log.writeLog(getClass().getName(), this, "Event not deleted", 2);
                return Response
                        .status(400)
                        .type("plain/text")
                        .entity("Either the event wasn't found or you aren't the owner of the event you are trying to delete")
                        .build();
            }
        } else {
            return Response
                    .status(403)
                    .type("plain/text")
                    .entity("You are not logged in - please log in before attempting to delete an event")
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
    public Response getEvents(@HeaderParam("Authorization") String token) throws SQLException {
        CurrentStudentContext student = tokenController.getStudentFromTokens(token);
        Student currentStudent = student.getCurrentStudent();

        if (currentStudent != null) {


                String json = gson.toJson(eventController.getAllEvents());
                Log.writeLog(getClass().getName(), this, "All events fetched", 0);
                return Response
                        .status(200)
                        .type("application/json")
                        .entity(Crypter.encrypt(json))
                        .build();
        } else {
            return Response
                    .status(403)
                    .type("plain/text")
                    .entity("You are not logged in - please log in before attempting to get a list of all events")
                    .build();
        }
    }

    /**
     *
     * @param token
     * @param idEvent
     * @return Responses
     * @throws SQLException
     * @throws IllegalAccessException
     */
    @GET
    @Path("{idEvent}/students")
    public Response getAttendingStudents(@HeaderParam("Authorization") String token, @PathParam("idEvent") String idEvent) throws SQLException, IllegalAccessException {

        CurrentStudentContext student = tokenController.getStudentFromTokens(token);
        Student currentStudent = student.getCurrentStudent();
        if (currentStudent != null) {
            ArrayList<Student> foundAttendingStudents;

            if (idEvent.isEmpty()) {
                Log.writeLog(getClass().getName(), this, "Event not found", 2);
                return Response
                        .status(400)
                        .type("plain/text")
                        .entity("The event with this id could not be found")
                        .build();
            } else {
                foundAttendingStudents = eventController.getAttendingStudents(idEvent);
                // If student not found:
                if (foundAttendingStudents.isEmpty()) {
                    Log.writeLog(getClass().getName(), this, "No atten'ding students at event", 2);
                    return Response
                            .status(400)
                            .type("plain/text")
                            .entity("No attending students")
                            .build();
                } else {
                    String json = gson.toJson(foundAttendingStudents);

                    Log.writeLog(getClass().getName(), this, "Attending students fetched", 0);
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
                    .entity("You are not logged in - please log in before attempting to get a list of students attending this event")
                    .build();
        }

    }

    /**
     *
     * @param token
     * @param eventJson
     * @return Responses
     * @throws SQLException
     */
    @POST
    @Path("/join")
    public Response joinEvent(@HeaderParam("Authorization") String token, String eventJson) throws SQLException, ResponseException {

        eventJson = gson.fromJson(eventJson, String.class);
        eventJson = crypter.decrypt(eventJson);

        CurrentStudentContext student = tokenController.getStudentFromTokens(token);
        Student currentStudent = student.getCurrentStudent();
        if (currentStudent != null) {
            Event event = gson.fromJson(eventJson, Event.class);

                eventController.joinEvent(event.getIdEvent(), currentStudent.getIdStudent());

                String json = gson.toJson(event);

                Log.writeLog(getClass().getName(), this, "Event joined", 0);
                return Response
                        .status(200)
                        .type("application/json")
                        .entity(Crypter.encrypt(json))
                        .build();


        } else {
            return Response
                    .status(403)
                    .type("plain/text")
                    .entity("You are not logged in - please log in before attempting to get a list of students attending this event")
                    .build();
        }
    }

}