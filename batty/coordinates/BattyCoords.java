/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package batty.coordinates;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.Direction;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
/**
 *
 * @author BatHeart
 * @version 1.7.2 (1.4.2)
 */
public class BattyCoords extends Gui {
    
	private static Minecraft mc;
	private static GuiIngame gui = new GuiIngame(mc);
	
    int showCoords = 1;
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
    
	protected static final ResourceLocation batUIResourceLocation = new ResourceLocation("battyUI:textures/batheart_icon.png");
	
	static float batLogoScaler = 0.036F;
	static int batLogoU = 0;
	static int batLogoV = 0;
	static int batLogoX = (int) (256.0F * batLogoScaler);
	static int batLogoY = (int) (256.0D * batLogoScaler);    
    
    int coordLocation;    
	int myXLine, myYLine, myZLine, myBiomeLine;
	int myBaseOffset;
	int myCoord1Offset, myCoord2Offset;
	int myRHSlocation;
	int coordBoxW, coordBoxH;
	int coordBoxL, coordBoxR, coordBoxTop, coordBoxBase;
	
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
		this.retrieveRuntimeOptions();

    }
    
	/**
	 * Draws a rectangle of the texture provided at the location specified, sized and scaled as specified
	 */
	public static void drawTexture(int x, int y, int u, int v, int width,
			int height, ResourceLocation resourceLocation, float scaler)

	{
		x = (int) (x / scaler);
		y = (int) (y / scaler);

		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glScalef(scaler, scaler, scaler);

		mc.renderEngine.bindTexture(resourceLocation);
		gui.drawTexturedModalRect(x, y, u, v, width, height);

		GL11.glPopMatrix();
	}
