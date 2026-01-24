package yay.evy.everest.vstuff.content.physgrabber;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.valkyrienskies.core.api.ships.LoadedShip;

public class GrabberHum extends AbstractTickableSoundInstance {
    private final LoadedShip ship;
    private final Player player;
    private boolean isStopping = false;
    private float fade = 1.0f;

    protected GrabberHum(SoundEvent sound, LoadedShip ship, Player player) {
        super(sound, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.ship = ship;
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.4f;
        this.pitch = 0.6f;
    }

    public void startStopping() {
        this.isStopping = true;
    }

    @Override
    public void tick() {
        if (isStopping) {
            fade -= 0.05f;
            this.pitch *= 0.9f;
            this.volume *= 0.8f;

            if (fade <= 0 || this.volume <= 0.01f) {
                this.stop();
            }
            return;
        }

        if (this.ship == null || PhysGrabberClientHandler.getGrabbedShip() != this.ship) {
            startStopping();
            return;
        }

        Vec3 shipPos = PhysGrabberClientHandler.getGrabbedShipPos(1.0F);
        if (shipPos != null) {
            this.x = (float) shipPos.x;
            this.y = (float) shipPos.y;
            this.z = (float) shipPos.z;

            Vec3 target = player.getEyePosition(1.0F).add(player.getLookAngle().scale(5.0));
            double distance = shipPos.distanceTo(target);

            float targetPitch = 0.6f + (float) Math.min(distance * 0.2, 0.5f);
            float targetVolume = 0.4f + (float) Math.min(distance * 0.3, 0.4f);

            this.pitch += (targetPitch - this.pitch) * 0.03f;
            this.volume += (targetVolume - this.volume) * 0.03f;
        }
    }
}