package com.flansmod.physics.common.tests;

import com.flansmod.physics.common.FlansPhysicsMod;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.util.TransformStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

public class TransformTests {

    public static void runTests()
    {
        // -----------------------------------------------------------------------------------------
        // - Test Section #1 - Check ToEuler and FromEuler are inverse with single parameter input -
        Vector3f eulerYaw90 = new Vector3f(0f, 90f, 0f);
        Vector3f eulerYaw135 = new Vector3f(0f, 135f, 0f);
        Vector3f eulerPitchUp90 = new Vector3f(-90f, 0f, 0f);
        Vector3f eulerPitchDown45 = new Vector3f(45f, 0f, 0f);
        Vector3f eulerPitchDown90 = new Vector3f(90f, 0f, 0f);
        Vector3f eulerRollLeft45 = new Vector3f(0f, 0f, -45f);
        Vector3f eulerRollRight45 = new Vector3f(0f, 0f, 45f);
        Vector3f eulerRollRight60 = new Vector3f(0f, 0f, 60f);
        Vector3f eulerRollRight90 = new Vector3f(0f, 0f, 90f);
        Quaternionf yaw90 = Transform.quatFromEuler(eulerYaw90);
        Quaternionf yaw135 = Transform.quatFromEuler(eulerYaw135);
        Quaternionf pitchUp90 = Transform.quatFromEuler(eulerPitchUp90);
        Quaternionf pitchDown45 = Transform.quatFromEuler(eulerPitchDown45);
        Quaternionf pitchDown90 = Transform.quatFromEuler(eulerPitchDown90);
        Quaternionf rollLeft45 = Transform.quatFromEuler(eulerRollLeft45);
        Quaternionf rollRight45 = Transform.quatFromEuler(eulerRollRight45);
        Quaternionf rollRight60 = Transform.quatFromEuler(eulerRollRight60);
        Quaternionf rollRight90 = Transform.quatFromEuler(eulerRollRight90);
        assertEqual(eulerYaw90, Transform.toEuler(yaw90), "ToEuler != FromEuler^-1");
        assertEqual(eulerYaw135, Transform.toEuler(yaw135), "ToEuler != FromEuler^-1");
        assertEqual(eulerPitchUp90, Transform.toEuler(pitchUp90), "ToEuler != FromEuler^-1");
        assertEqual(eulerPitchDown45, Transform.toEuler(pitchDown45), "ToEuler != FromEuler^-1");
        assertEqual(eulerPitchDown90, Transform.toEuler(pitchDown90), "ToEuler != FromEuler^-1");
        assertEqual(eulerRollRight60, Transform.toEuler(rollRight60), "ToEuler != FromEuler^-1");
        assertEqual(eulerRollLeft45, Transform.toEuler(rollLeft45), "ToEuler != FromEuler^-1");
        assertEqual(eulerRollRight45, Transform.toEuler(rollRight45), "ToEuler != FromEuler^-1");
        assertEqual(eulerRollRight90, Transform.toEuler(rollRight90), "ToEuler != FromEuler^-1");

        // ------------------------------------------------------------------------------------
        // - Test Section #2 - Check multi-parameter euler angles -
        Vector3f eulerYaw90Pitch45 = new Vector3f(45f, 90f, 0f);
        Vector3f eulerYaw90Pitch45Roll45 = new Vector3f(45f, 90f, 45f);
        Vector3f eulerPitch45Roll60 = new Vector3f(45f, 0f, 60f);
        Quaternionf yaw90Pitch45 = Transform.quatFromEuler(eulerYaw90Pitch45);
        Quaternionf yaw90Pitch45Roll45 = Transform.quatFromEuler(eulerYaw90Pitch45Roll45);
        Quaternionf pitch45Roll60 = Transform.quatFromEuler(eulerPitch45Roll60);
        assertEqual(eulerYaw90Pitch45, Transform.toEuler(yaw90Pitch45), "ToEuler != FromEuler^-1");
        assertEqual(eulerYaw90Pitch45Roll45, Transform.toEuler(yaw90Pitch45Roll45), "ToEuler != FromEuler^-1");
        assertEqual(eulerPitch45Roll60, Transform.toEuler(pitch45Roll60), "ToEuler != FromEuler^-1");



        // ------------------------------------------------------------------------------------------------
        // - Test Section #3 - Check that these Quaternions do expected things to the Minecraft unit axes -
        Vector3f mcNorth = new Vector3f(0f, 0f, -1f);
        Vector3f mcEast = new Vector3f(1f, 0f, 0f);
        Vector3f mcSouth = new Vector3f(0f, 0f, 1f);
        Vector3f mcWest = new Vector3f(-1f, 0f, 0f);
        Vector3f mcUp = new Vector3f(0f, 1f, 0f);
        Vector3f mcDown = new Vector3f(0f, -1f, 0f);
        // Yaw 90 tests
        assertEqual(Transform.rotate(mcSouth, yaw90), 	    mcWest, 	"South * Yaw90 != West");
        assertEqual(Transform.rotate(mcWest, yaw90),  	    mcNorth, 	"West * Yaw90 != North");
        assertEqual(Transform.rotate(mcNorth, yaw90), 	    mcEast, 	"North * Yaw90 != East");
        assertEqual(Transform.rotate(mcEast, yaw90),  	    mcSouth, 	"East * Yaw90 != South");
        assertEqual(Transform.rotate(mcUp, yaw90), 		    mcUp, 		"Up * Yaw90 != Up");
        assertEqual(Transform.rotate(mcDown, yaw90),  	    mcDown, 	"Down * Yaw90 != Down");
        // Pitch up 90 tests
        assertEqual(Transform.rotate(mcNorth, pitchUp90), 	mcUp, 	    "North * PitchUp90 != Up");
        assertEqual(Transform.rotate(mcEast, pitchUp90), 	    mcEast,     "East * PitchUp90 != East");
        assertEqual(Transform.rotate(mcUp, pitchUp90), 	    mcSouth,    "Up * PitchUp90 != South");
        // Pitch down 90 tests
        assertEqual(Transform.rotate(mcNorth, pitchDown90), 	mcDown,     "North * PitchDown90 != Down");
        assertEqual(Transform.rotate(mcEast, pitchDown90), 	mcEast,     "East * PitchDown90 != East");
        assertEqual(Transform.rotate(mcUp, pitchDown90), 	    mcNorth,    "Up * PitchDown90 != North");
        // Roll tests
        assertEqual(Transform.rotate(mcNorth, rollRight90), 	mcNorth,    "North * RollRight90 != North");
        assertEqual(Transform.rotate(mcEast, rollRight90), 	mcDown,     "East * PitchDown90 != Down");
        assertEqual(Transform.rotate(mcUp, rollRight90), 	    mcEast,     "Up * PitchDown90 != East");



        // Composition tests
        Vector3f mcDownNorth = new Vector3f(0f, -Maths.cosF(Maths.TauF / 8f), -Maths.sinF(Maths.TauF / 8f));
        Vector3f mcDownEast = new Vector3f(Maths.sinF(Maths.TauF / 8f), -Maths.cosF(Maths.TauF / 8f), 0f);
        assertEqual(Transform.rotate(mcNorth, pitchDown45), mcDownNorth, "Pitch north not as expected");
        assertEqual(Transform.rotate(Transform.rotate(mcNorth, pitchDown45), yaw90), mcDownEast, "Pitch east not as expected");

        assertEqual(
                yaw90Pitch45.transform(mcNorth, new Vector3f()),
                yaw90.transform(pitchDown45.transform(mcNorth, new Vector3f()), new Vector3f()),//Rotate(Rotate(mcNorth, pitchDown45), yaw90),
                "YawPitch composition incorrect");


        // ------------------------------------------------------------------------------------
        // - Test Section #4 - Check Quaternion composition matches our expected behaviour -
        // Minecraft applies Roll(Z), then Pitch(X), then Yaw(Y)
        assertEqual(Transform.compose(pitchDown45, yaw90), yaw90Pitch45, "Composition unexpected");
        assertEqual(Transform.compose(rollRight45, pitchDown45, yaw90), yaw90Pitch45Roll45, "Composition unexpected");
        assertEqual(Transform.compose(rollRight60, pitchDown45), pitch45Roll60, "Composition unexpected");

        // ----------------------------------------------------------------------------------------------
        // - Test Section #5 - Validate some basic Transforms
        assertEqual(Transform.IDENTITY.forward(), mcNorth, "IDENTITY.Forward() not North");
        assertEqual(Transform.IDENTITY.up(), mcUp, "IDENTITY.Up() not Up");
        assertEqual(Transform.IDENTITY.right(), mcEast, "IDENTITY.Right() not East");
        // Test the properties of translations
        Transform offsetXAxis = Transform.fromPos(1d, 0d, 0d, () -> "X Axis");
        Transform offsetYAxis = Transform.fromPos(0d, 1d, 0d, () -> "Y Axis");
        Transform offsetZAxis = Transform.fromPos(0d, 0d, 1d, () -> "Z Axis");
        verifyCommutative(offsetXAxis, offsetYAxis);
        verifyCommutative(offsetXAxis, offsetZAxis);
        verifyAssociative(offsetXAxis, offsetYAxis, offsetZAxis);
        // Test the properties of rotations
        Transform tYaw90 = Transform.fromEuler(eulerYaw90, () -> "Yaw90");
        Transform tYaw135 = Transform.fromEuler(eulerYaw135, () -> "Yaw135");
        Transform tPitchUp90 = Transform.fromEuler(eulerPitchUp90, () -> "PitchUp90");
        Transform tPitchDown45 = Transform.fromEuler(eulerPitchDown45, () -> "PitchDown45");
        Transform tRollRight45 = Transform.fromEuler(eulerRollRight45, () -> "RollRight45");
        Transform tRollRight90 = Transform.fromEuler(eulerRollRight90, () -> "RollRight90");
        assertEqual(TransformStack.of(tYaw90, tYaw90, tYaw90, tYaw90).Top(), Transform.IDENTITY, "4 turns != identity");
        verifyAssociative(tRollRight45, tPitchUp90, tYaw90);
        verifyCommutative(tYaw90, tYaw135);
        verifyCommutative(tRollRight45, tRollRight90);
        verifyCommutative(tPitchUp90, tPitchDown45);
        // Interaction between translation and rotation
        verifyCommutative(offsetXAxis, tPitchUp90);
        verifyCommutative(offsetYAxis, tYaw90);
        verifyCommutative(offsetZAxis, tRollRight45);


        // Test Section #6 - Look Along
        assertEqual(Transform.fromLookDirection(tYaw90.forward(), tYaw90.up()), tYaw90, "Look along failed");
        assertEqual(Transform.fromLookDirection(tPitchDown45.forward(), tPitchDown45.up()), tPitchDown45, "Look along failed");
        assertEqual(Transform.fromLookDirection(tRollRight90.forward(), tRollRight90.up()), tRollRight90, "Look along failed");

        // Test Section #7 - Non-uniform scale
        Transform flipTest = Transform.fromPosAndEuler(new Vec3(30d, 31.3d, -12d), 45f, 43f, 13f, () -> "FlipTest");
        assertEqual(flipTest, flipTest.reflect(true, false, false).reflect(true, false, false), "FlipX not self-inverse");
        assertEqual(flipTest, flipTest.reflect(false, true, false).reflect(false, true, false), "FlipY not self-inverse");
        assertEqual(flipTest, flipTest.reflect(false, false, true).reflect(false, false, true), "FlipZ not self-inverse");

        Transform composed = TransformStack.of(tYaw90, tRollRight45, tPitchDown45).Top();
        assertEqual(Transform.fromLookDirection(composed.forward(), composed.up()), composed, "Look along failed");

    }
    private static void verifyAssociative(@Nonnull Transform a, @Nonnull Transform b, @Nonnull Transform c)
    {
        assertEqual(TransformStack.of(TransformStack.of(a, b).Top(), c).Top(),
                TransformStack.of(a, TransformStack.of(b, c).Top()).Top(),
                "Transforms not associative");
    }
    private static void verifyCommutative(@Nonnull Transform a, @Nonnull Transform b)
    {
        assertEqual(
                TransformStack.of().andThen(a).andThen(b).Top(),
                TransformStack.of().andThen(b).andThen(a).Top(),
                "Transforms not commutative");
    }

