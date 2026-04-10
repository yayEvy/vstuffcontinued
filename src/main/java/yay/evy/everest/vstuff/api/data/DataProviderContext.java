package yay.evy.everest.vstuff.api.data;

import net.minecraft.data.CachedOutput;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class DataProviderContext {

    private final ArrayList<CompletableFuture<?>> futures;
    private final CachedOutput output;

    public DataProviderContext(CachedOutput output) {
        this.output = output;
        this.futures = new ArrayList<>();
    }

    CompletableFuture<?> combineFutures() {
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    public CachedOutput getOutput() {
        return this.output;
    }

    public void addFuture(CompletableFuture<?> future) {
        this.futures.add(future);
    }

}
