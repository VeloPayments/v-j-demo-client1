package com.velopayments.blockchain.demo;

import com.velopayments.blockchain.cert.*;
import com.velopayments.blockchain.client.RemoteAgentConfiguration;
import com.velopayments.blockchain.client.RemoteAgentConnection;
import com.velopayments.blockchain.crypt.*;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import javax.net.SocketFactory;

public class Demo1 {
    public static void main(String[] args) {
        try {
            /* argument length check. */
            if (args.length < 2) {
                System.out.println("Expecting two arguments:");
                System.out.println("* The client private certificate.");
                System.out.println("* The blockchain agent public certificate.");
                return;
            }

            System.out.println("Reading entity private certificate: " + args[0]);
            Certificate clientPrivate = readCertificateFromFile(args[0]);
            UUID clientId = getArtifactId(clientPrivate);
            EncryptionKeyPair clientEncKeys =
                getEncryptionKeyPair(clientPrivate);

            System.out.println("Reading agent public certificate: " + args[1]);
            Certificate agentPublic = readCertificateFromFile(args[1]);
            UUID agentId = getArtifactId(agentPublic);
            EncryptionPublicKey agentEncPublic =
                getEncryptionPublicKey(agentPublic);

            System.out.println("Initiating agent connection...");

            RemoteAgentConfiguration config =
                new RemoteAgentConfiguration(
                    "localhost", 4931, agentId, null);

            RemoteAgentConnection conn =
                new RemoteAgentConnection(
                    config, SocketFactory.getDefault(),
                    clientId, clientEncKeys.getPrivateKey());

            conn.connect();
            System.out.println("handshake success!");

        } catch (Exception e) {
            System.out.println("Got Exception " + e);
        }
    }

    private static Certificate readCertificateFromFile(String filename)
    throws IOException {
        FileInputStream in = null;
        try {
            File f = new File(filename);
            in = new FileInputStream(f);

            byte[] arr = new byte[(int)f.length()];
            in.read(arr);

            return Certificate.fromByteArray(arr);
        } catch (Throwable e) {
            throw e;
        } finally {
            if (null != in) {
                in.close();
            }
        }
    }

    private static UUID getArtifactId(Certificate cert) {
        CertificateReader reader =
            new CertificateReader(new CertificateParser(cert));

        return reader.getFirst(Field.ARTIFACT_ID).asUUID();
    }

    private static EncryptionPublicKey
    getEncryptionPublicKey(Certificate cert) {
        CertificateReader reader =
            new CertificateReader(new CertificateParser(cert));

        return
            new EncryptionPublicKey(
                reader.getFirst(Field.PUBLIC_ENCRYPTION_KEY).asByteArray());
    }

    private static EncryptionKeyPair
    getEncryptionKeyPair(Certificate cert) {
        CertificateReader reader =
            new CertificateReader(new CertificateParser(cert));

        EncryptionPublicKey pub = 
            new EncryptionPublicKey(
                reader.getFirst(Field.PUBLIC_ENCRYPTION_KEY).asByteArray());
        EncryptionPrivateKey priv = 
            new EncryptionPrivateKey(
                reader.getFirst(Field.PRIVATE_ENCRYPTION_KEY).asByteArray());

        return new EncryptionKeyPair(pub, priv);
    }
}
