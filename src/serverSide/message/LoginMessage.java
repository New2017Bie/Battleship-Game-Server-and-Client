package serverSide.message;

public class LoginMessage extends Message {
    @Override
    public MessageType getMessageType() {
        return MessageType.LOGIN;
    }

    LoginMessage(String username) {
        super(username);
    }

    @Override
    public String toString() {
        return "LoginMessage{" +
                "username='" + username + '\'' +
                ", module='" + module + '\'' +
                ", messageType=" + getMessageType() +
                '}';
    }
}
