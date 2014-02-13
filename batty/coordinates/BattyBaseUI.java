/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package batty.coordinates;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "Batty's Coordinates", name = "Batty's Coordinates", version = "1.7.2_1.3.0")
/**
 * Forge Base class for BattyCoords
 * @author BatHeart
 */
public class BattyBaseUI {

	/**
	 * This is the instance of Batty's Coordinates mod that Forge uses
	 */
	@Instance("Batty's Coordinates")
	public static BattyBaseUI instance;
	/**
	 * Says where the client and server 'proxy' code is loaded.
	 */
	@SidedProxy(clientSide = "batty.coordinates.client.ClientProxy", serverSide = "batty.coordinates.CommonProxy")
	public static CommonProxy proxy;

    public static KeyBinding hideunhideCoordskey = new KeyBinding("Hide / Unhide Coords", Keyboard.KEY_NUMPAD4,"Batty's Coordinates");
    public static KeyBinding moveCoordScreenPos = new KeyBinding("Change Coords Screen Position", Keyboard.KEY_NUMPAD1,"Batty's Coordinates");

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ClientRegistry.registerKeyBinding(hideunhideCoordskey);
        ClientRegistry.registerKeyBinding(moveCoordScreenPos);		
	}

	@EventHandler
	public void load(FMLInitializationEvent event) {
		proxy.registerRenderers();

	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new BattyCoords(Minecraft
				.getMinecraft()));
		FMLCommonHandler.instance().bus().register(new BattyCoordsKeys());
	}

}
