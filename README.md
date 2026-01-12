# journeycreative
# About
Journey Creative is a fabric mod that adds an incremental creative inventory to the survival inventory inspired by Journey mode from Terraria.
> Both the server and client need this mod installed to function. It works on multiplayer!  
> You are free to include this mod in your modpack

# Features

- A creative-like inventory accessed directly from within the survival inventory where players can get an unlimited amount of unlocked items.  
- A Terraria like trash can that can be used to delete unwanted items from within the inventory while holding the last deleted item to recover mistaken deletions (reset upon server exit.)  
- Keybinds that allow you to rotate the rows of your inventory in either direction, inspired by Stardew Valley. (Unbound by default - recommended to bind to Tab and Shift-Tab using the [Amecs mod](https://modrinth.com/mod/amecs)).  
- Datapack configurable restrictions on researching items - facilitating a per world config on how many items are necessary to research an item, requiring other items to be researched first, preventing certain items from being researched entirely, and facilitating unlocks through other means (like a modpack quest book).  
- Automatic support for items from other mods, so long as those items are included in the creative inventory (any item that isn't in the creative inventory does not work in the mod. For example, enchanted books that have a lower than max level enchantment will not show up even if they are researched.)

# How to Use

Unlocking items for the creative inventory requires the crafting of two items: Ender Archive and Research Vessel, which require end game materials to craft.  
The Research Vessel is like a shulker box that only takes in one item.  Once you fill it up to its capacity (usually one shulker of that item, but can be configered to less than that), you can put the use the full Research Vessel in the Ender Archive and you'll get a Research Certificate for that item.  
Use the Research Certificate and the item will be unlocked.  

**_Don't want to craft those items to be able to research?_**  
You can change the gamerule researchItemsUnlocked to True and the Research Vessel and Ender Archive will be automatically unlocked in the Journey inventory, bypassing the crafting requirement.  
(the gamerule is journeycreative:research_items_unlocked in 1.21.11)  

# Datapack Configurations 

This mod facilitates configuring the research process by adding a datapack under data/journeycreative/research. [See an example](src/main/resources/data/journeycreative/research)

1. research_amount.json: Changes the amount of items needed to research (this can't go above the natural limit of one shulker box).
2. research_prerequisite.json: Makes it so you can't research an item until you've finished researching another item (e.g. you can't research iron nuggets until you've researched iron ingots).
3. research_prohibited.json: Completely prevents an item from ever being researched / unlocked in the journey inventory.
4. research_certificate_blocked.json: Prevents you from creating a Research Certificate in the Ender Archive, but still allows you to use a Research Certificate to research the item. This isn't used, but is there in case a modpack developer / server owner wants to use some other method to unlock an item instead (e.g. quest book reward.)

## default configs
By default, the following adjustments will be present.  
### Research Amount:
1. All sherds: 16 to unlock
2. All mob heads (except for wither skull): 16 to unlock
3. Sponges and Wet Sponges: 576 (9 stacks) to unlock
4. Nether Star: 576 (9 stacks) to unlock
5. Enchanted Golden Apple: 256 (4 stacks) to unlock
6. Ancient Debris: 1152 (18 stacks) to unlock
7. Netherite Ingot: 320 (5 stacks) to unlock
8. All banner patters: 1 to unlock
9. Heavy Core: 27 to unlock
10. All music discs: 9 to unlock
11. Music Disc Fragment 5: 64 to unlock

### Research Prerequisites:
1. Iron Nugget - Iron Ingot to unlock
2. Gold Nugget - Gold Ingot to unlock
3. Copper Nugget - Copper Ingot to unlock
4. Shulker Box (including dyes) - Shulker Shells to unlock
5. Research Vessel - Shulker Box, Ender Eye, Dragon Breath, and End Crystal to unlock

### Research Prohibited:
1. Air
2. Barrier
3. Research Certificate
