# Gravity Project for Minecraft Forge 1.16.5
> [!NOTE]
> Branch 1.16.5-forge is licenced under the MIT [License](LICENSE).

> [!IMPORTANT]
> It's THE BETA of Gravity Mod. It has some problems with Camera

This mod adds Gravity library to Minecraft 1.16.5 with Forge.

### Usage:
- For Player 
  - Download Jar file from [GitHub Releases](https://github.com/Weever1337/GravityProject/releases) and move the file to your `mods` folder.
- For Developer
  - In your build.gradle, add below code:
```groovy
repositories {
    maven {
        name = "Gravity Maven"
        url = uri("https://pkgs.dev.azure.com/weever1337/60e8f2cf-4d94-41e0-9b30-f900d6f1459d/_packaging/gravityproject/maven/v1")
    }
}


dependencies {
    implementation('org.weever.gravitymod:gravityproject:0.1-beta')
}
```

# Links
- Modrinth: *soon*
- Curseforge: *soon*

