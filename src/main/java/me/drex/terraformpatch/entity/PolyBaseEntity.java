package me.drex.terraformpatch.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.world.entity.EntityType;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolyBaseEntity(EntityType<?> entityType) implements PolymerEntity {

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
        return entityType;
    }
}
