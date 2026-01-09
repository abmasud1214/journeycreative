package mod.journeycreative.keybinds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class ModKeyBindings {
    public static KeyBinding ROTATE_INVENTORY;
    public static KeyBinding REVERSE_ROTATE_INVENTORY;

    public static void register() {
        ROTATE_INVENTORY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.journeycreative.rotate_inventory",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.journeycreative"
        ));

        REVERSE_ROTATE_INVENTORY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.journeycreative.reverse_rotate_inventory",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.journeycreative"
        ));
    }

}
