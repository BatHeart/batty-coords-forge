/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package batty.coordinates;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.Direction;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
/**
 *
 * @author BatHeart
 * @version 1.7.2 (1.3.1)
 */
public class BattyCoords extends Gui {
    
    private final Minecraft mc;
    boolean showCoords = true;
    boolean shadedCoords = true;
    int myTitleText = 0xFF8800; // default textcolour = oldgold
    int myPosCoordText = 0x55FFFF; // default coordscolour = aqua
    int myNegCoordText = 0xCCFFFF; // default coordscolour = coolblue
    int myCoordText = 0x55FFFF; // default coordscolour = aqua
    int myCompassText = 0xFF8800; // default compasscolour = oldgold
    int myRectColour = 0x88555555;
    int myPosX;
    int myPosY;
    int myPosZ;
    int myAngle;
    int myDir;
    int myMoveX;
    int myMoveZ;
    int myFind;
    private static final String[] myCardinalPoint = new String[] {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
    private static final String[] myColourList = new String[]
       {"black", "darkblue", "darkgreen", "darkaqua",
        "darkred", "purple", "brown", "grey", 
        "darkgrey", "blue", "green", "aqua",
        "sage", "violet",
        "orange", "lime", "silver", "coolblue", 
        "red","gold", "oldgold", "lightpurple", "pink", "yellow", "white"};
    private static final int[] myColourCodes = 
       {0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 
        0xAA0000, 0xAA00AA, 0xAA5500, 0xAAAAAA, 
        0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 
        0x88AA00, 0x8855CC, 
        0xCC5500, 0xCCFF00, 0xCCCCCC, 0xCCFFFF, 
        0xFF5555, 0xFFAA00, 0xFF8800, 0xFF55FF, 0xFFAAAA, 0xFFFF55, 0xFFFFFF};
    private File optionsFile;
    private File runtimeFile;
    private int secondCounter = 0;
    private int minuteCounter = 0;
    private int hourCounter = 0;
    private int tickCounter = 0;
    Properties propts = new Properties();
    Properties proprt = new Properties();
    /**
     * Constructor for BattyCoords: also handles reading of option files
     * @param par1Minecraft Instance of Minecraft, giving access to variables and methods 
     */
    public BattyCoords(Minecraft par1Minecraft) {
        this.mc = par1Minecraft;
        this.optionsFile = new File(this.mc.mcDataDir, "BatMod.properties");
        this.runtimeFile = new File(this.mc.mcDataDir, "BatMod.runtime");


        this.retrieveOptions();

    }
    /**
     * Searches for 'name' within the array 'names'
     * @param names Array of String to be searched
     * @param name String that we expect to find
     * @return Integer element where match found, or -1 if no match
     */    
    private static int nameSearch(String[] names, String name) {
        for (int n = 0; n < names.length; n++) {
            if (names[n].equals(name)) {
                return n;
            }
        }
        return -1;
    }

	/**
	 * Given a 360-degree compass bearing, converts it to an integer relating to
	 * one of the 8 cardinal points of the compass
	 * 
	 * @param par0
	 *            Floating point 360-degree compass bearing
	 * @return integer compass direction (0=North, 1=North-East etc)
	 */
	private int getCardinalPoint(float par0) {
		double myPoint;
		myPoint = MathHelper.wrapAngleTo180_float(par0) + 180D;
		myPoint += 22.5D;
		myPoint %= 360D;
		myPoint /= 45D;
		return MathHelper.floor_double(myPoint);
	}

	/**
	 * Converts the showCoords variable into a String ready to be written away to the runtime options file
	 * @return
	 */
	private String constructCoordVisString() {
		String var1 = "";
		var1 = var1 + this.showCoords;
		return var1;
	}
	/**
	 * Handles the retrieval of options from the BatMod.properties file, interprets and stores the values 
	 */
    private void retrieveOptions() {


        if (this.optionsFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(this.optionsFile);
                try {
                    propts.load(fis);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } catch (FileNotFoundException var5) {
                var5.printStackTrace();
            }
        }
        
        String myShade = propts.getProperty("Coords.shade");
        String myTxtCol1 = propts.getProperty("Coords.colours.TitleText");
        String myTxtCol6 = propts.getProperty("Coords.colours.CoordText");
        String myTxtCol2 = propts.getProperty("Coords.colours.PosCoordText");
        String myTxtCol7 = propts.getProperty("Coords.colours.NegCoordText");
        String myTxtCol3 = propts.getProperty("Coords.colours.CompassText");
        
        if (myShade != null) {
            this.shadedCoords = myShade.equals("true");
        }
        if (myTxtCol1 != null) {
            myFind = nameSearch(myColourList, myTxtCol1);
            if (myFind != -1){
                myTitleText = myColourCodes[myFind];
            }
        }

        if (myTxtCol2 != null) {
            myFind = nameSearch(myColourList, myTxtCol2);
            if (myFind != -1){
                myPosCoordText = myColourCodes[myFind];
            }
        }
        if (myTxtCol7 != null) {
            myFind = nameSearch(myColourList, myTxtCol7);
            if (myFind != -1) {
                myNegCoordText = myColourCodes[myFind];
            }
        }
        if (myTxtCol6 != null) {
            myFind = nameSearch(myColourList, myTxtCol6);
            if (myFind != -1) {
                myPosCoordText = myColourCodes[myFind];
                myNegCoordText = myColourCodes[myFind];
            }
        }        
        if (myTxtCol3 != null) {
            myFind = nameSearch(myColourList, myTxtCol3);
            if (myFind != -1){
                myCompassText = myColourCodes[myFind];
            }
        }

    }
    /**
     * Writes the player's Coordinates and Compass Bearing onto the game screen
     * 
     */
    private void renderPlayerCoords() {
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        FontRenderer var8 = this.mc.fontRenderer;

        myPosX = MathHelper.floor_double(this.mc.thePlayer.posX);
        myPosY = MathHelper.floor_double(this.mc.thePlayer.posY);
        myPosZ = MathHelper.floor_double(this.mc.thePlayer.posZ);
        myAngle = getCardinalPoint(this.mc.thePlayer.rotationYaw);
        myDir = MathHelper.floor_double((double) (this.mc.thePlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        myMoveX = Direction.offsetX[myDir];
        myMoveZ = Direction.offsetZ[myDir];

        if (this.shadedCoords) {
            drawRect((int) 1, 1, 71, 31, myRectColour);
        }

        var8.drawStringWithShadow(String.format("X: "), 2, 2, myTitleText);
        var8.drawStringWithShadow(String.format("Y: "), 2, 12, myTitleText);
        var8.drawStringWithShadow(String.format("Z: "), 2, 22, myTitleText);
        
        if (myPosX >= 0) {
            var8.drawStringWithShadow(String.format("%d", new Object[]{Integer.valueOf(myPosX)}), 12, 2, myPosCoordText);
        } else {
            var8.drawStringWithShadow(String.format("%d", new Object[]{Integer.valueOf(myPosX)}), 12, 2, myNegCoordText);
        }
        var8.drawStringWithShadow(String.format("%d", new Object[]{Integer.valueOf(myPosY)}), 12, 12, myPosCoordText);
        if (myPosZ >= 0) {
            var8.drawStringWithShadow(String.format("%d", new Object[]{Integer.valueOf(myPosZ)}), 12, 22, myPosCoordText);
        } else {
            var8.drawStringWithShadow(String.format("%d", new Object[]{Integer.valueOf(myPosZ)}), 12, 22, myNegCoordText);
        }        
        
        var8.drawStringWithShadow(myCardinalPoint[myAngle], 58, 12, myCompassText);

        //var8.drawStringWithShadow(String.format(Direction.directions[myDir].substring(0, 1)), 62, 12, 0xFF8800);


        //ItemStack var10 = new ItemStack(Item.compass);
        //itemRenderer.renderItemAndEffectIntoGUI(var8, this.mc.renderEngine, var10, 66, 2);        
    }
    /**
     * Toggles the showCoords boolean - called each time the F6 key is pressed
     */
    public void hideUnhideCoords() {
        this.showCoords = !this.showCoords;
        BattyCoordsKeys.keyToggleCoords = false;
    }
    /**
     * Publicly exposed method, handles Coordinate rendering when they are intended to appear
     */
    @SubscribeEvent
    public void renderPlayerInfo(RenderGameOverlayEvent event) {
        if (event.isCancelable() || event.type != ElementType.EXPERIENCE) {
            return;
        }

        if (BattyCoordsKeys.keyToggleCoords) {
            this.hideUnhideCoords();
        }

        if (!this.mc.gameSettings.showDebugInfo) {
            if (this.showCoords) {
                this.renderPlayerCoords();
            }

        }

    }
 
}
