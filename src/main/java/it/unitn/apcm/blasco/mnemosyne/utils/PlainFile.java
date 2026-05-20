package it.unitn.apcm.blasco.mnemosyne.utils;

import java.util.Arrays;

public record PlainFile(String name, byte[] content) implements AutoCloseable {

    @Override
    public void close() {
        Arrays.fill(this.content, (byte) 0);
    }
}
