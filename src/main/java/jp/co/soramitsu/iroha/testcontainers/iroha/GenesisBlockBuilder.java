package jp.co.soramitsu.iroha.testcontainers.iroha;

import com.sun.tools.javac.util.List;
import iroha.protocol.BlockOuterClass;
import iroha.protocol.Primitive.RolePermission;
import iroha.protocol.TransactionOuterClass;
import java.security.KeyPair;
import java.time.Instant;
import javax.xml.bind.DatatypeConverter;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;
import jp.co.soramitsu.iroha.java.Transaction;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor
public class GenesisBlockBuilder {

  public static final KeyPair defaultKeyPair = Ed25519Sha3.keyPairFromBytes(
      DatatypeConverter
          .parseHexBinary("0000000000000000000000000000000000000000000000000000000000000000"),
      DatatypeConverter
          .parseHexBinary("43eeb17f0bab10dd51ab70983c25200a1742d31b3b7b54c38c34d7b827b26eed")
  );

  private BlockOuterClass.Block.Payload.Builder blockPayload = BlockOuterClass.Block.Payload
      .newBuilder();

  @SneakyThrows
  public GenesisBlockBuilder addDefaultTransaction() {

    addTransaction(
        Transaction.builder(null, Instant.now())
            // create peer with default public key
            .addPeer("0.0.0.0:10001", defaultKeyPair.getPublic().getEncoded())
            // give all permissions
            .createRole("defaultRole", List.from(RolePermission.values()))
            // create domain "test"
            .createDomain("test", "defaultRole")
            // create account test@test with default pubkey
            .createAccount("test", "test", defaultKeyPair.getPublic())
            // set default role for account test@test (can do anything)
            .appendRole("test@test", "defaultRole")
            .build() // iroha-pure-java model Transaction
            .build() // protobuf Transaction
    );

    return this;
  }

  public GenesisBlockBuilder addTransaction(TransactionOuterClass.Transaction transaction) {
    blockPayload.addTransactions(transaction);
    return this;
  }

  public GenesisBlockBuilder addAllTransactions(
      Iterable<? extends TransactionOuterClass.Transaction> transactions) {
    blockPayload.addAllTransactions(transactions);
    return this;
  }

  public BlockOuterClass.Block build() {
    return BlockOuterClass.Block.newBuilder()
        .setPayload(blockPayload)
        .build();
  }
}
