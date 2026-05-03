package ru.fwx.packpulse;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class PackPulseFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PackPulseRuntime.start(FabricLoader.getInstance().getGameDir());
    }
}

