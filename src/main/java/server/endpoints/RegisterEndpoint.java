package server.endpoints;

import com.google.gson.Gson;
import server.controllers.StudentController;
import server.controllers.TokenController;
import server.models.Student;
import server.providers.StudentTable;
import server.resources.Log;
import server.utility.Crypter;
import server.utility.CurrentStudentContext;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.sql.SQLException;


@Path("/register")
public class RegisterEndpoint {

    private StudentController studentController = new StudentController();
    private StudentTable studentTable = new StudentTable();
    private TokenController tokenController = new TokenController();

    private Gson gson = new Gson();
    private Crypter crypter = new Crypter();

    /**
     *
     * @param token
     * @param jsonStudent
     * @return Responses
     * @throws Exception
     */
    @POST
    @Produces("Application/json")
    public Response register(@HeaderParam("Authorization") String token, String jsonStudent) throws Exception {

        jsonStudent = gson.fromJson(jsonStudent, String.class);
        jsonStudent = crypter.decrypt(jsonStudent);

        CurrentStudentContext student = tokenController.getStudentFromTokens(token);
        Student currentStudent = student.getCurrentStudent();
        if (currentStudent != null) {
            return Response
                    .status(400)
                    .type("plain/text")
                    .entity("You are already logged in, no need to register again. Please log out before trying to register a new account")
                    .build();
        } else {
            Student registerStudent;

            try {
                registerStudent = gson.fromJson(jsonStudent, Student.class);
            } catch (IllegalArgumentException e) {
                System.out.print(e.getMessage());
                Log.writeLog(getClass().getName(), this, "User couldn't be registered", 2);
                return Response
                        .status(400)
                        .type("plain/text")
                        .entity("The entered information isn't valid, please try again.")
                        .build();
            }
            try {
                studentController.verifyStudentCreation(registerStudent.getFirstName(), registerStudent.getLastName(), registerStudent.getPassword(), registerStudent.getEmail(), registerStudent.getVerifyPassword());
            } catch (IllegalArgumentException ee) {
                System.out.print(ee.getMessage());
                //Bør måske ændres til at user ikke kunne verifies pga forkert info om fornavn, efternavn, kodeord eller email
                Log.writeLog(getClass().getName(), this, "User couldn't be registered", 2);
                return Response
                        .status(400)
                        .type("text/plain")
                        .entity("Some of the information didn't fulfill our requirements, please try again.")
                        .build();
            }
            try {
                studentTable.addStudent(registerStudent);

                String registerJson = gson.toJson(registerStudent);

                Log.writeLog(getClass().getName(), this, registerStudent + " registered", 0);
                return Response
                        .status(200)
                        .type("application/json")
                        .entity(Crypter.encrypt(registerJson))
                        .build();

            } catch (SQLException e) {
                Log.writeLog(getClass().getName(), this, "User already exists", 2);
                return Response
                        .status(404)
                        .type("text/plain")
                        .entity("This user already exists, please log-in.")
                        .build();
            }
        }

    }
}
