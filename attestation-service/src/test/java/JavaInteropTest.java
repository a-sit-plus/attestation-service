import at.asitplus.attestation.*;
import at.asitplus.attestation.android.AndroidAttestationChecker;
import at.asitplus.attestation.android.AndroidAttestationConfiguration;
import at.asitplus.attestation.android.HardwareAttestationChecker;
import at.asitplus.attestation.android.PatchLevel;
import at.asitplus.attestation.android.exceptions.AttestationException;
import at.asitplus.attestation.android.exceptions.CertificateInvalidException;
import at.asitplus.attestation.android.exceptions.RevocationException;
import com.google.android.attestation.ParsedAttestationRecord;
import org.junit.jupiter.api.Assertions;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.time.Duration;
import java.util.*;


public class JavaInteropTest {


    public static void testDefaults() {
        Assertions.assertThrows(AttestationException.class, () -> {
                    new DefaultAttestationService(
                            new AndroidAttestationConfiguration.Builder(new AndroidAttestationConfiguration.AppData(
                                    "at.asitplus.attestation-example", Collections.emptyList())).build(),
                            new IOSAttestationConfiguration(new IOSAttestationConfiguration.AppData(
                                    "1234567890",
                                    "at.asitplus.attestation-example")));
                },
                "No signature digests specified");

        Assertions.assertThrows(AttestationException.class, () -> {
                    new DefaultAttestationService(
                            new AndroidAttestationConfiguration.Builder(new AndroidAttestationConfiguration.AppData("at.asitplus.attestation-example",
                                    new ArrayList<>()
                            )).build(),
                            new IOSAttestationConfiguration(new IOSAttestationConfiguration.AppData(
                                    "1234567890",
                                    "at.asitplus.attestation-example")),
                            Duration.ZERO);
                },
                "No signature digests specified");

        Assertions.assertThrows(AttestationException.class, () -> {
                    new DefaultAttestationService(
                            new AndroidAttestationConfiguration.Builder(new AndroidAttestationConfiguration.AppData("at.asitplus.attestation-example",
                                    new ArrayList<>(),
                                    10)
                            ).build(),
                            new IOSAttestationConfiguration(new IOSAttestationConfiguration.AppData(
                                    "1234567890",
                                    "at.asitplus.attestation-example")),
                            Duration.ZERO);
                },
                "No signature digests specified");

        Assertions.assertThrows(AttestationException.class, () -> {
                    new DefaultAttestationService(
                            new AndroidAttestationConfiguration.Builder(new AndroidAttestationConfiguration.AppData("at.asitplus.attestation-example",
                                    new ArrayList<>(),
                                    10,
                                    10000)
                            ).build(),
                            new IOSAttestationConfiguration(new IOSAttestationConfiguration.AppData(
                                    "1234567890",
                                    "at.asitplus.attestation-example",
                                    true)),
                            Duration.ZERO);
                },
                "No signature digests specified");

        Assertions.assertThrows(AttestationException.class, () -> {
                    new DefaultAttestationService(
                            new AndroidAttestationConfiguration.Builder(new AndroidAttestationConfiguration.AppData("at.asitplus.attestation-example",
                                    new ArrayList<>()
                            )).build(),
                            new IOSAttestationConfiguration(new IOSAttestationConfiguration.AppData(
                                    "1234567890",
                                    "at.asitplus.attestation-example",
                                    false),
                                    "14.1"),
                            Duration.ZERO);
                },
                "No signature digests specified");
    }

    public static void testAttestationCallsJavaFriendliness() throws NoSuchAlgorithmException {
        AttestationService service = new DefaultAttestationService(
                new AndroidAttestationConfiguration.Builder(new AndroidAttestationConfiguration.AppData("at.asitplus.attestation-example",
                        Arrays.asList(new byte[][]{new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8}}))
                ).build(),
                new IOSAttestationConfiguration(new IOSAttestationConfiguration.AppData(
                        "1234567890",
                        "at.asitplus.attestation-example",
                        false),
                        "14.1"),
                Duration.ZERO);

        AttestationResult result = service.verifyAttestation(Collections.emptyList(), new byte[]{}, null);


        if (result instanceof AttestationResult.Android) {
            ((AttestationResult.Android) result).getAttestationCertificate();
            ((AttestationResult.Android) result).getAttestationRecord();
        }

        if (result instanceof AttestationResult.IOS) {
            ((AttestationResult.IOS) result).getClientData();
        }

        if (result instanceof AttestationResult.Error) {
            Throwable cause = ((AttestationResult.Error) result).getCause();
            String explanation = ((AttestationResult.Error) result).getExplanation();
            Assertions.assertEquals("Attestation proof is empty", explanation);
            Assertions.assertNull(cause);
        }

        Assertions.assertTrue(result instanceof AttestationResult.Error);


        KeyAttestation<ECPublicKey> keyAttestationResult = service.verifyKeyAttestation(Collections.emptyList(),
                new byte[]{0, 2, 3, 2, 2}, (ECPublicKey) KeyPairGenerator.getInstance("EC").
                        generateKeyPair().getPublic());


        Assertions.assertFalse(keyAttestationResult.isSuccess());
        Assertions.assertNull(keyAttestationResult.getAttestedPublicKey());
    }

    public static void javaDemo() {
        byte[] challenge = new byte[]{0, 2, 3, 4, 5, 6};
        List<X509Certificate> certificateChain = Collections.emptyList();

        List<AndroidAttestationConfiguration.AppData> apps = new LinkedList<>();

        apps.add(new AndroidAttestationConfiguration.AppData(
                "at.asitplus.example",
                Collections.singletonList(Base64.getDecoder().decode("NLl2LE1skNSEMZQMV73nMUJYsmQg7+Fqx/cnTw0zCtU="))
        ));
        apps.add(new AndroidAttestationConfiguration.AppData(
                "at.asitplus.anotherexample",
                Collections.singletonList(Base64.getDecoder().decode("NLl2LE1skNSEMZQMV73nMUJYsmQg7+Fqx/cnTw0zCtU=")),
                2
        ));
        AndroidAttestationConfiguration config = new AndroidAttestationConfiguration.Builder(apps)
                .androidVersion(11000)
                .ingoreLeafValidity()
                .patchLevel(new PatchLevel(2023, 03))
                .verificationSecondsOffset(-500) //we to account for time drift
                .build();

        AndroidAttestationChecker checker = new HardwareAttestationChecker(config);
        try {
            ParsedAttestationRecord attestationRecord = checker.verifyAttestation(certificateChain, new Date(), challenge);
            //all good
        } catch (AttestationException | CertificateInvalidException | RevocationException e) {
            //untrusted device/app
        }
    }

}
