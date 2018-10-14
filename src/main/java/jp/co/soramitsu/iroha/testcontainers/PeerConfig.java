package jp.co.soramitsu.iroha.testcontainers;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.JsonFormat;
import iroha.protocol.BlockOuterClass;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;
import jp.co.soramitsu.iroha.testcontainers.detail.GenesisBlockBuilder;
import jp.co.soramitsu.iroha.testcontainers.detail.IrohaConfig;
import jp.co.soramitsu.iroha.testcontainers.detail.RuntimeIOException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.junit.Rule;
import org.testcontainers.shaded.com.google.common.io.Files;

/**
 *
 */
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class PeerConfig {

  public static final String peerKeypairName = "iroha_peer_key";

  @Getter
  private final Map<String, KeyPair> keyPairMap = new HashMap<>();

  @Builder.Default
  @Getter
  @Rule
  private File dir = Files.createTempDir();

  @Builder.Default
  @Getter
  private IrohaConfig irohaConfig = IrohaConfig
      .builder()
      .build();

  @Builder.Default
  @Getter
  private BlockOuterClass.Block genesisBlock = new GenesisBlockBuilder()
      .addDefaultTransaction()
      .build();

  private void writeToFile(String filename, String data) throws IOException {
    File file = new File(dir, filename);
    PrintWriter writer = new PrintWriter(file);
    writer.write(data);
    writer.close();
  }

  private void writeKey(String filename, byte[] key) throws IOException {
    writeToFile(filename, DatatypeConverter.printHexBinary(key).toLowerCase());
  }

  private void writeJsonConfig() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    byte[] data = mapper.writeValueAsBytes(irohaConfig);
    writeToFile(IrohaConfig.defaultConfigFileName, new String(data, UTF_8));
  }

  private void writeGenesisBlock() throws IOException {
    String json = JsonFormat.printer().print(genesisBlock);
    writeToFile(
        GenesisBlockBuilder.defaultGenesisBlockName,
        json
    );
  }

  public PeerConfig withPeerKeyPair(KeyPair keyPair) {
    return withKeyPair(peerKeypairName, keyPair);
  }

  public PeerConfig withKeyPair(@NonNull String name, @NonNull KeyPair keyPair) {
    keyPairMap.put(name, keyPair);

    try {
      writeKey(name + ".pub", keyPair.getPublic().getEncoded());
      writeKey(name + ".priv", keyPair.getPrivate().getEncoded());
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    }

    return this;
  }

  public void save() {
    try {
      withPeerKeyPair(GenesisBlockBuilder.defaultKeyPair);
      writeJsonConfig();
      writeGenesisBlock();
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    }
  }
}
