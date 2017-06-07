package com.github.rmannibucau.envkey.impl;

import com.github.rmannibucau.envkey.spi.OpenGPG;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static java.util.Optional.ofNullable;

public class BouncyCastleOpenGPG implements OpenGPG {
    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Override
    public String decrypt(final String env, final String privKey, final String pwd) {
        try (final InputStream stream = new ByteArrayInputStream(env.getBytes(StandardCharsets.UTF_8))) {
            final InputStream pgpStream = PGPUtil.getDecoderStream(stream);
            final PGPObjectFactory pgpObjectFactory = new PGPObjectFactory(pgpStream);
            final Object next = pgpObjectFactory.nextObject();
            final PGPEncryptedDataList pgpEncryptedDataList = PGPEncryptedDataList.class.cast(PGPEncryptedDataList.class.isInstance(next) ? next : pgpObjectFactory.nextObject());
            final Iterator<PGPPublicKeyEncryptedData> encryptedDataObjects = pgpEncryptedDataList.getEncryptedDataObjects();
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(encryptedDataObjects, Spliterator.IMMUTABLE), false)
                    .map(ed -> {
                        try (final InputStream pk = new ByteArrayInputStream(privKey.getBytes(StandardCharsets.UTF_8))) {
                            final PGPSecretKeyRingCollection keyRingCollection = new PGPSecretKeyRingCollection(
                                    PGPUtil.getDecoderStream(pk));
                            return ofNullable(keyRingCollection.getSecretKey(ed.getKeyID())).map(secretKey -> {
                                try {
                                    return secretKey.extractPrivateKey(pwd.toCharArray(), "BC");
                                } catch (PGPException | NoSuchProviderException e) {
                                    throw new IllegalStateException(e);
                                }
                            }).map(s -> {
                                try {
                                    return ed.getDataStream(s, "BC");
                                } catch (PGPException | NoSuchProviderException e) {
                                    throw new IllegalStateException(e);
                                }
                            }).map(clearStream -> {
                                try {
                                    final PGPObjectFactory clearObjectFactory = new PGPObjectFactory(clearStream);
                                    Object message = clearObjectFactory.nextObject();
                                    if (PGPCompressedData.class.isInstance(message)) {
                                        final PGPCompressedData cData = PGPCompressedData.class.cast(message);
                                        final PGPObjectFactory pgpFact = new PGPObjectFactory(cData.getDataStream());
                                        message = pgpFact.nextObject();
                                    }

                                    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    if (!PGPLiteralData.class.isInstance(message)) {
                                        throw new PGPException("Message is not a simple encrypted file - type unknown.");
                                    }

                                    final InputStream s = PGPLiteralData.class.cast(message).getInputStream();
                                    int b;
                                    while ((b = s.read()) >= 0) {
                                        byteArrayOutputStream.write(b);
                                    }
                                    return new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
                                } catch (final PGPException | IOException ioe) {
                                    throw new IllegalStateException(ioe);
                                }
                            }).map(result -> {
                                if (ed.isIntegrityProtected()) {
                                    try {
                                        if (!ed.verify()) {
                                            throw new IllegalStateException("Verify failed");
                                        }
                                    } catch (PGPException | IOException e) {
                                        throw new IllegalStateException(e);
                                    }
                                }
                                return result;
                            }).orElse(null);
                        } catch (final IOException | PGPException e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No secret key found"));
        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }
}
