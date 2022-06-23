package hu.progmasters.conference.exceptionhandler;

public class EmailNotValidException extends RuntimeException{

    private String email;

    public EmailNotValidException(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public EmailNotValidException setEmail(String email) {
        this.email = email;
        return this;
    }
}
