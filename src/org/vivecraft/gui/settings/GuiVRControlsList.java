package org.vivecraft.gui.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.vivecraft.control.ButtonTuple;
import org.vivecraft.control.VRButtonMapping;
import org.vivecraft.provider.MCOpenVR;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;

public class GuiVRControlsList extends ExtendedList
{
    private final GuiVRControls parent;
    private final Minecraft mc;
    
    public GuiVRControlsList(GuiVRControls parent, Minecraft mc)
    {
        super(Minecraft.getInstance(), parent.width + 80, parent.height, 63, parent.height - 32, 20);
        this.parent = parent;
        this.mc = mc;
        buildList();
    }
    

	@Override
	protected int getScrollbarPosition() {
		return this.width - 100;
	}    
	
	@Override
	public int getRowWidth() {
		return this.width;
	}
    
    public void buildList() {
        ArrayList<VRButtonMapping> bindings = new ArrayList<>(minecraft.vrSettings.buttonMappings.values());
        Collections.sort(bindings);
        this.clearEntries();

        String cat = null;
        int var7 = bindings.size();
        for (int i = 0; i < var7; i++)
        {
        	VRButtonMapping kb = bindings.get(i);
        	
        	if (parent.guiFilter != kb.isGUIBinding()) continue;
        	String s = kb.keyBinding != null ? kb.keyBinding.getKeyCategory() : (kb.isKeyboardBinding() ? "Keyboard Emulation" : null);
        	if (s == null) continue;
        	if (s != null && !s.equals(cat)) {
                cat = s;
                this.addEntry(new GuiVRControlsList.CategoryEntry(cat));
            }
            this.addEntry(new GuiVRControlsList.MappingEntry(kb, null));
        }     
    }
    
    private boolean checkMappingConflict(VRButtonMapping mapping) {
    	for (VRButtonMapping vb : minecraft.vrSettings.buttonMappings.values()) {
    		if (vb.conflictsWith(mapping))
    			return true;
    	}
    	return false;
    }

    public class CategoryEntry extends ExtendedList.AbstractListEntry
    {
        private final String labelText;
        private final int labelWidth;

        public CategoryEntry(String p_i45028_2_)
        {
            this.labelText = I18n.format(p_i45028_2_, new Object[0]);
            this.labelWidth = GuiVRControlsList.this.minecraft.fontRenderer.getStringWidth(this.labelText);
        }

		@Override
		public void render(int index, int x, int y, int width, int height, int mouseX, int mouseY, boolean p_194999_5_,float partialTicks)
        {
        //    minecraft.fontRenderer.drawString(this.labelText, GuiVRControlsList.this.minecraft.currentScreen.width / 2 - this.labelWidth / 2, this.getY() + entryHeight - GuiVRControlsList.this.minecraft.fontRenderer.FONT_HEIGHT - 1, 16777215);
        }

    }

    public class MappingEntry extends ExtendedList.AbstractListEntry
    {
        private final VRButtonMapping myKey;
        private final Button btnChangeKeyBinding;
        private final Button btnChangeKeyBindingList;
		private final Button btnClearKeyBinding;
		private List<String> tooltip = new ArrayList<>();
        private TextFieldWidget guiEnterText;
        private GuiVRControls parentGuiScreen;
        
        private MappingEntry(VRButtonMapping key, GuiVRControls parent)
        {
            this.myKey = key;
            this.btnChangeKeyBinding = null;//new Button(0, 0, 0, 140, 18, "") {};
            this.btnChangeKeyBindingList = null;//new Button(0, 0, 0, 38, 18, "Multi...") {};
			this.btnClearKeyBinding = null;//new Button(0, 0, 0, 18, 18, TextFormatting.RED + "X") {};
            this.parentGuiScreen = parent;
            updateButtonText();
        }
        
        private void updateButtonText() {
            String str = "";
            for (ButtonTuple tuple : myKey.buttons) {
            	if (tuple.controller.getController().isButtonActive(tuple.button)) {
            		if (!str.isEmpty()) {
            			str = "Multiple";
            			break;
            		}
            		str = tuple.toReadableString();
            	}
            }

            if (str.isEmpty()) {
            	str = "None";
			} else if (myKey.modifiers != 0) {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < MCOpenVR.MODIFIER_COUNT; i++) {
					if (myKey.hasModifier(i))
						sb.append("Mod ").append(i + 1).append(" + ");
				}

				str = sb.append(str).toString();
			}

