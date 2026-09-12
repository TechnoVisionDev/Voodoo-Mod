package com.technovision.voodoo.events;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;

/**
 * Stores data to be passed to DamageReceivedEvent.
 *
 * @author TechnoVision
 */
public class DamageReceivedEvent {

    private ServerPlayer player;
    private DamageSource source;
    private float amount;

    public DamageReceivedEvent(ServerPlayer player, DamageSource source, float amount) {
        this.player = player;
        this.source = source;
        this.amount = amount;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public DamageSource getSource() {
        return source;
    }

    public float getAmount() {
        return amount;
    }
}
