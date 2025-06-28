public class PlayerAssistant {
    public final client c;

    public PlayerAssistant(client Client) {
        this.c = Client;
    }

    public void sendFrame171(int state, int componentId) {
        if(c.getOutStream() != null) {
            c.getOutStream().createFrame(171);
            c.getOutStream().writeByte(state);
            c.getOutStream().writeWord(componentId);
            c.flushOutStream();
        }
    }
    public void sendFrame126(String s, int id) {
        if (c.getOutStream() != null) {
            c.getOutStream().createFrameVarSizeWord(126);
            c.getOutStream().writeString(s);
            c.getOutStream().writeWordA(id);
            c.getOutStream().endFrameVarSizeWord();
            c.flushOutStream();
        }
    }
    public void sendConfig(final int settingID, final int value) {
        if (c.getOutStream() != null) {
            if (value < 128) {
                c.getOutStream().createFrame(36);
                c.getOutStream().writeWordBigEndian(settingID);
                c.getOutStream().writeByte(value);
                c.updateRequired = true;
                c.appearanceUpdateRequired = true;
            } else {
                c.getOutStream().createFrame(87);
                c.getOutStream().writeWordBigEndian_dup(settingID);
                c.getOutStream().writeDWord_v1(value);
                c.updateRequired = true;
                c.appearanceUpdateRequired = true;
            }
        }
        c.flushOutStream();
    }
    public void sendFrame248(int MainFrame, int SubFrame) {
        if (c.getOutStream() != null)
            c.getOutStream().createFrame(248);
        c.getOutStream().writeWordA(MainFrame);
        c.getOutStream().writeWord(SubFrame);
        c.flushOutStream();
    }
    public void setSidebarInterface(int menuId, int form) {
        if (c.getOutStream() != null)
        c.getOutStream().createFrame(71);
        c.getOutStream().writeWord(form);
        c.getOutStream().writeByteA(menuId);
    }
    public void resetItems(int WriteFrame) {
        if (c.getOutStream() != null)
        c.getOutStream().createFrameVarSizeWord(53);
        c.getOutStream().writeWord(WriteFrame);
        c.getOutStream().writeWord(c.playerItems.length);
        for (int i = 0; i < c.playerItems.length; i++) {
            if (c.playerItemsN[i] > 254) {
                c.getOutStream().writeByte(255); // item's stack count. if over 254, write byte 255
                c.getOutStream().writeDWord_v2(c.playerItemsN[i]); // and then the real value with writeDWord_v2
            } else {
                c.getOutStream().writeByte(c.playerItemsN[i]);
            }
            if (c.playerItems[i] > Config.MAX_ITEMS || c.playerItems[i] < 0) {
                c.playerItems[i] = Config.MAX_ITEMS;
            }
            c.getOutStream().writeWordBigEndianA(c.playerItems[i]); // item id
        }
        c.getOutStream().endFrameVarSizeWord();
    }
    public void openUpBank() {
        if (c.getOutStream() != null) {
            resetItems(5064);
            c.rearrangeBank();
            c.resetBank();
            //resetTempItems();
            c.getOutStream().createFrame(248);
            c.getOutStream().writeWordA(5292);
            c.getOutStream().writeWord(5063);
            c.flushOutStream();
            c.InBank = 1;
        }

    }

    public void openUpBank2() {
        if (c.getOutStream() != null) {
            resetItems(5064);
            c.rearrangeBank2();
            c.resetBank2();
            //resetTempItems();
            c.getOutStream().createFrame(248);
            c.getOutStream().writeWordA(5292);
            c.getOutStream().writeWord(5063);
            c.flushOutStream();
            c.InBank = 2;
        }

    }

    public void openUpBank3() {
        if (c.getOutStream() != null) {
            resetItems(5064);
            c.rearrangeBank3();
            c.resetBank3();
            //resetTempItems();
            c.getOutStream().createFrame(248);
            c.getOutStream().writeWordA(5292);
            c.getOutStream().writeWord(5063);
            c.flushOutStream();
            c.InBank = 3;
        }

    }
}
