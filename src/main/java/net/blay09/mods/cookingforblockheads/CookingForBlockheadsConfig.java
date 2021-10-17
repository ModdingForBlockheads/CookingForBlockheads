package net.blay09.mods.cookingforblockheads;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class CookingForBlockheadsConfig {

    public static class Common {
        public final ForgeConfigSpec.BooleanValue cowJarEnabled;
        public final ForgeConfigSpec.DoubleValue cowJarMilkPerTick;
        public final ForgeConfigSpec.DoubleValue compressedCowJarMilkMultiplier;
        public final ForgeConfigSpec.BooleanValue sinkRequiresWater;
        public final ForgeConfigSpec.BooleanValue largeCounters;
        public final ForgeConfigSpec.BooleanValue disallowOvenAutomation;
        public final ForgeConfigSpec.DoubleValue ovenFuelTimeMultiplier;
        public final ForgeConfigSpec.DoubleValue ovenCookTimeMultiplier;
        public final ForgeConfigSpec.BooleanValue ovenRequiresCookingOil;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> modBlacklist;

        public Common(ForgeConfigSpec.Builder builder) {
            cowJarEnabled = builder
                    .comment("If true, a cow can be squished into a Milk Jar by dropping an anvil on top.")
                    .translation("waystones.config.cowJarEnabled")
                    .define("cowJarEnabled", true);

            cowJarMilkPerTick = builder
                    .comment("The amount of milk the cow in a jar generates per tick.")
                    .translation("waystones.config.cowJarMilkPerTick")
                    .defineInRange("cowJarMilkPerTick", 0.5f, 0, Float.MAX_VALUE);

            compressedCowJarMilkMultiplier = builder
                    .comment("Ex Compressum compatibility. Multiplier applied to the milk per tick for Compressed Cow in a Jar.")
                    .translation("waystones.config.compressedCowJarMilkMultiplier")
                    .defineInRange("compressedCowJarMilkMultiplier", 9, 0, Float.MAX_VALUE);

            sinkRequiresWater = builder
                    .comment("Set this to true if you'd like the sink to require water to be piped in, instead of providing infinite of it.")
                    .translation("waystones.config.sinkRequiresWater")
                    .define("sinkRequiresWater", false);

            largeCounters = builder
                    .comment("Enabling this will make the kitchen counters have twice as much inventory space.")
                    .translation("waystones.config.largeCounters")
                    .define("largeCounters", false);

            disallowOvenAutomation = builder
                    .comment("Set this to true if you'd like to disallow automation of the oven (pipes and such won't be able to insert/extract)")
                    .translation("waystones.config.disallowOvenAutomation")
                    .define("disallowOvenAutomation", false);

            ovenFuelTimeMultiplier = builder
                    .comment("The fuel multiplier for the cooking oven. Higher values means fuel lasts longer, 1.0 is furnace default.")
                    .translation("waystones.config.ovenFuelTimeMultiplier")
                    .defineInRange("ovenFuelTimeMultiplier", 0.33f, 0.1f, 2f);

            ovenCookTimeMultiplier = builder
                    .comment("The cooking time multiplier for the cooking oven. Higher values means it will take longer.")
                    .translation("waystones.config.ovenCookTimeMultiplier")
                    .defineInRange("ovenCookTimeMultiplier", 1f, 0.25f, 9f);

            ovenRequiresCookingOil = builder
                    .comment("Set this to true if you'd like the oven to only accept cooking oil as fuel (requires Pam's Harvestcraft)")
                    .translation("waystones.config.ovenRequiresCookingOil")
                    .define("ovenRequiresCookingOil", false);

            modBlacklist = builder
                    .comment("A list of mod namespaces from which you wouldn't like to see ingredients listed in the recipe book")
                    .translation("waystones.config.modBlacklist")
                    .defineList("modBlacklist", Lists.newArrayList(""), s -> s instanceof String);
        }
    }

    public static class Client {
        public final ForgeConfigSpec.BooleanValue showIngredientIcon;

        public Client(ForgeConfigSpec.Builder builder) {
            showIngredientIcon = builder
                    .comment("Set to false if you don't want ingredients to be marked with a special icon in the recipe book.")
                    .translation("waystones.config.showIngredientIcon")
                    .define("showIngredientIcon", true);
        }
    }

    static final ForgeConfigSpec commonSpec;
    public static final Common COMMON;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    static final ForgeConfigSpec clientSpec;
    public static final Client CLIENT;

    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        clientSpec = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

}
