package ru.lyoha.feedall;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import org.lwjgl.glfw.GLFW;

public class FeedAllClient implements ClientModInitializer {

    private KeyBinding keyBinding;

    @Override
    public void onInitializeClient() {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.feedall.feed",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.feedall"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null || client.interactionManager == null) {
                return;
            }
            while (keyBinding.wasPressed()) {
                var player = client.player;
                var world = client.world;
                var stack = player.getMainHandStack();
                if (stack.isEmpty()) {
                    continue;
                }
                double radius = 16.0;
                double x = player.getX();
                double y = player.getY();
                double z = player.getZ();
                Box box = new Box(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
                var animals = world.getEntitiesByClass(AnimalEntity.class, box, entity -> true);
                if (animals.isEmpty()) {
                    continue;
                }
                for (AnimalEntity animal : animals) {
                    if (!animal.isAlive()) {
                        continue;
                    }
                    if (animal.isBaby()) {
                        continue;
                    }
                    if (!animal.isBreedingItem(stack)) {
                        continue;
                    }
                    client.interactionManager.interactEntity(player, animal, Hand.MAIN_HAND);
                }
            }
        });
    }
}
