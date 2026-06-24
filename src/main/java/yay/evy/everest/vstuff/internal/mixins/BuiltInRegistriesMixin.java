package yay.evy.everest.vstuff.internal.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.infrastructure.registry.VStuffBuiltInRegistries;

import java.util.function.Consumer;

@Mixin(BuiltInRegistries.class)
public class BuiltInRegistriesMixin {
    static { VStuffBuiltInRegistries.init(); }

    @WrapOperation(method = "validate", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;forEach(Ljava/util/function/Consumer;)V"))
    private static <T extends Registry<?>> void vstuff$weWantNiceRegistries(Registry<T> instance, Consumer<T> consumer, Operation<Void> original) {
        Consumer<T> callback = (t) -> {
            if (!t.key().location().getNamespace().equals(VStuff.MOD_ID))
                consumer.accept(t);
        };

        original.call(instance, callback);
    }
}