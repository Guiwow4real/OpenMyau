package myau.config;

import com.google.gson.*;
import myau.Myau;
import myau.mixin.IAccessorMinecraft;
import myau.module.Module;
import myau.util.ChatUtil;
import myau.property.Property;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.ArrayList;

public class Config {
    public static Minecraft mc = Minecraft.getMinecraft();
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public String name;
    public File file;

    public Config(String name, boolean newConfig) {
        this.name = name;
        this.file = new File("./config/Myau/", String.format("%s.json", this.name));
        try {
            file.getParentFile().mkdirs();
            if (newConfig) {
                ((IAccessorMinecraft) mc).getLogger().info(String.format("Created: %s", this.file.getName()));
            }
        } catch (Exception e) {
            ((IAccessorMinecraft) mc).getLogger().error(e.getMessage());
        }
    }

    public void load() {
        try {
            JsonElement parsed = new JsonParser().parse(new BufferedReader(new FileReader(file)));
            JsonObject jsonObject = parsed.getAsJsonObject();
            // Load GUI colors
            // GuiConfig.load(jsonObject); // <--- REMOVED THIS LINE
            // Note: GuiConfig was removed, using GuiModule instead
            
            // Load Frame positions if saveGuiState is enabled
            myau.module.modules.GuiModule guiModule = (myau.module.modules.GuiModule) myau.Myau.moduleManager.modules.get(myau.module.modules.GuiModule.class);
            if (guiModule != null && guiModule.saveGuiState.getValue()) {
                // Load frame positions
                if (jsonObject.has("framePositions")) {
                    JsonObject framePositions = jsonObject.getAsJsonObject("framePositions");
                    for (java.util.Map.Entry<String, JsonElement> entry : framePositions.entrySet()) {
                        String categoryName = entry.getKey();
                        JsonObject positionObj = entry.getValue().getAsJsonObject();
                        int x = positionObj.get("x").getAsInt();
                        int y = positionObj.get("y").getAsInt();
                        
                        // Store frame position for later use by ClickGuiScreen
                        myau.ui.clickgui.ClickGuiScreen.framePositions.put(categoryName, new int[]{x, y});
                    }
                }
                
                // Load category expanded states
                if (jsonObject.has("categoryExpandedStates")) {
                    JsonObject expandedStates = jsonObject.getAsJsonObject("categoryExpandedStates");
                    for (java.util.Map.Entry<String, JsonElement> entry : expandedStates.entrySet()) {
                        String categoryName = entry.getKey();
                        boolean isExpanded = entry.getValue().getAsBoolean();
                        myau.ui.clickgui.ClickGuiScreen.categoryExpandedStates.put(categoryName, isExpanded);
                    }
                }
                
                // Load last selected category
                if (jsonObject.has("lastSelectedCategory")) {
                    myau.ui.clickgui.ClickGuiScreen.lastSelectedCategory = jsonObject.get("lastSelectedCategory").getAsString();
                }
                
                // Load last selected module
                if (jsonObject.has("lastSelectedModule")) {
                    myau.ui.clickgui.ClickGuiScreen.lastSelectedModule = jsonObject.get("lastSelectedModule").getAsString();
                }
            }
            
            for (Module module : Myau.moduleManager.modules.values()) {
                JsonElement moduleObj = jsonObject.get(module.getName());
                if (moduleObj != null) {
                    JsonObject object = moduleObj.getAsJsonObject();
                    JsonElement toggled = object.get("toggled");
                    module.setEnabled(toggled.getAsBoolean());
                    JsonElement key = object.get("key");
                    module.setKey(key.getAsInt());
                    JsonElement hidden = object.get("hidden");
                    module.setHidden(hidden.getAsBoolean());
                    ArrayList<Property<?>> list = Myau.propertyManager.properties.get(module.getClass());
                    if (list != null) {
                        for (Property<?> property : list) {
                            if (object.has(property.getName())) {
                                property.read(object);
                            }
                        }
                    }
                }
            }
            ChatUtil.sendFormatted(String.format("%sConfig has been loaded (&a&o%s&r)&r", Myau.clientName, file.getName()));
        } catch (Exception e) {
            ((IAccessorMinecraft) mc).getLogger().error(e.getMessage());
            ChatUtil.sendFormatted(String.format("%sConfig couldn't be loaded (&c&o%s&r)&r", Myau.clientName, file.getName()));
        }
    }

    public void save() {
        try {
            JsonObject object = new JsonObject();
            // Save GUI colors
            // GuiConfig.save(object); // <--- REMOVED THIS LINE
            // Note: GuiConfig was removed, using GuiModule instead
            
            // Save Frame positions if saveGuiState is enabled
            myau.module.modules.GuiModule guiModule2 = (myau.module.modules.GuiModule) myau.Myau.moduleManager.modules.get(myau.module.modules.GuiModule.class);
            if (guiModule2 != null && guiModule2.saveGuiState.getValue()) {
                // Save frame positions
                if (!myau.ui.clickgui.ClickGuiScreen.framePositions.isEmpty()) {
                    JsonObject framePositions = new JsonObject();
                    for (java.util.Map.Entry<String, int[]> entry : myau.ui.clickgui.ClickGuiScreen.framePositions.entrySet()) {
                        JsonObject positionObj = new JsonObject();
                        positionObj.addProperty("x", entry.getValue()[0]);
                        positionObj.addProperty("y", entry.getValue()[1]);
                        framePositions.add(entry.getKey(), positionObj);
                    }
                    object.add("framePositions", framePositions);
                }
                
                // Save category expanded states
                if (!myau.ui.clickgui.ClickGuiScreen.categoryExpandedStates.isEmpty()) {
                    JsonObject expandedStates = new JsonObject();
                    for (java.util.Map.Entry<String, Boolean> entry : myau.ui.clickgui.ClickGuiScreen.categoryExpandedStates.entrySet()) {
                        expandedStates.addProperty(entry.getKey(), entry.getValue());
                    }
                    object.add("categoryExpandedStates", expandedStates);
                }
                
                // Save last selected category
                if (myau.ui.clickgui.ClickGuiScreen.lastSelectedCategory != null) {
                    object.addProperty("lastSelectedCategory", myau.ui.clickgui.ClickGuiScreen.lastSelectedCategory);
                }
                
                // Save last selected module
                if (myau.ui.clickgui.ClickGuiScreen.lastSelectedModule != null) {
                    object.addProperty("lastSelectedModule", myau.ui.clickgui.ClickGuiScreen.lastSelectedModule);
                }
            }
            
            for (Module module : Myau.moduleManager.modules.values()) {
                JsonObject moduleObject = new JsonObject();
                moduleObject.addProperty("toggled", module.isEnabled());
                moduleObject.addProperty("key", module.getKey());
                moduleObject.addProperty("hidden", module.isHidden());
                ArrayList<Property<?>> list = Myau.propertyManager.properties.get(module.getClass());
                if (list != null) {
                    for (Property<?> property : list) {
                        property.write(moduleObject);
                    }
                }
                object.add(module.getName(), moduleObject);
            }
            PrintWriter printWriter = new PrintWriter(new FileWriter(file));
            printWriter.println(gson.toJson(object));
            printWriter.close();
            ChatUtil.sendFormatted(String.format("%sConfig has been saved (&a&o%s&r)&r", Myau.clientName, file.getName()));
        } catch (IOException e) {
            ChatUtil.sendFormatted(String.format("%sConfig couldn't be saved (&c&o%s&r)&r", Myau.clientName, file.getName()));
        }
    }
}
