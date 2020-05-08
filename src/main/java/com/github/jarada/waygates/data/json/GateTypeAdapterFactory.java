package com.github.jarada.waygates.data.json;

import com.github.jarada.waygates.data.Gate;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GateTypeAdapterFactory extends CustomizedTypeAdapterFactory<Gate> {

    public GateTypeAdapterFactory() {
        super(Gate.class);
    }

    @Override
    protected void beforeWrite(Gate source, JsonElement toSerialize) {
        JsonArray coords = toSerialize.getAsJsonObject().get("coords").getAsJsonArray();
        for (JsonElement element : coords) {
            JsonObject coord = (JsonObject)element;
            coord.remove("worldName");
        }
        toSerialize.getAsJsonObject().get("start").getAsJsonObject().remove("worldName");
    }

    @Override
    protected void afterRead(JsonElement deserialized) {
        JsonObject exit = deserialized.getAsJsonObject().get("exit").getAsJsonObject();
        JsonArray coords = deserialized.getAsJsonObject().get("coords").getAsJsonArray();
        for (JsonElement element : coords) {
            JsonObject coord = (JsonObject)element;
            coord.add("worldName", exit.get("worldName"));
        }
        deserialized.getAsJsonObject().get("start").getAsJsonObject().add("worldName", exit.get("worldName"));
    }
}
