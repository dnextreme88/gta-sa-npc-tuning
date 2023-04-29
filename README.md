# Introduction

This is a Grand Theft Auto: San Andreas CLEO plugin based from the [original NPC Tuning gist](https://gist.github.com/JuniorDjjr/a68191d9f4b5fb642c7f658ca9c1b8bf) by [JuniorDjjr](https://github.com/JuniorDjjr), where all cars with tuning parts, and paintjobs can appear in the street, not just limited to the racing cars or low riders. In addition, they have a higher chance of appearing in the street.

Take note that if the tuning part is specified for the car under ```carmods.dat``` and that tuning part is not supported by the car, this mod will cause your game to crash, as there's a chance that it will load that erratic tuning part. If this happens, use the tuning parts specified for your modded car, or use the default ```carmods.dat``` that came with the game for unmodded cars.

## Installation

Installation is fairly simple: 

1. First, download the [CLEO library](https://cleo.li/) and extract the contents to your GTA SA root directory, where your ```gta_sa.exe``` executable is found.

2. Download the [CLEO+](https://www.mixmods.com.br/2022/11/cleoplus/) library and extract the contents under the ```CLEO/``` directory.

3. Download [modloader](https://www.mixmods.com.br/2018/01/modloader/) and extract the contents to your GTA SA root directory, where your ```gta_sa.exe``` executable is found.

4. Extract the ```NPC_Tuning``` directory from this repository to your `modloader/` directory. You may edit your settings in the file called ```NPC Tuning.ini```.

5. Run GTA SA and see if there are cars on the streets that are tuned. The tuning parts applied to these cars are based on what is defined under ```carmods.dat``` and paintjobs with more than 1 .txd files (eg. ```jester1.txd```, ```jester2.txd```, ```jester3.txd```, ```jester4.txd```). If you have modded your game and replaced cars that have paintjobs/tuning parts, there's a chance to see them with either or both.

## Contributions

Contributions are welcome! Please create a new branch and make a pull request to the main branch so that I'll review the changes and approve it if it's beneficial enough.

## Credits

- To Rockstar North for making a wonderful game.
- To Seemann for creating the CLEO library.
- To LINK/2012 for creating the modloader.
- To JuniorDjjr for creating the original [NPC Tuning mod](https://www.mixmods.com.br/2020/11/npc-tuning-trafego-com-carros-tunados/) and the CLEO+ library.
