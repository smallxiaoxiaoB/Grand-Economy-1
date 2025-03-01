package the_fireplace.grandeconomy.earnings;

import com.google.common.collect.Maps;
import com.google.gson.*;
import net.minecraft.util.ResourceLocation;
import the_fireplace.grandeconomy.GrandEconomy;

import java.io.*;
import java.util.Collections;
import java.util.Map;

public final class ConversionItems {
    static ConversionItems instance = null;

    private static ConversionItems getInstance() {
        if(instance == null)
            load();
        return instance;
    }

    private Map<ResourceLocation, Map<Integer, Integer>> items = Maps.newHashMap();

    public static boolean hasValue(ResourceLocation itemResource, int meta) {
        return getInstance().items.containsKey(itemResource) && getInstance().items.get(itemResource).containsKey(meta) && getInstance().items.get(itemResource).get(meta) > 0;
    }

    public static int getValue(ResourceLocation itemResource, int meta) {
        return getInstance().items.containsKey(itemResource) && getInstance().items.get(itemResource).containsKey(meta) && getInstance().items.get(itemResource).get(meta) > 0 ? getInstance().items.get(itemResource).get(meta) : 0;
    }

    private static void load() {
        instance = new ConversionItems();
        JsonParser jsonParser = new JsonParser();
        try {
            Object obj = jsonParser.parse(new FileReader(new File(GrandEconomy.configDir, "conversion_items.json")));
            if(obj instanceof JsonObject) {
                JsonObject jsonObject = (JsonObject) obj;
                JsonArray itemMap = jsonObject.get("items").getAsJsonArray();
                for (int i = 0; i < itemMap.size(); i++) {
                    ResourceLocation res = new ResourceLocation(itemMap.get(i).getAsJsonObject().get("item").getAsString());
                    if(!instance.items.containsKey(res))
                        instance.items.put(res, Maps.newHashMap());
                    instance.items.get(res).put(itemMap.get(i).getAsJsonObject().get("meta").getAsInt(), itemMap.get(i).getAsJsonObject().get("value").getAsInt());
                }
            }
        } catch (FileNotFoundException e) {
            //Generic entries that won't be usable in survival, so users have a template of what the entries look like
            instance.items.put(new ResourceLocation("minecraft:bedrock"), Maps.asMap(Collections.singleton(0), x -> 30));
            instance.items.put(new ResourceLocation("minecraft:command_block"), Maps.asMap(Collections.singleton(0), x -> 50));
            save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void save() {
        JsonObject obj = new JsonObject();
        JsonArray itemMap = new JsonArray();
        for(Map.Entry<ResourceLocation, Map<Integer, Integer>> entry: instance.items.entrySet()) {
            JsonObject outputEntry = new JsonObject();
            for(Map.Entry<Integer, Integer> entry1 : entry.getValue().entrySet()) {
                outputEntry.add("item", new JsonPrimitive(entry.getKey().toString()));
                outputEntry.add("meta", new JsonPrimitive(entry1.getKey()));
                outputEntry.add("value", new JsonPrimitive(entry1.getValue()));
            }
            itemMap.add(outputEntry);
        }
        obj.add("items", itemMap);
        try {
            FileWriter file = new FileWriter(new File(GrandEconomy.configDir, "conversion_items.json"));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(obj);
            file.write(json);
            file.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
