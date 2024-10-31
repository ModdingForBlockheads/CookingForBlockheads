package net.blay09.mods.cookingforblockheads.client;

import net.blay09.mods.balm.api.client.screen.BalmScreens;
import net.blay09.mods.cookingforblockheads.client.gui.screen.*;
import net.blay09.mods.cookingforblockheads.menu.ModMenus;

public class ModScreens {
    public static void initialize(BalmScreens screens) {
        screens.registerScreen(ModMenus.spiceRack::get, SpiceRackScreen::new);
        screens.registerScreen(ModMenus.oven::get, OvenScreen::new);
        screens.registerScreen(ModMenus.counter::get, CounterScreen::new);
        screens.registerScreen(ModMenus.fridge::get, FridgeScreen::new);
        screens.registerScreen(ModMenus.fruitBasket::get, FruitBasketScreen::new);
        screens.registerScreen(ModMenus.noFilterBook::get, KitchenScreen::new);
        screens.registerScreen(ModMenus.recipeBook::get, KitchenScreen::new);
        screens.registerScreen(ModMenus.craftingBook::get, KitchenScreen::new);
        screens.registerScreen(ModMenus.cookingTable::get, KitchenScreen::new);
        screens.registerScreen(ModMenus.cuttingBoard::get, CuttingBoardScreen::new);
    }
}
