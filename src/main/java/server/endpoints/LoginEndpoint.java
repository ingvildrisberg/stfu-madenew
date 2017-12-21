package server.endpoints;

import com.google.gson.Gson;
import server.controllers.TokenController;
import server.models.Student;
import server.models.Token;
import server.providers.StudentTable;
import server.resources.Log;
import server.utility.Authenticator;
import server.utility.Crypter;
import server.utility.CurrentStudentContext;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;


@Path("/login")
public class LoginEndpoint {

    private StudentTable studentTable = new StudentTable();
    private TokenController tokenController = new TokenController();
    private Gson gson = new Gson();
    private Crypter crypter = new Crypter();

    /**
     *
     * @param token
     * @param jsonLogin
     * @return Responses
     * @throws Exception
     */
    @POST
    public Response login(@HeaderParam("Authorization") String token, String jsonLogin) throws Exception {

        jsonLogin = new Gson().fromJson(jsonLogin, String.class);
        jsonLogin = crypter.decrypt(jsonLogin);

        CurrentStudentContext student = tokenController.getStudentFromTokens(token);
        Student currentStudent = student.getCurrentStudent();
        if (currentStudent != null) {

            Log.writeLog(getClass().getName(), this, "Already logged in", 2);
            return Response
                    .status(400)
                    .type("plain/text")
                    .entity("You are already logged in, no need to login again.")
                    .build();
        } else {
            Student foundStudent;
            Student needAuthStudent = gson.fromJson(jsonLogin, Student.class);
            try {
                foundStudent = studentTable.getStudentByEmail(needAuthStudent.getEmail());

            } catch (Exception notFound) {
                Log.writeLog(getClass().getName(), this, "Email not found/not existing", 2);

                return Response
                        .status(401)
                        .type("plain/text")
                        .entity("Email does not exist")
                        .build();
            }

            String doHash = Authenticator.hashWithSalt(needAuthStudent.getPassword(), (needAuthStudent.getEmail() + foundStudent.getCreatedTime()));

            if (doHash.equals(foundStudent.getPassword())) {
                //sets the token for the student
                String newToken = tokenController.setToken(foundStudent);
                Token theNewToken = new Token();
                theNewToken.setToken(newToken);
                foundStudent.setToken(theNewToken);


                String tokenJson = gson.toJson(newToken);

                Log.writeLog(getClass().getName(), this, "Logged in", 0);
                return Response
                        .status(200)
                        .type("application/json")
                        .entity(Crypter.encrypt(tokenJson))
                        .build();
            } else {
                Log.writeLog(getClass().getName(), this, "Password incorect", 2);
                return Response
                        .status(403)
                        .type("plain/text")
                        .entity("Password not correct")
                        .build();
            }
        }
    }
}
