package com.flansmod.common.gunshots;

import com.flansmod.common.actions.*;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.network.FlansModPacketHandler;
import com.flansmod.common.network.toclient.ShotFiredMessage;
import com.flansmod.common.network.toserver.ShotRequestMessage;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.guns.GunContext;
import com.flansmod.common.types.guns.GunDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class GunshotManager
{
	private HashMap<Entity, ActionStack> ActionStacks = new HashMap<Entity, ActionStack>();

	public ActionStack GetActionStack(Entity entity)
	{
		return GetOrCreateActionStack(entity);
	}

	// ----------------------------------------------------------------------------------------------------------------
	// CLIENT
	// ----------------------------------------------------------------------------------------------------------------

	public void HookClient(IEventBus modEventBus)
	{
		FlansModPacketHandler.RegisterClientHandler(ShotFiredMessage.class, ShotFiredMessage::new, this::OnClientShotsFired);
		MinecraftForge.EVENT_BUS.addListener(this::ClientTick);

	}

	@OnlyIn(Dist.CLIENT)
	public void ClientLookAt(Player player, InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		if(stack.getItem() instanceof GunItem gunItem)
		{
			// Set our context and work out what Look At will do
			GunContext gunContext = GunContext.CreateFromPlayer(player, hand);
			GunDefinition gunDef = gunContext.GunDef();
			ActionDefinition[] actionsDefs = gunContext.GetLookAtActions();

			// Then fire all the actions, with no sync to server
			List<Action> actions = new ArrayList<>();
			CreateActions(player, actionsDefs, actions, hand);
			TriggerActions(player.level, gunContext, actions);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void ClientShoot(Player player, InteractionHand hand, EActionSet actionSet)
	{
		ItemStack stack = player.getItemInHand(hand);
		if(stack.getItem() instanceof GunItem gunItem)
		{
			// Set our context and create the lists of actions this shoot event will trigger
			GunContext gunContext = GunContext.CreateFromPlayer(player, hand);
			GunDefinition gunDef = gunContext.GunDef();
			ActionDefinition[] actionsDefs = gunContext.GetActionDefinitions(actionSet);
			List<Action> otherActions = new ArrayList<>(actionsDefs.length);
			ShootAction shootAction = CreateActions(player, actionsDefs, otherActions, hand);

			ActionStack actionStack = GetOrCreateActionStack(player);

			//shootAction.FindLoadedAmmo(gunContext);

			// TODO: Check if we can shoot based on our local data about our
			 // a) Inventory, ammo levels
			 // b) Shoot cooldown
			 // c) Handedness

			boolean bCanFire = shootAction == null || shootAction.CanStart(gunContext);
			for(Action action : otherActions)
				if(!action.CanStart(gunContext))
					bCanFire = false;
			if(actionStack.GetShotCooldown() > 0.0f)
				bCanFire = false;

			if(bCanFire)
			{
				GunshotCollection shotsFired = null;
				if (shootAction != null)
				{
					// Calculate the GunshotCollection (as we believe it to be)
					// (Using some sort of deterministic random? - or sending our RNG results to server for analysis)
					shootAction.Calculate(gunContext, actionStack, actionSet);
					TriggerAction(player.level, gunContext, shootAction);
					shotsFired = shootAction.GetResults();
				}

				// Start any other actions we have attached to the trigger
				TriggerActions(player.level, gunContext, otherActions);

				// Finally, send our shot data to the server for verification, action and propogation
				ClientSendToServer(shotsFired);
			}
		}
	}

	// You fired a shot, tell the server where you shot
	@OnlyIn(Dist.CLIENT)
	private void ClientSendToServer(GunshotCollection shots)
	{
		FlansModPacketHandler.SendToServer(new ShotRequestMessage(shots));
	}

	// This will only be sent to you when someone else fires a shot. This is where you play various actions in response
	@OnlyIn(Dist.CLIENT)
	private void OnClientShotsFired(ShotFiredMessage msg)
	{
		// Reconstruct the shot details and context
		GunshotCollection shotCollection = msg.Get();
		InteractionHand hand = shotCollection.seatID == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		GunContext gunContext = GunContext.TryCreateFromEntity(shotCollection.Shooter(), hand);
		if(gunContext.IsValid())
		{
			ActionDefinition[] actionsDefs = gunContext.GetActionDefinitions(shotCollection.actionUsed);
			List<Action> otherActions = new ArrayList<>(actionsDefs.length);
			ShootAction shootAction = CreateActions(shotCollection.Shooter(), actionsDefs, otherActions, hand);

			// TODO: We should hash-check the action set we use, as there could be a race condition
			// between switching what actions are active for the weapon and triggering this code

			shootAction.SetResults(shotCollection);

			TriggerAction(shotCollection.Shooter().level, gunContext, shootAction);
			TriggerActions(shotCollection.Shooter().level, gunContext, otherActions);
		}

	}

	// If you fired some shots and told the server where they went, you will receive a verification message back
	// When you do, any rejected shots might cause your client to rollback some visuals, sounds or other actions
	@OnlyIn(Dist.CLIENT)
	private void OnClientVerifyShots()
	{

	}

	// ----------------------------------------------------------------------------------------------------------------
	// SERVER
	// ----------------------------------------------------------------------------------------------------------------

	public void HookServer(IEventBus modEventBus)
	{
		FlansModPacketHandler.RegisterServerHandler(ShotRequestMessage.class, ShotRequestMessage::new, this::OnServerReceivedShotData);
		MinecraftForge.EVENT_BUS.addListener(this::ServerTick);
	}

	// When a client tells us what they shot, we need to verify it
	private void OnServerReceivedShotData(ShotRequestMessage msg, ServerPlayer from)
	{
		// Reconstruct the shot details and context
		GunshotCollection shotCollection = msg.Get();
		InteractionHand hand = shotCollection.seatID == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		GunContext gunContext = GunContext.CreateFromPlayer(from, hand);
		ActionDefinition[] actionsDefs = gunContext.GetActionDefinitions(shotCollection.actionUsed);
		List<Action> otherActions = new ArrayList<>(actionsDefs.length);
		ShootAction shootAction = CreateActions(from, actionsDefs, otherActions, hand);

		// TODO: We should hash-check the action set we use, as there could be a race condition
		// between switching what actions are active for the weapon and triggering this code

		// Verify that this shot makes sense by itself
		// TODO: Check if we can shoot based on our local data about our
		// a) Inventory, ammo levels
		// b) Shoot cooldown
		// c) Handedness

		boolean bValid = shootAction.ValidateAndSetResults(gunContext, shotCollection);

		// TODO: Random spot check later - run a little statistical analysis on this player's shots over some time period

		// If we are happy, run all the server actions
		if(bValid)
		{
			TriggerAction(from.level, gunContext, shootAction);
			TriggerActions(from.level, gunContext, otherActions);

			// Then propogate the shot to all other interested parties
			ServerPropogateShot(gunContext, shotCollection);
		}

		// TODO: And let the shooter know whether we verified their shots or not
	}

	//
	private void ServerPropogateShot(GunContext gunContext, GunshotCollection shotCollection)
	{
		float noiseLevel = 100.0f; // gunContext.GetNoiseLevel();

		// Calculate everyone near the origin or near an endpoint
		List<Vec3> positions = new ArrayList<>(shotCollection.Count() * 2);
		for(int i = 0; i < shotCollection.Count(); i++)
		{
			positions.add(shotCollection.Get(i).origin);
			positions.add(shotCollection.Get(i).Endpoint());
		}

		// Then send them some messages about the shot
		FlansModPacketHandler.SendToAllAroundPoints(
			new ShotFiredMessage(shotCollection),
			shotCollection.dimension,
			positions,
			noiseLevel,
			gunContext.owner);
	}


	// ----------------------------------------------------------------------------------------------------------------
	// COMMON
	// ----------------------------------------------------------------------------------------------------------------

	private ShootAction CreateActions(Entity entity, ActionDefinition[] actionDefs, List<Action> nonShootActions, InteractionHand hand)
	{
		return CreateActions(GetOrCreateActionStack(entity), actionDefs, nonShootActions, hand);
	}

	private ShootAction CreateActions(ActionStack stack, ActionDefinition[] actionDefs, List<Action> nonShootActions, InteractionHand hand)
	{
		ShootAction shoot = null;
		for(ActionDefinition def : actionDefs)
		{
			Action action = Actions.CreateAction(stack, def, hand);
			if(action instanceof ShootAction found)
				shoot = found;
			else if(action != null)
				nonShootActions.add(action);
		}
		return shoot;
	}

	private ActionStack GetOrCreateActionStack(Entity entity)
	{
		ActionStack entitysActionStack = ActionStacks.get(entity);
		if(entitysActionStack == null)
		{
			entitysActionStack = new ActionStack();
			ActionStacks.put(entity, entitysActionStack);
		}
		return entitysActionStack;
	}

	private void TriggerActions(Level level, GunContext context, Collection<Action> actions)
	{
		ActionStack entitysActionStack = GetOrCreateActionStack(context.shootFrom);
		for(Action action : actions)
			entitysActionStack.AddAction(level, context, action);
	}

	private void TriggerAction(Level level, GunContext context, Action action)
	{
		ActionStack entitysActionStack = GetOrCreateActionStack(context.shootFrom);
		entitysActionStack.AddAction(level, context, action);
	}

	public void ServerTick(TickEvent.ServerTickEvent tickEvent)
	{
		if(tickEvent.phase == TickEvent.Phase.END)
		{
			for(var kvp : ActionStacks.entrySet())
			{
				Entity entity = kvp.getKey();
				ActionStack stack = kvp.getValue();
				GunContext mainHandContext = GunContext.TryCreateFromEntity(entity, InteractionHand.MAIN_HAND);
				GunContext offHandContext = GunContext.TryCreateFromEntity(entity, InteractionHand.OFF_HAND);
				stack.OnTick(entity.level, mainHandContext, offHandContext);
			}
		}
	}

	public void ClientTick(TickEvent.ClientTickEvent tickEvent)
	{
		if(tickEvent.phase == TickEvent.Phase.END)
		{
			for(var kvp : ActionStacks.entrySet())
			{
				Entity entity = kvp.getKey();
				ActionStack stack = kvp.getValue();
				GunContext mainHandContext = GunContext.TryCreateFromEntity(entity, InteractionHand.MAIN_HAND);
				GunContext offHandContext = GunContext.TryCreateFromEntity(entity, InteractionHand.OFF_HAND);
				stack.OnTick(Minecraft.getInstance().level, mainHandContext, offHandContext);
			}
		}
	}
}
