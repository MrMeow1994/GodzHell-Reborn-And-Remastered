import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class VarBitDefinitions {

    private static final Logger log = LoggerFactory.getLogger(VarBitDefinitions.class);

    public static final void unpackConfig() {
        try {
            stream = new stream(FileUtils.readFileToByteArray(new File("./data/configs/varbit.dat")));
            totalvarbit = stream.readUnsignedWord();
            if (cache == null)
                cache = new VarBitDefinitions[totalvarbit];
            for(int j = 0; j < totalvarbit; j++)
            {
                if (cache[j] == null)
                    cache[j] = new VarBitDefinitions();
                cache[j].readValues(stream);
            }
            if (stream.currentOffset != stream.buffer.length) {
                log.error("WARNING: varbit load mismatch");
            }
            log.info("Successfully loaded: {} Varbit definitions.", totalvarbit);
        } catch (Exception e) {
            log.error("Error: An error has occured whilst loading Varbit definitions!", e);
        }
    }

    private void readValues(stream stream) {
        int opcode = stream.readUnsignedByte();
        if (opcode == 0) {
            return;
        } else if (opcode == 1) {
            configID = stream.readUnsignedWord();
            lsb = stream.readUnsignedByte();
            msb = stream.readUnsignedByte();
        } else {
            log.error("Error: Invalid varbit opcode: {}", opcode);
        }
    }

    private static VarBitDefinitions cache[];
    public int configID;
    public int lsb;
    public int msb;
    private static stream stream;
    public static int totalvarbit;
    private static int[] values;

    private static final int[] MASK_LOOKUP = new int[32];

    static {
        int i = 2;
        for (int i2 = 0; i2 < 32; i2++) {
            MASK_LOOKUP[i2] = i - 1;
            i <<= 1;
        }
        values = new int[5000];
    }
    public int getBaseVar() {
        return configID;
    }

    public int getStartBit() {
        return lsb;
    }

    public int getEndBit() {
        return msb;
    }

    public static VarBitDefinitions get(final int id) {
        if (id < 0 || id >= cache.length || cache[id] == null) {
            return null;
        }
        return cache[id];
    }

    public static int getBitValue(final int id) {
        final VarBitDefinitions defs = get(id);
        if (defs == null) {
            return 0;
        }
        int baseVarValue = values[defs.getBaseVar()]; // Assuming values is accessible here
        int numberOfBits = defs.getEndBit() - defs.getStartBit();
        int mask = MASK_LOOKUP[numberOfBits]; // Ensure MASK_LOOKUP is correctly initialized
        return (baseVarValue >> defs.getStartBit()) & mask;
    }
    public int setBit(final int id, int value) {
        if (id == -1) {
            return 0;
        }
        final VarBitDefinitions defs = VarBitDefinitions.get(id);
        if (defs == null) {
            return  0;
        }
        int mask = MASK_LOOKUP[defs.getEndBit() - defs.getStartBit()];
        if (value < 0 || value > mask) {
            value = 0;
        }
        mask <<= defs.getStartBit();
        final int varpValue = (values[defs.getBaseVar()] & (~mask) | value << defs.getStartBit() & mask);
        return varpValue;
    }
}
