package yay.evy.everest.vstuff;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(vstuff.MOD_ID)
public class vstuff {
    public static final String MOD_ID = "vstuff";
    public static final Logger LOGGER = LogUtils.getLogger();

    public vstuff() {
        LOGGER.info("VStuff mod initialized");


    }

    }


