import java.awt.Color;
import java.io.BufferedWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.everythingrs.lib.gson.JsonArray;
import com.everythingrs.lib.gson.JsonObject;
import org.apache.commons.lang3.RandomUtils;

public class ColorManager {

    public static final int RECOLOR_PICKER = 625, COMPLETIONIST_INTERFACE = 21503;

    public static final int[] DEFAULT_COLOURS = {0x0, 0x0, 0x0, 0x0}, COMPLETIONIST_DEFAULT_COLOURS = {0xfebe, 0xfeb0, 0xfea2, 0xf613},
            COLOR_POSITIONS = {21518, 21523, 21533, 21528};

    public static boolean COLOR_SWITCHING_ENABLED = false;

    private static final int[] RAINBOW = {
            convertRBG(148, 0, 211),
            convertRBG(75, 0, 130),
            convertRBG(0, 0, 255),
            convertRBG(0, 255, 0),
            convertRBG(255, 255, 0),
            convertRBG(255, 127, 0),
            convertRBG(255, 0, 0)
    };

    private final Player player;
    private Map<Integer, int[]> items;

    public ColorManager(Player player) {
        this.player = player;
        this.items = new HashMap<>();
    }

    public static void init() {

        CycleEventHandler.getSingleton().addEvent(null, new CycleEvent() {
            @Override
            public void execute(CycleEventContainer container) {
                if (COLOR_SWITCHING_ENABLED) {
                    try {

                        Player[] players = PlayerHandler.players;
                        for (int index = players.length - 1; index >= 0; index--) {
                            Player player = players[index];
                            if (player == null)
                                continue;
                            int[] color = new int[]{RAINBOW[RandomUtils.nextInt(0, RAINBOW.length)], 0, 0, 0};
                            player.getColorManager().recolor(50405, color);
                            player.getColorManager().recolor(31021, color);
                            player.getColorManager().recolor(31022, color);
                            player.getColorManager().recolor(31023, color);
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }, 2 * 30);
    }

    public static int convertRBG(int red, int green, int blue) {
        float[] HSB = Color.RGBtoHSB(red, green, blue, null);
        float hue = (HSB[0]);
        float saturation = (HSB[1]);
        float brightness = (HSB[2]);
        int encode_hue = (int) (hue * 63);            //to 6-bits
        int encode_saturation = (int) (saturation * 7);        //to 3-bits
        int encode_brightness = (int) (brightness * 127);    //to 7-bits
        return (encode_hue << 10) + (encode_saturation << 7) + (encode_brightness);
    }

    public void openCompletionist(int item) {
        client client = (client) player;

        client.getAttributes().set("recolour-item", item);
        int[] colours = getColors(item);
        for (int index = 0; index < COLOR_POSITIONS.length; index++) {
            client.frame122(COLOR_POSITIONS[index], colours[index]);
        }

        client.getPA().showInterface(COMPLETIONIST_INTERFACE);
    }

    public void openRecolorPicker(int item, int slot) {
        client client = (client) player;

        client.getAttributes().set("recolour-item", item);
        client.getAttributes().set("recolour-slot", slot);

        client.getPA().showInterface(RECOLOR_PICKER);
    }

    public void recolor(int item, int[] colors) {
        items.put(item, colors);
        player.appearanceUpdateRequired = true;
    }

    public void reset(int item) {
        items.remove(item);
        player.appearanceUpdateRequired = true;
    }

    public int[] getColors(int item) {
        return items.getOrDefault(item, getDefaultColours(item).clone());
    }

    public static int[] getDefaultColours(int item) {
        switch (item) {
            case 20769:
            case 20771:
                return COMPLETIONIST_DEFAULT_COLOURS;
        }

        return DEFAULT_COLOURS;
    }

    public void save(JsonObject json) {
        JsonObject colorBlock = new JsonObject();

        items.forEach((item, colors) -> {
            JsonArray arr = new JsonArray();
            for (int c : colors) {
                arr.add(c);
            }
            colorBlock.add(String.valueOf(item), arr);
        });

        json.add("color_meta", colorBlock);
    }

    public void load(JsonObject json) {
        if (!json.has("color_meta"))
            return;

        JsonObject colorBlock = json.getAsJsonObject("color_meta");

        colorBlock.entrySet().forEach(entry -> {
            int item = Integer.parseInt(entry.getKey());
            JsonArray arr = entry.getValue().getAsJsonArray();

            int[] colors = new int[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                colors[i] = arr.get(i).getAsInt();
            }

            items.put(item, colors);
        });
    }

    public Map<Integer, int[]> getItems() {
        return items;
    }
    public void setColorItems(Map<Integer, int[]> colorItems) {
        this.items = colorItems;
    }

}
