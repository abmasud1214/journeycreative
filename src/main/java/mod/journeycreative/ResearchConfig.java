package mod.journeycreative;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Identifier;

import java.io.Reader;
import java.util.*;

public class ResearchConfig {
    public static final Map<Identifier, Integer> RESEARCH_AMOUNT_REQUIREMENTS = new HashMap<>();
    public static final Map<Identifier, List<Identifier>> RESEARCH_PREREQUISITES = new HashMap<>();
    public static final Set<Identifier> RESEARCH_PROHIBITED = new HashSet<>();

    public static void loadResearchAmounts(Reader reader) {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        JsonObject requirements = root.getAsJsonObject("requirements");

        for (Map.Entry<String, JsonElement> requirement : requirements.entrySet()) {
            Identifier itemId = Identifier.of(requirement.getKey());
            int amount = requirement.getValue().getAsInt();

            RESEARCH_AMOUNT_REQUIREMENTS.put(itemId, amount);
        }
    }

    public static void loadResearchPrerequisites(Reader reader) {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        JsonObject requirements = root.getAsJsonObject("prerequisites");

        for (Map.Entry<String, JsonElement> requirement : requirements.entrySet()) {
            Identifier itemId = Identifier.of(requirement.getKey());
            List<Identifier> itemPrereqs = new ArrayList<>();
            for (JsonElement element : requirement.getValue().getAsJsonArray()) {
                itemPrereqs.add(Identifier.of(element.getAsString()));
            }

            RESEARCH_PREREQUISITES.put(itemId, itemPrereqs);
        }
    }

    public static void loadResearchProhibited(Reader reader) {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        JsonArray prohibited = root.getAsJsonArray("unresearchable");
        for (JsonElement element : prohibited) {
            RESEARCH_PROHIBITED.add(Identifier.of(element.getAsString()));
        }
    }
}