			tooltip.clear();
			if (GuiVRControlsList.this.minecraft.fontRenderer.getStringWidth(str) > this.btnChangeKeyBinding.getWidth() - 6) {
				tooltip.add(str);

				StringBuilder sb = new StringBuilder(str);
				while (sb.length() > 5 && GuiVRControlsList.this.minecraft.fontRenderer.getStringWidth(StringUtils.abbreviate(sb.toString(), sb.length() - 1)) > this.btnChangeKeyBinding.getWidth() - 6) {
					sb.setLength(sb.length() - 1);
				}

				str = StringUtils.abbreviate(sb.toString(), sb.length() - 1);
			}

            if (parent.pressMode && parent.mapping == myKey) {
            	this.btnChangeKeyBinding.setMessage("> " + TextFormatting.YELLOW + str + TextFormatting.RESET + " <");
            } else if (!str.equals("None") && checkMappingConflict(myKey)) {
            	this.btnChangeKeyBinding.setMessage(TextFormatting.RED + str);
            } else {
            	this.btnChangeKeyBinding.setMessage(str);
            }
        }
        
		@Override
		public void render(int index, int x, int y, int width, int height, int mouseX, int mouseY, boolean p_194999_5_,float partialTicks)
        {
        	GuiVRControlsList.this.minecraft.fontRenderer.drawString(this.myKey.toReadableString(), x + 40, y + height / 2 - GuiVRControlsList.this.minecraft.fontRenderer.FONT_HEIGHT / 2, 16777215);
        	this.btnChangeKeyBinding.x = GuiVRControlsList.this.minecraft.currentScreen.width / 2 - 20;
        	this.btnChangeKeyBinding.y = y;
        	updateButtonText();

        	this.btnChangeKeyBinding.render(mouseX, mouseY, partialTicks);

        	this.btnChangeKeyBindingList.x = GuiVRControlsList.this.minecraft.currentScreen.width / 2 + 140;
        	this.btnChangeKeyBindingList.y = y;
        	this.btnChangeKeyBindingList.render(mouseX, mouseY, partialTicks);

			this.btnClearKeyBinding.x = GuiVRControlsList.this.minecraft.currentScreen.width / 2 + 121;
			this.btnClearKeyBinding.y = y;
			this.btnClearKeyBinding.render(mouseX, mouseY, partialTicks);

			if (this.btnChangeKeyBinding.isHovered()) {
				btnChangeKeyBinding.renderToolTip(mouseX, mouseY);
				GlStateManager.disableLighting();
				GlStateManager.disableDepthTest();
				RenderHelper.disableStandardItemLighting();
				GlStateManager.disableRescaleNormal();
			}
        }
        
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
		   	if (this.btnChangeKeyBinding.mouseClicked(mouseX, mouseY, button))
        	{
        		if (!parent.pressMode) {
        			parent.pressMode = true;
                	parent.mapping = myKey;   
                	parent.mappingButtons = new HashSet<>(myKey.buttons);
                	parent.mappingModifiers = myKey.modifiers;
                	return true;
        		} else if (parent.mapping == myKey) {
    				parent.pressMode = false;
        			parent.mapping = null;
        			parent.mappingButtons = null;
        			return true;
        		}
        		return false;
        	}
        	else if (this.btnChangeKeyBindingList.mouseClicked(mouseX, mouseY, button))
            {           	
        		if (parent.pressMode) return false;
            	parent.selectionMode = true;
            	parent.mapping = myKey;   
            	parent.mappingButtons = new HashSet<>(myKey.buttons);
            	parent.mappingModifiers = myKey.modifiers;
            	return true;          
            }
        	else if (this.btnClearKeyBinding.mouseClicked(mouseX, mouseY, button))
        	{
				if (parent.pressMode) return false;
				
	        	if (myKey.isKeyboardBinding()) {
	            	GuiVRControlsList.this.minecraft.vrSettings.buttonMappings.remove(myKey.functionId);
	            	GuiVRControlsList.this.buildList();
	        	} else if (!myKey.functionId.equals("GUI Left Click")) { // Another anti-screwing thing
					myKey.buttons.removeIf(tuple -> tuple.controller.getController().isButtonActive(tuple.button));
					myKey.modifiers = 0;
	        	}
	        	
				return true;
			}
            else
            {
                return false;
            }
		}
		


//        @Override
//        public boolean mouseReleased(double mouseX, double mouseY, int button) {
//            this.btnChangeKeyBinding.mouseReleased(mouseX, mouseY, button);
//            this.btnDeleteKeyBinding.mouseReleased(mouseX, mouseY, button);
//        }
        

    }
}
