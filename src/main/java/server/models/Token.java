package server.models;

import server.utility.Authenticator;

public class Token {

    private String token;
    private Student student;

    /**
     *
     * @param student
     */
    public Token(Student student){
        setToken(Authenticator.hashWithSalt(student.getEmail(), student.getSalt()));

    }

    public Token(){}

    public void setStudent (Student student) {
        this.student = student;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken(){
        return token;
    }

}
