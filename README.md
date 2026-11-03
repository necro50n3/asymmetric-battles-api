[![Modrinth](https://img.shields.io/modrinth/dt/sI88vVWl?style=for-the-badge&logo=modrinth&label=Modrinth)](https://modrinth.com/mod/sI88vVWl)
[![CurseForge](https://img.shields.io/curseforge/dt/1717490?style=for-the-badge&logo=curseforge&label=Curseforge)](https://www.curseforge.com/minecraft/mc-mods/asymmetric-battles-api)
[![Discord](https://img.shields.io/discord/1355016729679499284?style=for-the-badge&logo=discord&label=Discord)](https://discord.gg/6jBar3y6nt)
[![GitHub](https://img.shields.io/badge/GitHub-565656?style=for-the-badge&logo=github)](https://github.com/necro50n3/asymmetric-battles-api)

# Asymmetric Battles API

## Expand the capabilities of Cobblemon's battle engine!

Asymmetric Battles API lifts core restrictions around Cobblemon's and Showdown's battle engine allowing for some truly wild battles.

![Sextuple Battle](https://i.imgur.com/1xtuMNX.png)

- Adds Quadruple (4v4), Pentuple (5v5), Sextuple (6v6) and Horde (1v6) battles.
- Allows starting Double and Triple battles (and the new formats) with fewer than the required number of Pokémon.
- Allows Multi Battles to start with only one actor on either side.
- Allows actors to join Multi Battles after they have already started.

## Compatibilities
Asymmetric Battles API heavily modifies the showdown files and may cause incompatibility issues with mods that do similar things, but includes built-in compatibility with a few popular mods.
- Mega Showdown
- Genesis Forms

## For Developers
<details>
<summary><b style="font-size: 16px">Getting Started</b></summary>

<pre><code>repositories {
    maven {
        name = "Cobbled Creators Maven"
        url = "https://maven.cobbled-creators.org/releases/"
    }
}</code></pre>
<details>
<summary><b>Common</b></summary>
<pre><code>dependencies {
    modImplementation "com.necro.asymmetric.battles:asymmetricbattles-common:&lt;version&gt;"
}</code></pre>
</details>

<details>
<summary><b>Fabric</b></summary>
<pre><code>dependencies {
    modImplementation "com.necro.asymmetric.battles:asymmetricbattles-fabric:&lt;version&gt;"
}</code></pre>
</details>

<details>
<summary><b>NeoForge</b></summary>
<pre><code>dependencies {
    modImplementation "com.necro.asymmetric.battles:asymmetricbattles-neoforge:&lt;version&gt;"
}</code></pre>
</details>

</details>
ㅤ
<details>
<summary><b style="font-size: 16px">Custom Spawns</b></summary>

A custom spawn listener can be registered using the BattleSpawnReloadListener, with a key word that defines the sub folder spawns can be registered in.
See [Cobblemon Horde Enocunters](https://github.com/necro50n3/cobblemon-horde-encounters) for an example on how to implement this.

A random spawn can be generated from the valid spawn pool using `AsymmetricAPI.getRandomBattleSpawn()`.

</details>
ㅤ
<details>
<summary><b style="font-size: 16px">Multi Battles</b></summary>

A BattleActor can be dynamically injected into an existing Multi Battle or Raid Battle using `AsymmetricAPI.setMultiBattleActor()` and `AsymmetricAPI.setRaidBattleActor()` respectively.

Additionally, a Multi Battle or Raid Battle can be started with insufficient actors on either side by using a `DummyActor`.
It is important to note that P1 and P2 cannot be a `DummyActor` when starting a battle, but can be replaced mid-battle.

</details>