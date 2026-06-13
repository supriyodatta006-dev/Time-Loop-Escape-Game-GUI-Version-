package game.levels;

import game.core.Difficulty;
import game.entities.Interactable;
import game.entities.Interactable.InteractableType;
import game.entities.Item;
import game.entities.Item.ItemType;
import game.entities.Room;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class LevelFactory {

    private static final int COLS = 12;
    private static final int ROWS = 9;
    private static final int TILE = 64;

    private LevelFactory() {}

    public static List<Room> generateRooms(Difficulty diff) {
        List<Room> list = new ArrayList<>();
        list.add(buildLibrary(diff));
        list.add(buildClocktower(diff));
        list.add(buildLab(diff));

        list.add(buildCrypt(diff));
        list.add(buildObservatory(diff));
        list.add(buildSubmarine(diff));
        list.add(buildNexus(diff));

        return list;
    }

    private static Room buildLibrary(Difficulty diff) {
        Room r = new Room("library", "The Forgotten Library", COLS, ROWS, TILE);
        r.setTheme(new Color(0x1B, 0x26, 0x39), new Color(0x0D, 0x1B, 0x2A), new Color(0x00, 0xBC, 0xD4));

        for (int c = 2; c <= 4;  c++) r.setTile(c, 2, Room.TILE_WALL);
        for (int c = 8; c <= 10; c++) r.setTile(c, 2, Room.TILE_WALL);

        for (int row = 4; row <= 6; row++) {
            r.setTile(2, row, Room.TILE_WALL);
            r.setTile(9, row, Room.TILE_WALL);
        }

        r.setTile(5, 5, Room.TILE_PUZZLE);

        r.addItem(new Item("clue_torn_page", "Torn Page",
                "A page from a clock manual. Numbers circled.", ItemType.CLUE, 4, 3));
        r.addItem(new Item("clue_ink_stamp", "Ink Stamp",
                "Serpent eating its tail.", ItemType.CLUE, 8, 6));
        r.addItem(new Item("key_bronze_key", "Bronze Key",
                "Engraved: 'KEEP'.", ItemType.KEY_ITEM, 4, 7));
        if (diff.redHerringCount >= 1)
            r.addItem(new Item("herring_book", "Dusty Book",
                    "Blank inside.", ItemType.RED_HERRING, 9, 3));

        r.addInteractable(new Interactable("ia_shelf", "Bookshelf", "Search shelves",
                InteractableType.BOOKSHELF, 3, 1, null,
                ia -> ia.showFloating("A hidden symbol — eye of the serpent!")));

        r.addInteractable(new Interactable("ia_chest", "Locked Chest", "Needs Bronze Key",
                InteractableType.CHEST, 8, 7, "key_bronze_key",
                ia -> ia.showFloating("Chest open — an ancient rune inside!")));

        r.addInteractable(new Interactable("ia_panel", "Puzzle Panel", "Enter the sequence",
                InteractableType.PUZZLE_PANEL, 5, 5,
                diff == Difficulty.EASY ? null : "clue_torn_page",
                ia -> {
                    ia.showFloating("Sequence accepted! The door unseals.");
                    r.unlockExit();
                }));

        return r;
    }

    private static Room buildClocktower(Difficulty diff) {
        Room r = new Room("clocktower", "The Clocktower Chamber", COLS, ROWS, TILE);
        r.setTheme(new Color(0x1C, 0x10, 0x07), new Color(0x3E, 0x27, 0x23), new Color(0xFF, 0xB3, 0x00));

        r.setTile(2, 2, Room.TILE_WALL); r.setTile(9, 2, Room.TILE_WALL);
        r.setTile(2, 3, Room.TILE_WALL); r.setTile(9, 3, Room.TILE_WALL);

        r.addItem(new Item("clue_clock_hand", "Clock Hand",
                "Broken hour hand. Points to 8.", ItemType.CLUE, 5, 4));
        r.addItem(new Item("clue_engraving", "Brass Engraving",
                "'The loop breaks when time speaks backwards.'", ItemType.CLUE, 9, 6));
        r.addItem(new Item("key_cog", "Gear Cog",
                "Fits the clock mechanism perfectly.", ItemType.KEY_ITEM, 3, 6));
        if (diff.redHerringCount >= 2)
            r.addItem(new Item("herring_watch", "Broken Watch",
                    "Stopped at midnight.", ItemType.RED_HERRING, 8, 7));
        if (diff.redHerringCount >= 3)
            r.addItem(new Item("herring_rag", "Oil Rag",
                    "Greasy and useless.", ItemType.RED_HERRING, 10, 5));

        r.addInteractable(new Interactable("ia_clock", "Grand Clock", "Insert Gear Cog",
                InteractableType.CLOCK, 6, 5, "key_cog",
                ia -> {
                    ia.showFloating("TICK... TOCK... The clock chimes!");
                    r.unlockExit();
                }));

        r.addInteractable(new Interactable("ia_mirror", "Old Mirror", "Reflection delayed.",
                InteractableType.MIRROR, 1, 5, null,
                ia -> ia.showFloating("'Look behind the fourth hour.'")));

        return r;
    }

    private static Room buildLab(Difficulty diff) {
        Room r = new Room("lab", "Temporal Laboratory", COLS, ROWS, TILE);
        r.setTheme(new Color(0x0A, 0x16, 0x28), new Color(0x00, 0x1F, 0x3D), new Color(0x7C, 0x4D, 0xFF));

        for (int c = 3; c <= 5; c++) r.setTile(c, 3, Room.TILE_WALL);
        for (int c = 7; c <= 9; c++) r.setTile(c, 3, Room.TILE_WALL);

        for (int c = 7; c <= 9; c++) r.setTile(c, 7, Room.TILE_WALL);

        r.setTile(5, 4, Room.TILE_PUZZLE);
        r.setTile(6, 4, Room.TILE_PUZZLE);
        r.setTile(7, 4, Room.TILE_PUZZLE);

        r.addItem(new Item("clue_formula", "Blackboard Formula",
                "E=MC\u00B2 but one variable is \u221E.", ItemType.CLUE, 4, 5));
        r.addItem(new Item("clue_log", "Research Log",
                "'Loop #47. Subject shows awareness.'", ItemType.CLUE, 8, 2));
        r.addItem(new Item("key_card", "Access Card",
                "Clearance: OMEGA", ItemType.KEY_ITEM, 3, 7));
        if (diff.redHerringCount >= 4)
            r.addItem(new Item("herring_vial", "Empty Vial",
                    "Useless residue.", ItemType.RED_HERRING, 10, 2));
        if (diff.redHerringCount >= 5)
            r.addItem(new Item("herring_goggles", "Cracked Goggles",
                    "Nothing special.", ItemType.RED_HERRING, 5, 6));

        r.addInteractable(new Interactable("ia_terminal", "OMEGA Terminal", "Requires Access Card",
                InteractableType.COMPUTER_TERMINAL, 6, 4, "key_card",
                ia -> ia.showFloating("LOOP OVERRIDE INITIATED...")));

        r.addInteractable(new Interactable("ia_exit_panel", "Exit Sequence", "Input the codes",
                InteractableType.PUZZLE_PANEL, 6, 1,
                diff == Difficulty.NIGHTMARE ? "key_card" : null,
                ia -> {
                    ia.showFloating("TEMPORAL LOCK DISENGAGED!");
                    r.unlockExit();
                }));

        return r;
    }

    private static Room buildCrypt(Difficulty diff) {
        Room r = new Room("crypt", "The Ancient Crypt", COLS, ROWS, TILE);
        r.setTheme(new Color(0x12, 0x0A, 0x00), new Color(0x1E, 0x0D, 0x00), new Color(0xB8, 0x46, 0x00));

        r.setTile(1, 2, Room.TILE_WALL);  r.setTile(10, 2, Room.TILE_WALL);
        r.setTile(1, 7, Room.TILE_WALL);  r.setTile(10, 7, Room.TILE_WALL);

        r.setTile(3, 3, Room.TILE_WALL);  r.setTile(8, 3, Room.TILE_WALL);
        r.setTile(3, 4, Room.TILE_WALL);  r.setTile(8, 4, Room.TILE_WALL);

        r.setTile(6, 6, Room.TILE_PUZZLE);

        r.addItem(new Item("clue_rune_tablet", "Rune Tablet",
                "Strange symbols: \u25B3\u25CB\u25A1\u25B3 — a sequence.", ItemType.CLUE, 4, 5));
        r.addItem(new Item("clue_skull_scroll", "Skull Scroll",
                "'The dead remember what the living forget.'", ItemType.CLUE, 7, 2));
        r.addItem(new Item("key_skull_key", "Skull Key",
                "Carved from bone. Cold to the touch.", ItemType.KEY_ITEM, 9, 6));
        if (diff.redHerringCount >= 2)
            r.addItem(new Item("herring_torch", "Dead Torch",
                    "Won't light. Still smells of ash.", ItemType.RED_HERRING, 2, 6));
        if (diff.redHerringCount >= 4)
            r.addItem(new Item("herring_coin", "Ancient Coin",
                    "Worthless. Head of an unknown emperor.", ItemType.RED_HERRING, 10, 5));

        r.addInteractable(new Interactable("ia_sarcophagus", "Sarcophagus", "Examine the coffin",
                InteractableType.CHEST, 2, 1, null,
                ia -> ia.showFloating("'Loop #1 was not the first. Not even close.'")));

        r.addInteractable(new Interactable("ia_crypt_mirror", "Cracked Mirror", "Shattered reflection",
                InteractableType.MIRROR, 9, 3, null,
                ia -> ia.showFloating("You see yourself — from a loop ago.")));

        r.addInteractable(new Interactable("ia_altar", "Blood Altar", "Place the Skull Key",
                InteractableType.PUZZLE_PANEL, 6, 6, "key_skull_key",
                ia -> {
                    ia.showFloating("The altar glows crimson — path unsealed!");
                    r.unlockExit();
                }));

        return r;
    }

    private static Room buildObservatory(Difficulty diff) {
        Room r = new Room("observatory", "The Celestial Observatory", COLS, ROWS, TILE);
        r.setTheme(new Color(0x02, 0x02, 0x1A), new Color(0x04, 0x04, 0x35), new Color(0x00, 0xE5, 0xFF));

        r.setTile(2, 2, Room.TILE_WALL);  r.setTile(3, 2, Room.TILE_WALL);
        r.setTile(8, 2, Room.TILE_WALL);  r.setTile(9, 2, Room.TILE_WALL);

        r.setTile(2, 6, Room.TILE_WALL);  r.setTile(3, 6, Room.TILE_WALL);
        r.setTile(8, 6, Room.TILE_WALL);  r.setTile(9, 6, Room.TILE_WALL);

        r.setTile(4, 4, Room.TILE_PUZZLE);
        r.setTile(5, 4, Room.TILE_PUZZLE);

        r.addItem(new Item("clue_star_map", "Star Map",
                "Constellation 'Ouroboros' circled in red ink.", ItemType.CLUE, 4, 3));
        r.addItem(new Item("clue_comet_shard", "Comet Shard",
                "Glows faintly. Warm to the touch. Not from here.", ItemType.CLUE, 8, 5));
        r.addItem(new Item("key_crystal_lens", "Crystal Lens",
                "Focuses temporal light into a single point.", ItemType.KEY_ITEM, 4, 7));
        if (diff.redHerringCount >= 3)
            r.addItem(new Item("herring_asteroid", "Space Rock",
                    "Just a rock. Smells like nothing.", ItemType.RED_HERRING, 10, 7));
        if (diff.redHerringCount >= 5)
            r.addItem(new Item("herring_wrong_map", "Wrong Map",
                    "Charted the wrong galaxy entirely.", ItemType.RED_HERRING, 1, 4));

        r.addInteractable(new Interactable("ia_star_chart", "Star Charts", "Browse the charts",
                InteractableType.BOOKSHELF, 1, 1, null,
                ia -> ia.showFloating("'Polaris was never north here.'")));

        r.addInteractable(new Interactable("ia_telescope", "Grand Telescope", "Look through",
                InteractableType.MIRROR, 9, 1, null,
                ia -> ia.showFloating("'The loop is a closed orbit around a dead star.'")));

        r.addInteractable(new Interactable("ia_orrery", "Temporal Orrery", "Insert Crystal Lens",
                InteractableType.CLOCK, 3, 4, "key_crystal_lens",
                ia -> {
                    ia.showFloating("The planets align — the exit blazes open!");
                    r.unlockExit();
                }));

        return r;
    }

    private static Room buildSubmarine(Difficulty diff) {
        Room r = new Room("submarine", "Submarine Control Room", COLS, ROWS, TILE);
        r.setTheme(new Color(0x00, 0x12, 0x1A), new Color(0x00, 0x22, 0x30), new Color(0x00, 0xFF, 0xA0));

        r.setTile(1, 2, Room.TILE_WALL);  r.setTile(2, 2, Room.TILE_WALL);
        r.setTile(9, 2, Room.TILE_WALL);  r.setTile(10, 2, Room.TILE_WALL);

        r.setTile(1, 6, Room.TILE_WALL);  r.setTile(2, 6, Room.TILE_WALL);
        r.setTile(9, 6, Room.TILE_WALL);  r.setTile(10, 6, Room.TILE_WALL);

        r.setTile(5, 4, Room.TILE_PUZZLE);
        r.setTile(6, 4, Room.TILE_PUZZLE);
        r.setTile(7, 4, Room.TILE_PUZZLE);

        r.addItem(new Item("clue_pressure_gauge", "Pressure Gauge",
                "Reading: 47 atmospheres. Deeply unusual.", ItemType.CLUE, 3, 3));
        r.addItem(new Item("clue_sonar_log", "Sonar Log",
                "'Signal repeats every 47 seconds. No source found.'", ItemType.CLUE, 7, 5));
        r.addItem(new Item("key_emergency_key", "Emergency Key",
                "Red. Marked: SURFACE OVERRIDE.", ItemType.KEY_ITEM, 4, 7));
        if (diff.redHerringCount >= 2)
            r.addItem(new Item("herring_oxygen_tank", "Empty O2 Tank",
                    "Bone dry. Not helpful.", ItemType.RED_HERRING, 10, 3));
        if (diff.redHerringCount >= 4)
            r.addItem(new Item("herring_nav_chart", "Navigation Chart",
                    "All routes lead back to here.", ItemType.RED_HERRING, 1, 5));

        r.addInteractable(new Interactable("ia_periscope", "Periscope", "Look through",
                InteractableType.MIRROR, 9, 1, null,
                ia -> ia.showFloating("'We're 300 meters below 1987.'")));

        r.addInteractable(new Interactable("ia_radio", "Radio Console", "Listen in",
                InteractableType.COMPUTER_TERMINAL, 2, 4, null,
                ia -> ia.showFloating("Static... then: '...help us... loop 47...'")));

        r.addInteractable(new Interactable("ia_control_panel", "SURFACE OVERRIDE", "Insert Emergency Key",
                InteractableType.COMPUTER_TERMINAL, 6, 4, "key_emergency_key",
                ia -> {
                    ia.showFloating("SURFACING — TEMPORAL LOCK DISENGAGED!");
                    r.unlockExit();
                }));

        return r;
    }

    private static Room buildNexus(Difficulty diff) {
        Room r = new Room("nexus", "The Temporal Nexus", COLS, ROWS, TILE);
        r.setTheme(new Color(0x05, 0x00, 0x15), new Color(0x0F, 0x00, 0x30), new Color(0xFF, 0x00, 0xFF));

        r.setTile(2, 2, Room.TILE_WALL);  r.setTile(9, 2, Room.TILE_WALL);
        r.setTile(2, 3, Room.TILE_WALL);  r.setTile(9, 3, Room.TILE_WALL);
        r.setTile(2, 6, Room.TILE_WALL);  r.setTile(9, 6, Room.TILE_WALL);
        r.setTile(2, 7, Room.TILE_WALL);  r.setTile(9, 7, Room.TILE_WALL);

        r.setTile(5, 1, Room.TILE_PUZZLE);
        r.setTile(6, 1, Room.TILE_PUZZLE);
        r.setTile(7, 1, Room.TILE_PUZZLE);
        r.setTile(5, 4, Room.TILE_PUZZLE);
        r.setTile(6, 4, Room.TILE_PUZZLE);
        r.setTile(7, 4, Room.TILE_PUZZLE);

        r.addItem(new Item("clue_void_fragment", "Void Fragment",
                "'The answer was always 47. The loop ends at 47.'", ItemType.CLUE, 5, 4));
        r.addItem(new Item("clue_echo_log", "Echo Log",
                "'Subject has completed all loops. Final sequence required.'", ItemType.CLUE, 8, 1));
        r.addItem(new Item("key_omega_shard", "Omega Shard",
                "Pulsates with temporal energy. This is it.", ItemType.KEY_ITEM, 4, 5));
        if (diff.redHerringCount >= 3)
            r.addItem(new Item("herring_paradox", "Paradox Note",
                    "Contradicts itself in every sentence.", ItemType.RED_HERRING, 10, 4));
        if (diff.redHerringCount >= 6)
            r.addItem(new Item("herring_fake_shard", "False Shard",
                    "A decoy. Shatters immediately.", ItemType.RED_HERRING, 7, 7));

        r.addInteractable(new Interactable("ia_echo_device", "Echo Device", "Replay the loops",
                InteractableType.COMPUTER_TERMINAL, 9, 5, null,
                ia -> ia.showFloating("You hear yourself: 'Don't give up. Almost there.'")));

        r.addInteractable(new Interactable("ia_nexus_mirror", "Spectral Mirror", "Gaze within",
                InteractableType.MIRROR, 1, 4, null,
                ia -> ia.showFloating("'This was always going to happen. You were always going to win.'")));

        r.addInteractable(new Interactable("ia_nexus_core", "NEXUS CORE", "Insert Omega Shard",
                InteractableType.PUZZLE_PANEL, 6, 1,
                diff == Difficulty.NIGHTMARE ? "clue_void_fragment" : "key_omega_shard",
                ia -> {
                    ia.showFloating("THE LOOP IS BROKEN. YOU ARE FREE.");
                    r.unlockExit();
                }));

        return r;
    }
}
