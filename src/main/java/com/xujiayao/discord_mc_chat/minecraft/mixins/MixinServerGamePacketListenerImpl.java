package com.xujiayao.discord_mc_chat.minecraft.mixins;

import com.xujiayao.discord_mc_chat.minecraft.MinecraftEvents;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC == 11900
//$$ import java.util.Objects;
//#endif
import java.util.Optional;

import static com.xujiayao.discord_mc_chat.Main.SERVER;

/**
 * @author Xujiayao
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class MixinServerGamePacketListenerImpl {

	@Shadow
	private ServerPlayer player;

	@Inject(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;)V", at = @At("HEAD"), cancellable = true)
	private void broadcastChatMessage(PlayerChatMessage playerChatMessage, CallbackInfo ci) {
		Optional<Component> result = MinecraftEvents.PLAYER_MESSAGE.invoker().message(player, playerChatMessage.decoratedContent().getString());
		if (result.isPresent()) {
			SERVER.getPlayerList().broadcastChatMessage(playerChatMessage.withUnsignedContent(result.get()), this.player, ChatType.bind(ChatType.CHAT, player));
			ci.cancel();
		}
	}

	@Inject(method = "performChatCommand(Lnet/minecraft/network/protocol/game/ServerboundChatCommandPacket;Lnet/minecraft/network/chat/LastSeenMessages;)V", at = @At("HEAD"))
	private void performChatCommand(ServerboundChatCommandPacket serverboundChatCommandPacket, LastSeenMessages lastSeenMessages, CallbackInfo ci) {
		MinecraftEvents.PLAYER_COMMAND.invoker().command(player, "/" + serverboundChatCommandPacket.command());
	}
}
