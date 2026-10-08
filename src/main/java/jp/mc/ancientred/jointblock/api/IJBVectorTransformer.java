package jp.mc.ancientred.jointblock.api;

import net.minecraft.world.phys.Vec3;

public interface IJBVectorTransformer {

    Vec3 transformRelative(Vec3 vec);

    Vec3 transformAbsolute(Vec3 vec);

    Vec3 transformOnlyRotation(Vec3 vec);
}
