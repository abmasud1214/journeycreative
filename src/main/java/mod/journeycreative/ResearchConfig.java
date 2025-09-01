package mod.journeycreative;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Identifier;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class ResearchConfig {
    public static final Map<Identifier, Integer> RESEARCH_AMOUNT_REQUIREMENTS = new HashMap<>();

    public static void loadResearchAmounts(Reader reader) {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        JsonObject requirements = root.getAsJsonObject("requirements");

        for (Map.Entry<String, JsonElement> requirement : requirements.entrySet()) {
            Identifier itemId = Identifier.of(requirement.getKey());
            int amount = requirement.getValue().getAsInt();

            RESEARCH_AMOUNT_REQUIREMENTS.put(itemId, amount);
        }
    }
}
