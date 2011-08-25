package com.cloud.ovm.object;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cloud.ovm.object.OvmHost.Details;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public class Coder {
	private static Gson _gson;
	private static Gson _mapGson;
	
	public static Object[] EMPTY_PARAMS = new Object[0];
	
	private static class MapDecoder implements JsonDeserializer<Map<String, String>> {
		@Override
		public Map<String, String> deserialize(JsonElement arg0, Type arg1,
				JsonDeserializationContext arg2) throws JsonParseException {
			if (!arg0.isJsonObject()) {
				throw new JsonParseException(arg0.toString() + " is not Json Object, cannot convert to map");
			}
			JsonObject objs = arg0.getAsJsonObject();
			Map<String, String> map = new HashMap<String ,String>();
			for (Entry<String, JsonElement> e : objs.entrySet()) {
				if (!e.getValue().isJsonPrimitive()) {
					throw new JsonParseException(e.getValue().toString() + " is not a Json primitive," + arg0 + " can not convert to map");
				}
				map.put(e.getKey(), e.getValue().getAsString());
			}
			return map;
		}
		
	}
	
	static {
		GsonBuilder _builder = new GsonBuilder();
		_builder.registerTypeAdapter(Map.class, new MapDecoder());
		_mapGson = _builder.create();
		_gson = new Gson();
	}
	
	public static String toJson(Object obj) {
		return _gson.toJson(obj);
	}
	
	public static <T> T fromJson(String str, Class<T> clz) {
		return (T)_gson.fromJson(str, clz);
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, String> mapFromJson(String str) {
		return _mapGson.fromJson(str, Map.class);
	}
	
	public static List<String> listFromJson(String str) {
		Type listType = new TypeToken<List<String>>() {}.getType();
		return _gson.fromJson(str, listType);
	}
}
