package ru.fwx.packpulse;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;

@Mod(value = PackPulseRuntime.MOD_ID, dist = Dist.CLIENT)
public final class PackPulseNeoForge {
    public PackPulseNeoForge(IEventBus modEventBus) {
        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> PackPulseRuntime.start(FMLPaths.GAMEDIR.get()));
    }
}

