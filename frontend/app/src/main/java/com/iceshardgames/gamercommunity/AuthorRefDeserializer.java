package com.iceshardgames.gamercommunity;


import com.google.gson.*;
import java.lang.reflect.Type;

public class AuthorRefDeserializer implements JsonDeserializer<AuthorRef> {
    @Override
    public AuthorRef deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            if (json == null || json.isJsonNull()) return null;
            AuthorRef ar = new AuthorRef();

            if (json.isJsonPrimitive()) {
                // shape: "68c92bcc..."
                ar.id = json.getAsString();
                return ar;
            }

            if (json.isJsonObject()) {
                JsonObject o = json.getAsJsonObject();
                if (o.has("id") && !o.get("id").isJsonNull()) ar.id = o.get("id").getAsString();
                else if (o.has("_id") && !o.get("_id").isJsonNull()) ar.id = o.get("_id").getAsString();

                if (o.has("username") && !o.get("username").isJsonNull()) ar.username = o.get("username").getAsString();
                if (o.has("displayName") && !o.get("displayName").isJsonNull()) ar.displayName = o.get("displayName").getAsString();
                return ar;
            }

            return null;
        } catch (Exception e) {
            // defensive: don't break deserialization of other fields
            return null;
        }
    }
}
