# Cooking for Blockheads

Minecraft Mod. Adds a cooking book and multiblock kitchens that only shows recipes you can make with what you currently have in your inventory.

- [Modpack Permissions](https://mods.twelveiterations.com/permissions)

#### Downloads

[![Versions](http://cf.way2muchnoise.eu/versions/231484_latest.svg)](https://www.curseforge.com/minecraft/mc-mods/cooking-for-blockheads)
[![Downloads](http://cf.way2muchnoise.eu/full_231484_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/cooking-for-blockheads)

## Contributing

If you're interested in contributing to the mod, you can check
out [issues labelled as "help wanted"](https://github.com/TwelveIterationMods/CookingForBlockheads/issues?q=is%3Aopen+is%3Aissue+label%3A%22help+wanted%22).

When it comes to new features, it's best to confer with me first to ensure we share the same vision. You can join us on [Discord](https://discord.gg/VAfZ2Nau6j) if you'd like to talk.

Contributions must be done through pull requests. I will not be able to accept translations, code or other assets through any other channels.

## IMC API

The below is a list of IMC message keys handled by Cooking for Blockheads.

* **RegisterTool** (ItemStack)
* **RegisterWaterItem** (ItemStack)
* **RegisterMilkItem** (ItemStack)
* **RegisterToast** (TagCompound : {Input : ItemStack, Output : ItemStack})
* **RegisterToastError** (TagCompound : {Input : ItemStack, Message : String})
* **RegisterOvenFuel** (TagCompound : {Input : ItemStack, FuelValue : Numeric})
* **RegisterOvenRecipe** (TagCompound : {Input : ItemStack, Output : ItemStack})
* **RegisterNonFoodRecipe** (ItemStack)
* **RegisterCowClass** (EntityCowClassName : String) - this will cause the class to be loaded if it isn't already!

## Java API

If the IMC API is not enough for you, you can build against Cooking for Blockheads' Java API. I suggest using the CurseForge Maven to grab it as a dependency. For info on how to do that, you can check Cooking for Blockheads' own Gradle files.

The Java API allows everything the IMC API does, and certain tasks can only be achieved via the Java API.
However, if you don't need that extra control, it is recommended to use the IMC API.

## Adding Cooking for Blockheads to a development environment

Note that you will also need to add Balm if you want to test your integration in your environment.

### Using CurseMaven

Add the following to your `build.gradle`:

```groovy
repositories {
    maven { url "https://www.cursemaven.com" }
}

dependencies {
    // Replace ${cookingforblockheads_file_id} and ${balm_file_id} with the id of the file you want to depend on.
    // You can find it in the URL of the file on CurseForge (e.g. 3914527).
    // Forge: implementation fg.deobf("curse.maven:balm-531761:${balm_file_id}")
    // Fabric: modImplementation "curse.maven:balm-fabric-500525:${balm_file_id}"
    
    // Forge: implementation fg.deobf("curse.maven:cooking-for-blockheads-231484:${cookingforblockheads_file_id}")
    // Fabric: modImplementation "curse.maven:cooking-for-blockheads-fabric-634546:${cookingforblockheads_file_id}"
}
```

### Using Twelve Iterations Maven (includes snapshot and mojmap versions)

Add the following to your `build.gradle`:

```groovy
repositories {
    maven { 
        url "https://maven.twelveiterations.com/repository/maven-public/" 
        
        content {
            includeGroup "net.blay09.mods"
        }
    }
}

dependencies {
    // Replace ${cookingforblockheads_version} and ${balm_version} with the version you want to depend on. 
    // You can find the latest version for a given Minecraft version at https://maven.twelveiterations.com/service/rest/repository/browse/maven-public/net/blay09/mods/balm-common/ and https://maven.twelveiterations.com/service/rest/repository/browse/maven-public/net/blay09/mods/cookingforblockheads-common/
    // Common (mojmap): implementation "net.blay09.mods:balm-common:${balm_version}"
    // Forge: implementation fg.deobf("net.blay09.mods:balm-forge:${balm_version}")
    // Fabric: modImplementation "net.blay09.mods:balm-fabric:${balm_version}"
    
    // Common (mojmap): implementation "net.blay09.mods:cookingforblockheads-common:${cookingforblockheads_version}"
    // Forge: implementation fg.deobf("net.blay09.mods:cookingforblockheads-forge:${cookingforblockheads_version}")
    // Fabric: modImplementation "net.blay09.mods:cookingforblockheads-fabric:${cookingforblockheads_version}"
}
```
