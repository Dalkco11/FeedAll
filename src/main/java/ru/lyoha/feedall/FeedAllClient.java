package ru.lyoha.feedall;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class FeedAllClient implements ClientModInitializer {

    private KeyMapping keyBinding;

    public static final KeyMapping.Category CATEGORY = 
        KeyMapping.Category.register(Identifier.fromNamespaceAndPath("feedall", "feedall"));

    @Override
    public void onInitializeClient() {
        keyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.feedall.feed",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null || client.gameMode == null) {
                return;
            }
            while (keyBinding.consumeClick()) {
                var player = client.player;
                var level = client.level;
                var stack = player.getMainHandItem();
                if (stack.isEmpty()) {
                    continue;
                }
                double radius = 16.0;
                double x = player.getX();
                double y = player.getY();
                double z = player.getZ();
                AABB box = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
                var animals = level.getEntitiesOfClass(Animal.class, box, entity -> true);
                if (animals.isEmpty()) {
                    continue;
                }
                for (Animal animal : animals) {
                    if (!animal.isAlive()) {
                        continue;
                    }
                    if (animal.isBaby()) {
                        continue;
                    }
                    if (!animal.isFood(stack)) {
                        continue;
                    }
                    client.gameMode.interact(player, animal, new EntityHitResult(animal), InteractionHand.MAIN_HAND);
                }
            }
        });
    }
}
