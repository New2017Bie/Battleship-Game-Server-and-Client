package serverSide.message;

public class ChatMessage extends Message {

    private String text;

    ChatMessage(String username, String text) {
        super(username);
        setText(text);
    }

    ChatMessage(String text) {
        super(null);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    private void setText(String text) {
        this.text = text;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CHAT;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "text='" + text + '\'' +
                '}';
    }
}
