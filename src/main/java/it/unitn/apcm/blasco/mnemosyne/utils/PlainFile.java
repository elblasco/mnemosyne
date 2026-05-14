package it.unitn.apcm.blasco.mnemosyne.utils;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.modes.ChaCha20Poly1305;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;

public record PlainFile(String name, byte[] content) {

    public static PlainFile fromEncryptedFile(EncryptedFile encryptedFile, byte[] key) throws InvalidCipherTextException {
        byte[] ciphertextWithTag = new byte[encryptedFile.ciphertext().length + encryptedFile.tag().length];
        System.arraycopy(encryptedFile.ciphertext(), 0, ciphertextWithTag, 0, encryptedFile.ciphertext().length);
        System.arraycopy(encryptedFile.tag(), 0, ciphertextWithTag, encryptedFile.ciphertext().length, encryptedFile.tag().length);
        ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
        cipher.init(false, new AEADParameters(new KeyParameter(key), 128, encryptedFile.nonce()));

        byte[] output = new byte[cipher.getOutputSize(ciphertextWithTag.length)];
        int len = cipher.processBytes(ciphertextWithTag, 0, ciphertextWithTag.length, output, 0);
        cipher.doFinal(output, len);

        return new PlainFile(encryptedFile.fileName(), output);
    }
}
