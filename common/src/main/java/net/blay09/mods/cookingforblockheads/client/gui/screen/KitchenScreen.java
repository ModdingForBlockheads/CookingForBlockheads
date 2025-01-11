package net.blay09.mods.cookingforblockheads.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.blay09.mods.balm.mixin.AbstractContainerScreenAccessor;
import net.blay09.mods.cookingforblockheads.CookingForBlockheads;
import net.blay09.mods.cookingforblockheads.CookingForBlockheadsConfig;
import net.blay09.mods.cookingforblockheads.client.gui.SortButton;
import net.blay09.mods.cookingforblockheads.menu.KitchenMenu;
import net.blay09.mods.cookingforblockheads.menu.slot.CraftMatrixFakeSlot;
import net.blay09.mods.cookingforblockheads.menu.slot.CraftableListingFakeSlot;
import net.blay09.mods.cookingforblockheads.registry.CookingForBlockheadsRegistry;
import net.blay09.mods.cookingforblockheads.tag.ModItemTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class KitchenScreen extends AbstractContainerScreen<KitchenMenu> {

    private static final int SCROLLBAR_COLOR = 0xFFAAAAAA;
    private static final int SCROLLBAR_Y = 8;
    private static final int SCROLLBAR_WIDTH = 7;
    private static final int SCROLLBAR_HEIGHT = 77;

    private static final ResourceLocation guiTexture = ResourceLocation.fromNamespaceAndPath(CookingForBlockheads.MOD_ID, "textures/gui/gui.png");
    private static final int VISIBLE_ROWS = 4;
    private static final int VISIBLE_COLS = 3;

    private int scrollBarScaledHeight;
    private int scrollBarXPos;
    private int scrollBarYPos;
    private int currentOffset;

    private double mouseClickY = -1;
    private int indexWhenClicked;
    private int lastNumberOfMoves;

    private Button btnNextRecipe;
    private Button btnPrevRecipe;

    private EditBox searchBar;

    private final List<SortButton> sortButtons = new ArrayList<>();

    private final String[] noIngredients;
    private final String[] noSelection;

    public KitchenScreen(KitchenMenu menu, Inventory playerInventory, Component displayName) {
        super(menu, playerInventory, displayName);

        noIngredients = I18n.get("gui.cookingforblockheads.no_ingredients").split("\\\\n");
        noSelection = I18n.get("gui.cookingforblockheads.no_selection").split("\\\\n");
    }

    @Override
    protected void init() {
        imageHeight = 174;
        super.init();

        btnPrevRecipe = Button.builder(Component.literal("<"), it -> menu.nextRecipe(-1))
                .pos(width / 2 - 79, height / 2 - 51).size(13, 20).build();
        btnPrevRecipe.visible = false;
        addRenderableWidget(btnPrevRecipe);

        btnNextRecipe = Button.builder(Component.literal(">"), it -> menu.nextRecipe(1))
                .pos(width / 2 - 9, height / 2 - 51).size(13, 20).build();
        btnNextRecipe.visible = false;
        addRenderableWidget(btnNextRecipe);

        searchBar = new EditBox(minecraft.font, leftPos + imageWidth - 78, topPos - 5, 70, 10, searchBar, Component.empty());
        setInitialFocus(searchBar);
        addRenderableWidget(searchBar);

        int yOffset = -80;

        for (final var sortButton : CookingForBlockheadsRegistry.getSortButtons()) {
            SortButton button = new SortButton(width / 2 + 87, height / 2 + yOffset, sortButton, it -> {
                menu.setSortComparator(sortButton.getComparator(Minecraft.getInstance().player));
            });
            addRenderableWidget(button);
            sortButtons.add(button);

            yOffset += 20;
        }

        recalculateScrollBar();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (deltaY == 0) {
            return false;
        }

        if (menu.getSelectedRecipe() != null && mouseX >= leftPos + 24 && mouseY >= topPos + 20 && mouseX < leftPos + 78 && mouseY < topPos + 74) {
            Slot slot = ((AbstractContainerScreenAccessor) this).getHoveredSlot();
            if (slot instanceof CraftMatrixFakeSlot fakeSlot && fakeSlot.getVisibleStacks().size() > 1) {
                final var lockedInput = fakeSlot.scrollDisplayListAndLock(deltaY > 0 ? -1 : 1);
                menu.setLockedInput(fakeSlot.getIngredientIndex(), lockedInput);
            }
        } else {
            setCurrentOffset(deltaY > 0 ? currentOffset - 1 : currentOffset + 1);
        }

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        boolean result = super.mouseReleased(mouseX, mouseY, state);

        if (state != -1 && mouseClickY != -1) {
            mouseClickY = -1;
            indexWhenClicked = 0;
            lastNumberOfMoves = 0;
        }

        return result;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 && mouseX >= searchBar.getX() && mouseX < searchBar.getX() + searchBar.getWidth() && mouseY >= searchBar.getY() && mouseY < searchBar.getY() + searchBar.getHeight()) {
            searchBar.setValue("");
            menu.search(null);
            menu.updateCraftableSlots();
            setCurrentOffset(currentOffset);
            return true;
        } else {
            if (searchBar.mouseClicked(mouseX, mouseY, button)) {
                setFocused(searchBar);
                return true;
            }
        }

        if (mouseX >= scrollBarXPos && mouseX <= scrollBarXPos + SCROLLBAR_WIDTH && mouseY >= scrollBarYPos && mouseY <= scrollBarYPos + scrollBarScaledHeight) {
            mouseClickY = mouseY;
            indexWhenClicked = currentOffset;
        }

        Slot mouseSlot = ((AbstractContainerScreenAccessor) this).getHoveredSlot();
        if (mouseSlot instanceof CraftMatrixFakeSlot fakeSlot) {
            if (button == 0) {
                ItemStack itemStack = mouseSlot.getItem();
                final var recipe = menu.findCraftableForResultItem(itemStack);
                if (recipe != null) {
                    menu.selectCraftable(recipe);
                    setCurrentOffset(menu.getRecipesForSelectionIndex());
                }
            } else if (button == 1) {
                final var lockedInput = fakeSlot.toggleLock();
                menu.setLockedInput(fakeSlot.getIngredientIndex(), lockedInput);
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        boolean result = super.charTyped(c, keyCode);

        menu.search(searchBar.getValue());
        menu.updateCraftableSlots();
        setCurrentOffset(currentOffset);

        return result;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.player.closeContainer();
            return true;
        }

        if (searchBar.keyPressed(keyCode, scanCode, modifiers) || searchBar.isFocused()) {
            menu.search(searchBar.getValue());
            menu.updateCraftableSlots();
            setCurrentOffset(currentOffset);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        if (menu.isScrollOffsetDirty()) {
            setCurrentOffset(currentOffset);
            menu.setScrollOffsetDirty(false);
        }

        guiGraphics.blit(RenderType::guiTextured, guiTexture, leftPos, topPos - 10, 0, 0, imageWidth, imageHeight + 10, 256, 256);

        if (mouseClickY != -1) {
            float pixelsPerFilter = (SCROLLBAR_HEIGHT - scrollBarScaledHeight) / (float) Math.max(1,
                    (int) Math.ceil(menu.getItemListCount() / (float) VISIBLE_COLS) - VISIBLE_ROWS);
            if (pixelsPerFilter != 0) {
                int numberOfFiltersMoved = (int) ((mouseY - mouseClickY) / pixelsPerFilter);
                if (numberOfFiltersMoved != lastNumberOfMoves) {
                    setCurrentOffset(indexWhenClicked + numberOfFiltersMoved);
                    lastNumberOfMoves = numberOfFiltersMoved;
                }
            }
        }

        btnPrevRecipe.visible = menu.selectionHasRecipeVariants();
        btnPrevRecipe.active = menu.selectionHasPreviousRecipe();
        btnNextRecipe.visible = menu.selectionHasRecipeVariants();
        btnNextRecipe.active = menu.selectionHasNextRecipe();

        boolean hasRecipes = menu.getItemListCount() > 0;

        for (Button sortButton : sortButtons) {
            sortButton.active = hasRecipes;
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        Font font = minecraft.font;
        final var selection = menu.getSelectedRecipe();
        if (selection == null) {
            int curY = topPos + 79 / 2 - noSelection.length / 2 * font.lineHeight;
            for (String s : noSelection) {
                guiGraphics.drawString(font, s, leftPos + 23 + 27 - font.width(s) / 2, curY, 0xFFFFFFFF, true);
                curY += font.lineHeight + 5;
            }
        } else if (selection.recipeDisplayEntry().display() instanceof FurnaceRecipeDisplay) {
            guiGraphics.blit(RenderType::guiTextured, guiTexture, leftPos + 23, topPos + 19, 54, 184, 54, 54, 256, 256);
        } else {
            guiGraphics.blit(RenderType::guiTextured, guiTexture, leftPos + 23, topPos + 19, 0, 184, 54, 54, 256, 256);
        }

        if (selection != null) {
            for (CraftMatrixFakeSlot slot : menu.getMatrixSlots()) {
                if (slot.isLocked() && slot.getVisibleStacks().size() > 1) {
                    guiGraphics.blit(RenderType::guiTextured, guiTexture, leftPos + slot.x, topPos + slot.y, 176, 60, 16, 16, 256, 256);
                }
            }
        }

        guiGraphics.fill(scrollBarXPos, scrollBarYPos, scrollBarXPos + SCROLLBAR_WIDTH, scrollBarYPos + scrollBarScaledHeight, SCROLLBAR_COLOR);

        if (menu.getItemListCount() == 0) {
            guiGraphics.fill(leftPos + 97, topPos + 7, leftPos + 168, topPos + 85, 0xAA222222);
            int curY = topPos + 79 / 2 - noIngredients.length / 2 * font.lineHeight;
            for (String s : noIngredients) {
                guiGraphics.drawString(font, s, leftPos + 97 + 36 - font.width(s) / 2, curY, 0xFFFFFFFF, true);
                curY += font.lineHeight + 5;
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (CookingForBlockheadsConfig.getActive().showIngredientIcon) {
            var poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(0, 0, 300);
            for (Slot slot : menu.slots) {
                if (slot instanceof CraftableListingFakeSlot fakeSlot) {
                    if (slot.getItem().is(ModItemTags.INGREDIENTS)) {
                        guiGraphics.blit(RenderType::guiTextured, guiTexture, slot.x, slot.y, 176, 76, 16, 16, 256, 256);
                    }

                    final var craftable = fakeSlot.getCraftable();
                    if (craftable != null && craftable.missingUtensils()) {
                        guiGraphics.blit(RenderType::guiTextured, guiTexture, slot.x, slot.y, 176, 92, 16, 16, 256, 256);
                    }
                }
            }

            poseStack.popPose();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        var poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(0, 0, 300);
        for (Slot slot : menu.slots) {
            if (slot instanceof CraftMatrixFakeSlot fakeSlot) {
                if (fakeSlot.isMissing() && !slot.getItem().isEmpty()) {
                    guiGraphics.fillGradient(leftPos + slot.x, topPos + slot.y, leftPos + slot.x + 16, topPos + slot.y + 16, 0x77FF4444, 0x77FF5555);
                }
            }
        }
        poseStack.popPose();

        for (CraftMatrixFakeSlot matrixSlot : menu.getMatrixSlots()) {
            matrixSlot.updateSlot(partialTicks);
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void recalculateScrollBar() {
        int scrollBarTotalHeight = SCROLLBAR_HEIGHT - 1;
        this.scrollBarScaledHeight = (int) (scrollBarTotalHeight * Math.min(1f, ((float) VISIBLE_ROWS / (Math.ceil(menu.getItemListCount() / (float) VISIBLE_COLS)))));
        this.scrollBarXPos = leftPos + imageWidth - SCROLLBAR_WIDTH - 9;
        this.scrollBarYPos = topPos + SCROLLBAR_Y + ((scrollBarTotalHeight - scrollBarScaledHeight) * currentOffset / Math.max(1,
                (int) Math.ceil((menu.getItemListCount() / (float) VISIBLE_COLS)) - VISIBLE_ROWS));
    }

    private void setCurrentOffset(int currentOffset) {
        this.currentOffset = Math.max(0, Math.min(currentOffset, (int) Math.ceil(menu.getItemListCount() / (float) VISIBLE_COLS) - VISIBLE_ROWS));

        menu.setScrollOffset(this.currentOffset);

        recalculateScrollBar();
    }

    public List<Button> getSortingButtons() {
        return new ArrayList<>(sortButtons);
    }

}
