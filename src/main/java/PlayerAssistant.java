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
    public void sendQuest(String s, int id) {
        if (c.getOutStream() != null)
            c.getOutStream().createFrameVarSizeWord(126);
        c.getOutStream().writeString(s);
        c.getOutStream().writeWordA(id);
        c.getOutStream().endFrameVarSizeWord();
    }
    public void randomize(int o, int oo, int ooo, int oooo) {
        if (c.getOutStream() != null)
            c.getOutStream().createFrame(53);
        c.getOutStream().writeWord(o);
        c.getOutStream().writeWord(oo);
        c.getOutStream().writeByte(ooo);
        c.getOutStream().writeWordBigEndianA(oooo);
        c.flushOutStream();
    }

    public void sendFrame34(int id, int slot, int column, int amount) {
        if (c.getOutStream() != null) {
            c.getOutStream().createFrameVarSizeWord(34);
            c.getOutStream().writeWord(column);
            c.getOutStream().writeByte(4);
            c.getOutStream().writeDWord(slot);
            c.getOutStream().writeWord(id + 1);
            if (amount > 254) {
                c.getOutStream().writeByte(255);
                c.getOutStream().writeDWord(amount);
            } else {
                c.getOutStream().writeByte(amount);
            }
            c.getOutStream().endFrameVarSizeWord();
            c.flushOutStream();
        }
    }



    public void sendFrame200(int MainFrame, int SubFrame) {
        if (c.getOutStream() != null)
            c.getOutStream().createFrame(200);
        c.getOutStream().writeWord(MainFrame);
        c.getOutStream().writeWord(SubFrame);
        c.flushOutStream();
    }

    public void sendFrame75(int MainFrame, int SubFrame) {
        if (c.getOutStream() != null)
            c.getOutStream().createFrame(75);
        c.getOutStream().writeWordBigEndianA(MainFrame);
        c.getOutStream().writeWordBigEndianA(SubFrame);
        c.flushOutStream();
    }

    public void sendFrame164(int Frame) {
        if (c.getOutStream() != null)
            c.getOutStream().createFrame(164);
        c.getOutStream().writeWordBigEndian_dup(Frame);
        c.flushOutStream();
    }

    public void sendFrame246(int MainFrame, int SubFrame, int SubFrame2) {
        if (c.getOutStream() != null)
            c.getOutStream().createFrame(246);
        c.getOutStream().writeWordBigEndian(MainFrame);
        c.getOutStream().writeWord(SubFrame);
        c.getOutStream().writeWord(SubFrame2);
        c.flushOutStream();
    }

    public void sendFrame185(int Frame) {
        if (c.getOutStream() != null)
            c.getOutStream().createFrame(185);
        c.getOutStream().writeWordBigEndianA(Frame);
        c.flushOutStream();
    }

    public void sendInterfaceHidden (int MainFrame, int SubFrame) {
        if (c.getOutStream() != null)
            c.getOutStream().createFrame(171);
        c.getOutStream().writeByte(MainFrame);
        c.getOutStream().writeWord(SubFrame);
        c.flushOutStream();
    }

    public void RemoveAllWindows() {
        if (c.getOutStream() != null)
            c.resetVariables();
        c.getOutStream().createFrame(219);
        c.flushOutStream();
    }

    public void sendQuestSomething(int id) {
        if (c.getOutStream() != null)
            c.getOutStream().createFrame(79);
        c.getOutStream().writeWordBigEndian(id);
        c.getOutStream().writeWordA(0);
        c.flushOutStream();
    }
    public void showInterface(int interfaceid) {
        if (c.getOutStream() != null)
            c.resetAnimation();
        c.getOutStream().createFrame(97);
        c.getOutStream().writeUnsignedWord(interfaceid);
        c.flushOutStream();
    }
    public void setChatOptions(int publicChat, int privateChat, int tradeBlock) {
        if (c.getOutStream() != null)
            c.getOutStream().createFrame(206);
        c.getOutStream().writeByte(publicChat); // On = 0, Friends = 1, Off = 2, Hide = 3
        c. getOutStream().writeByte(privateChat); // On = 0, Friends = 1, Off = 2
        c.getOutStream().writeByte(tradeBlock); // On = 0, Friends = 1, Off = 2
    }
    public void fsBar(int id1, int id2, int id3) {
        if(c.getOutStream() != null) {
            c.getOutStream().createFrame(70);
            c.getOutStream().writeWord(id1);
            c.getOutStream().writeWordBigEndian(id2);
            c.getOutStream().writeWordBigEndian(id3);
        }
    }

    public void sendFrame230(int i1, int i2, int i3, int i4) { // i2 being negative logs you out, otherwise it doesn't log you out :O
        if(c.getOutStream() != null)
            c.getOutStream().createFrame(230);
        c.getOutStream().writeWordA(i1);
        c.getOutStream().writeWord(i2); // interface id?
        c.getOutStream().writeWord(i3);
        c.getOutStream().writeWordBigEndianA(i4); // junk? not sure
        c.updateRequired = true;
        c.appearanceUpdateRequired = true;
    }
    public void pmstatus(int status) { // status: loading = 0  connecting = 1  fine = 2
        if(c.getOutStream() != null) {
            c.getOutStream().createFrame(221);
            c.getOutStream().writeByte(status);
        }
    }

}
