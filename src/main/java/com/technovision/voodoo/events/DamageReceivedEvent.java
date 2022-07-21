package com.technovision.voodoo.events;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class DamageReceivedEvent {

    private ServerPlayerEntity player;
    private DamageSource source;
    private float amount;

    public DamageReceivedEvent(ServerPlayerEntity player, DamageSource source, float amount) {
        this.player = player;
        this.source = source;
        this.amount = amount;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    public DamageSource getSource() {
        return source;
    }

    public float getAmount() {
        return amount;
    }
}