    private static final float Epsilon = 0.03f;
    private static void assertEqual(@Nonnull Transform a, @Nonnull Transform b, @Nonnull String error)
    {
        Vector3f eulerA = Transform.toEuler(a.Orientation);
        Vector3f eulerB = Transform.toEuler(b.Orientation);
        if(!Maths.approx(eulerA.x, eulerB.x, Epsilon)
                || !Maths.approx(eulerA.y, eulerB.y, Epsilon)
                || !Maths.approx(eulerA.z, eulerB.z, Epsilon)
                || !Maths.approx(a.Position.x, b.Position.x, Epsilon)
                || !Maths.approx(a.Position.y, b.Position.y, Epsilon)
                || !Maths.approx(a.Position.z, b.Position.z, Epsilon)
                || !Maths.approx(a.Scale.x, b.Scale.x, Epsilon)
                || !Maths.approx(a.Scale.y, b.Scale.y, Epsilon)
                || !Maths.approx(a.Scale.z, b.Scale.z, Epsilon))
        {
            FlansPhysicsMod.LOGGER.error(error);
        }
    }


    private static void assertEqual(@Nonnull Quaternionf a, @Nonnull Quaternionf b, @Nonnull String error)
    {
        if(!Maths.approx(a.x, b.x, Epsilon)
                || !Maths.approx(a.y, b.y, Epsilon)
                || !Maths.approx(a.z, b.z, Epsilon)
                || !Maths.approx(a.w, b.w, Epsilon))
        {
            Vector3f eulerA = Transform.toEuler(a);
            Vector3f eulerB = Transform.toEuler(b);
            FlansPhysicsMod.LOGGER.error(error + ":" + eulerA + "," + eulerB);
        }
    }

