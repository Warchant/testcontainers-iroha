package jp.co.soramitsu.iroha.testcontainers.iroha;

import com.fasterxml.jackson.databind.ObjectMapper;
import iroha.protocol.BlockOuterClass;
import java.io.File;
import java.io.PrintWriter;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.junit.rules.TemporaryFolder;

@Data
@Builder
public class PeerConfig {

  public static final String peerKeypairName = "iroha_peer_key";

  private final Map<String, KeyPair> keyPairMap = new HashMap<>();

  @NonNull
  @Builder.Default
  private TemporaryFolder dir = new TemporaryFolder();

  @NonNull
  @Builder.Default
  private IrohaConfig irohaConfig = IrohaConfig
      .builder()
      .build();

  @NonNull
  @Builder.Default
  private BlockOuterClass.Block genesisBlock = new GenesisBlockBuilder()
      .addDefaultTransaction()
      .build();

  private void writeKey(String filename, byte[] key) {
    try {
      PrintWriter writer = new PrintWriter(dir.newFile(filename));
      writer.write(DatatypeConverter.printHexBinary(key).toLowerCase());
      writer.close();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private void writeJsonConfig() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      File configFile = dir.newFile(IrohaConfig.configFileName);
      mapper.writeValue(configFile, irohaConfig);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public PeerConfig() {
    save();
  }

  public PeerConfig withPeerKeyPair(KeyPair keyPair) {
    return withKeyPair(peerKeypairName, keyPair);
  }

  public PeerConfig withKeyPair(String name, KeyPair keyPair) {
    keyPairMap.put(name, keyPair);

    writeKey(name + ".pub", keyPair.getPublic().getEncoded());
    writeKey(name + ".priv", keyPair.getPrivate().getEncoded());

    return this;
  }

  public void save() {
    withPeerKeyPair(GenesisBlockBuilder.defaultKeyPair);
    writeJsonConfig();
  }
}
