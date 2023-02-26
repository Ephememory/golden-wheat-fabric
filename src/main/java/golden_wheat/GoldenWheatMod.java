package golden_wheat;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import golden_wheat.item.GoldWheat;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class GoldenWheatMod implements ModInitializer {

    public static String MOD_ID = "golden_wheat";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static Item GOLDENWHEATITEM = new GoldWheat(new Item.Settings().maxCount(64));

    public static final Identifier GILDED_WHEAT_PARTICLES_PACKET_ID = new Identifier(MOD_ID, "gilded_wheat_particles");

    private static ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        if(entity instanceof AnimalEntity animal) {
            ItemStack itemStack = player.getStackInHand(hand);
            if(itemStack.isOf(GOLDENWHEATITEM) && !world.isClient) {
                if(animal.isBaby()) {
                    animal.growUp(5000, true);
                    animal.setBreedingAge(1);
                    itemStack.decrement(1);

                    PacketByteBuf outBuf = PacketByteBufs.create();
                    outBuf.writeFloat((float) (entity.getX()));
                    outBuf.writeFloat((float) (entity.getY()));
                    outBuf.writeFloat((float) (entity.getZ()));
                    ServerPlayNetworking.send((ServerPlayerEntity) player, GILDED_WHEAT_PARTICLES_PACKET_ID, outBuf);

                    return ActionResult.CONSUME;
                }
            }
            return ActionResult.PASS;
        }
        return ActionResult.PASS;
    }

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, GoldWheat.id), GOLDENWHEATITEM);
        ClientPlayNetworking.registerGlobalReceiver(GILDED_WHEAT_PARTICLES_PACKET_ID, GoldenWheatMod::clientReceiveGildedWheatPacket);
        UseEntityCallback.EVENT.register(GoldenWheatMod::interact);
    }

    private static void clientReceiveGildedWheatPacket(MinecraftClient minecraftClient,
                                                       ClientPlayNetworkHandler clientPlayNetworkHandler,
                                                       PacketByteBuf packetByteBuf,
                                                       PacketSender packetSender) {
        float x = packetByteBuf.readFloat();
        float y = packetByteBuf.readFloat();
        float z = packetByteBuf.readFloat();
        Random r = new Random();

        for(int i = 0; i <= 6; i++) {
            minecraftClient.world.addParticle(ParticleTypes.HAPPY_VILLAGER,
                    x + r.nextFloat(1.5f),
                    y + r.nextFloat(2f),
                    z + r.nextFloat(1.5f), 0, 0, 0);
        }
    }

}