/**
 * Calls drawTexture() to present the BatHeart Logo at the screen position specified
 * @param x - location across screen from left to right
 * @param y - location down screen from top to bottom
 */
	protected static void drawLogoTexture(int x, int y) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(255.0F, 255.0F, 255.0F, 255.0F);

		drawTexture(x, y, batLogoU, batLogoV, (int) (batLogoX / batLogoScaler),
				(int) (batLogoY / batLogoScaler), batUIResourceLocation,
				batLogoScaler);

		GL11.glDisable(GL11.GL_BLEND);
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
	 * Converts the Minecraft.coordLocation variable into a String ready for
	 * writing to options file
	 * 
	 * @return a String containing value from "0" to "3"
	 */
	private String constructCoordLocString() {
		String var1 = "";
		var1 = var1 + this.coordLocation;
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
        String myTxtLoc1 = propts.getProperty("Coords.layout.ScreenPosition");        
        
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
/*		
        if (myTxtLoc1 != null) {
			if (myTxtLoc1.equals("topleft")) {
				this.coordLocation = 0;
			} else if (myTxtLoc1.equals("topright")) {
				this.coordLocation = 1;
			} else if (myTxtLoc1.equals("bottomleft")) {
				this.coordLocation = 2;
			} else if (myTxtLoc1.equals("bottomright")) {
				this.coordLocation = 3;
			}
		}
*/		
    }
    
	/**
	 * Handles retrieval, interpretation and storage of the saved game data from
	 * the BatMod.runtime file
	 */
	private void retrieveRuntimeOptions() {

		if (this.runtimeFile.exists()) {
			try {
				FileInputStream fis = new FileInputStream(this.runtimeFile);
				try {
					proprt.load(fis);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			} catch (FileNotFoundException var5) {
				var5.printStackTrace();
			}
		}

		String myCoordsVis = proprt.getProperty("Coords.visible");
		if (myCoordsVis != null) {
			this.showCoords = Integer.parseInt(myCoordsVis);
		}

		String myCoordsLoc = proprt.getProperty("Coords.location");
		if (myCoordsLoc != null) {
			this.coordLocation = Integer.parseInt(myCoordsLoc);
		}


	}
    
	/**
	 * Handles writing away the game data to the BatMod.runtime file
	 */
	private void storeRuntimeOptions() {


		proprt.setProperty("Coords.visible", this.constructCoordVisString());

		proprt.setProperty("Coords.location", this.constructCoordLocString());

		try {
			FileOutputStream fos = new FileOutputStream(this.runtimeFile);
			proprt.store(fos, null);
			fos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
    /**
     * Writes the player's Coordinates and Compass Bearing onto the game screen
     * 
     */
    private void renderPlayerCoords() {
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        FontRenderer var8 = this.mc.fontRenderer;
        ScaledResolution myRes = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
        myPosX = MathHelper.floor_double(this.mc.thePlayer.posX);
        myPosY = MathHelper.floor_double(this.mc.thePlayer.boundingBox.minY);
        myPosZ = MathHelper.floor_double(this.mc.thePlayer.posZ);
        myAngle = getCardinalPoint(this.mc.thePlayer.rotationYaw);
        myDir = MathHelper.floor_double((double) (this.mc.thePlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        myMoveX = Direction.offsetX[myDir];
        myMoveZ = Direction.offsetZ[myDir];

        coordBoxW = 70;
        coordBoxH = 30;
        
		// screen locations
		switch (this.coordLocation) {
		case 0:
			// top left positioning
			coordBoxR = coordBoxW + 1;
			coordBoxBase = coordBoxH + 1;
			break;
		case 1:
			// top right positioning
			coordBoxR = myRes.getScaledWidth() - 1;
			coordBoxBase = coordBoxH + 1;
			break;
			
		case 2:
			//bottom right positioning
			coordBoxR = myRes.getScaledWidth() - 1;
			coordBoxBase = myRes.getScaledHeight() - 1;			
			break;
			
		case 3:
			// bottom left positioning ** not permitted **
			coordBoxR = coordBoxW + 1;
			coordBoxBase = myRes.getScaledHeight() - 1;
			break;
		}
		
		coordBoxL = coordBoxR - coordBoxW;
		coordBoxTop = coordBoxBase - coordBoxH;
		myXLine = coordBoxTop + 1;
		myYLine = myXLine + 10;
		myZLine = myYLine + 10;
		myBaseOffset = coordBoxL + 1;
		myCoord1Offset = myBaseOffset + 10;
		myRHSlocation = coordBoxR - 13;        
        
        if (this.shadedCoords) {
            drawRect((int) coordBoxL, coordBoxTop, coordBoxR, coordBoxBase, myRectColour);
        }

        var8.drawStringWithShadow(String.format("x: "), myBaseOffset, myXLine, myTitleText);
        var8.drawStringWithShadow(String.format("y: "), myBaseOffset, myYLine, myTitleText);
        var8.drawStringWithShadow(String.format("z: "), myBaseOffset, myZLine, myTitleText);
        
        if (myPosX >= 0) {
            var8.drawStringWithShadow(String.format("%d", new Object[]{Integer.valueOf(myPosX)}), myCoord1Offset, myXLine, myPosCoordText);
        } else {
            var8.drawStringWithShadow(String.format("%d", new Object[]{Integer.valueOf(myPosX)}), myCoord1Offset, myXLine, myNegCoordText);
        }
        var8.drawStringWithShadow(String.format("%d", new Object[]{Integer.valueOf(myPosY)}), myCoord1Offset, myYLine, myPosCoordText);
        if (myPosZ >= 0) {
            var8.drawStringWithShadow(String.format("%d", new Object[]{Integer.valueOf(myPosZ)}), myCoord1Offset, myZLine, myPosCoordText);
        } else {
            var8.drawStringWithShadow(String.format("%d", new Object[]{Integer.valueOf(myPosZ)}), myCoord1Offset, myZLine, myNegCoordText);
        }           
        
		drawLogoTexture((myRHSlocation - 12), (myYLine-1));        
        
        var8.drawStringWithShadow(myCardinalPoint[myAngle], myRHSlocation, myYLine, myCompassText);

       
    }
    /**
     * Toggles the showCoords boolean - called each time the F6 key is pressed
     */
    public void hideUnhideCoords() {
        this.showCoords += 1;
        if (this.showCoords > 1){
        	this.showCoords = 0;
        }
		this.storeRuntimeOptions();
        BattyCoordsKeys.keyToggleCoords = false;
    }
    
	/**
	 * Moves the position that the coordinates appear in on-screen between the
	 * four corners
	 */
	public void rotateScreenCoords() {
		this.coordLocation += 1;
		if (this.coordLocation > 2) {
			this.coordLocation = 0;
		}
		this.storeRuntimeOptions();
		BattyCoordsKeys.keyMoveCoords = false;
	}
    /**
     * Copies the current coordinates as a string into the System Clipboard, for the player to
     * paste wherever they wish
     */
	public void copyScreenCoords() {
		
	    StringSelection myCoordString = new StringSelection("x:" + myPosX + " y:" + myPosY + " z:" + myPosZ);
	    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(myCoordString, null);
		
		BattyCoordsKeys.keyCopyCoords = false;
	}
	
		
	
    /**
     * Publicly exposed method, handles Coordinate rendering when they are intended to appear
     */
    @SubscribeEvent
    public void renderPlayerInfo(RenderGameOverlayEvent event) {
        if (event.isCancelable() || event.type != ElementType.HOTBAR) {
            return;
        }

        if (BattyCoordsKeys.keyToggleCoords) {
            this.hideUnhideCoords();
        }

		if (BattyCoordsKeys.keyMoveCoords) {
			this.rotateScreenCoords();
		}
		
		if (BattyCoordsKeys.keyCopyCoords) {
			this.copyScreenCoords();
		}
		
        if (!this.mc.gameSettings.showDebugInfo) {
            if (this.showCoords > 0) {
                this.renderPlayerCoords();
            }

        }

    }
 
}