    private static void assertEqual(@Nonnull Vec3 a, @Nonnull Vec3 b, @Nonnull String error)
    {
        if(!Maths.approx(a.x, b.x, Epsilon)
                || !Maths.approx(a.y, b.y, Epsilon)
                || !Maths.approx(a.z, b.z, Epsilon))
        {
            FlansPhysicsMod.LOGGER.error(error);
        }
    }
    private static void assertEqual(@Nonnull Vec3 a, @Nonnull Vector3f b, @Nonnull String error)
    {
        if(!Maths.approx(a.x, b.x, Epsilon)
                || !Maths.approx(a.y, b.y, Epsilon)
                || !Maths.approx(a.z, b.z, Epsilon))
        {
            FlansPhysicsMod.LOGGER.error(error);
        }
    }

    private static void assertEqual(@Nonnull Vector3f a, @Nonnull Vector3f b, @Nonnull String error)
    {
        if(!Maths.approx(a.x, b.x, Epsilon)
                || !Maths.approx(a.y, b.y, Epsilon)
                || !Maths.approx(a.z, b.z, Epsilon))
        {
            FlansPhysicsMod.LOGGER.error(error);
        }
    }

    private static void assertEqual(float a, float b, @Nonnull String error)
    {
        if(!Maths.approx(a, b, Epsilon))
        {
            FlansPhysicsMod.LOGGER.error(error);
        }
    }
}
