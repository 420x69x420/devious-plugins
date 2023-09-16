# devious-plugins
Plugins I make for Devious Client

1. **Fight Cave** 99.9% Credit goes to xKylee for the plugin <https://github.com/xKylee/plugins-source/blob/master/fightcave>, I just found the trigger in the code to flick prayer and added code to enable prayers with Devious API - also it drinks pots (sara brew, super restore, ranged pots)

2. **Phosani's nightmare** Credit also goes to xKylee for base structure of plugin which I added onto: <https://github.com/xKylee/plugins-source/tree/master/nightmare>. Kills Phosani's nightmare after you prepare the account with levels, gear, supplies. Currently requiring ectophial / ring of dueling / sanfew serum / prayer potion (optional) / anglerfish, but can set custom gear swaps in config. Script goes to ferox enclave to regear so start there, and it will do the rest until you run out of supplies. If you take damage during any point in the fight except parasite spawn (5hp) and final phase constant 15-dmg, make issue on this github project and I will look at it

Not-To-Do-Yet List for Phosani's Nightmare:
-Consider ammo/gear degradation for needing to not rekill
-Buy more supplies
-Put more ammo in things like toxic blowpipe and powered staffs

**Basic P2P runecrafting** (fire altar). Will maybe add air altar later and more fun things to this one, but found <https://github.com/maikel233/devious-plugins/tree/main/xRunecrafting> and found it was really bad plugin, so took some stuff from it and made new script. Requires rings of dueling, and optionally stamina potion(1), and pure essense and fire tiara, and will make fire runes.


# Events, Muling, Extended API, cool stuff

Sometime I will write an explanation of these things, because I can predict the future. Trust me. They can help with scripting. For now just know that if you want to build the 420xplugins from source, run jar task for the specific plugin and the API will be packaged in same final .jar via shadowjar to use commonly used methods.

PS sry Burak wouldn't let me dev for Storm Client so :c 
