package serverSide.jsonConverters;

import serverSide.message.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class JsonConverter {

    private static RuntimeTypeAdapterFactory<Message> messageAdapterFactory = RuntimeTypeAdapterFactory
            .of(Message.class, "type")
            .registerSubtype(LoginMessage.class, "Login")
            .registerSubtype(ResultMessage.class, "Result")
            .registerSubtype(GridStatusMessage.class, "GridStatus")
            .registerSubtype(GameActionMessage.class, "GameAction")
            .registerSubtype(ChatMessage.class, "Chat")
            .registerSubtype(SystemMessage.class, "System");

    private static Gson gson = new GsonBuilder().registerTypeAdapterFactory(messageAdapterFactory).create();

    public static String writeJson(Message message) {
        MessageWrapper mw = new MessageWrapper(message);

        return gson.toJson(mw);
    }

    public static Message readJson(String jsonFile) {
        MessageWrapper mw;

        try {
            mw = gson.fromJson(jsonFile, MessageWrapper.class);
        } catch (JsonSyntaxException e) {
            throw new JsonSyntaxException("Json data corrupted.");
        }

        if (mw != null) {
            return mw.getMessage();
        } else {
            return null;
        }
    }

    public static void main(String[] args) {

   }
}
