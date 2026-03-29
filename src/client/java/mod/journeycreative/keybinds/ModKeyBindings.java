package mod.journeycreative.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import mod.journeycreative.Journeycreative;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class ModKeyBindings {
    public static final KeyMapping.Category JOURNEY_CREATIVE = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "main"));
    public static KeyMapping ROTATE_INVENTORY;
    public static KeyMapping REVERSE_ROTATE_INVENTORY;

    public static void register() {
        ROTATE_INVENTORY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.journeycreative.rotate_inventory",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                JOURNEY_CREATIVE
        ));

        REVERSE_ROTATE_INVENTORY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.journeycreative.reverse_rotate_inventory",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                JOURNEY_CREATIVE
        ));
    }

}
