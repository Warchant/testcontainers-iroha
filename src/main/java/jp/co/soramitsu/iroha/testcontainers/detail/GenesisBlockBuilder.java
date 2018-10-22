package jp.co.soramitsu.iroha.testcontainers.detail;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;

import iroha.protocol.BlockOuterClass;
import iroha.protocol.Primitive.RolePermission;
import iroha.protocol.TransactionOuterClass;
import java.security.KeyPair;
import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;
import jp.co.soramitsu.iroha.java.Transaction;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
public class GenesisBlockBuilder {

  // this one must be equal to the name passed to iroha entrypoint.sh
  public static final String defaultGenesisBlockName = "genesis.block";
  public static final String defaultRoleName = "default";
  public static final String defaultDomainName = "test";

  public static final KeyPair defaultKeyPair = Ed25519Sha3.keyPairFromBytes(
      parseHexBinary("0000000000000000000000000000000000000000000000000000000000000000"),
      parseHexBinary("43eeb17f0bab10dd51ab70983c25200a1742d31b3b7b54c38c34d7b827b26eed")
  );

  private BlockOuterClass.Block.Payload.Builder blockPayload = BlockOuterClass.Block.Payload
      .newBuilder();

  public GenesisBlockBuilder addDefaultTransaction() {

    addTransaction(
        Transaction.builder(null, Instant.now())
            // create peer with default public key
            .addPeer("0.0.0.0:10001", defaultKeyPair.getPublic().getEncoded())
            // give all permissions
            .createRole(
                defaultRoleName,
                IntStream.range(0, 42 /* check RolePermission numbers */)
                    .boxed()
                    .map(RolePermission::forNumber)
                    .collect(Collectors.toList())
            )
            // create domain "test"
            .createDomain(defaultDomainName, defaultRoleName)
            // create account test@test with default pubkey
            .createAccount("test", defaultDomainName, defaultKeyPair.getPublic())
            .sign(defaultKeyPair)
            .build() // protobuf Transaction
    );

    return this;
  }

  public GenesisBlockBuilder addTransaction(
      @NonNull TransactionOuterClass.Transaction transaction) {
    blockPayload.addTransactions(transaction);
    return this;
  }

  public GenesisBlockBuilder addAllTransactions(
      @NonNull Iterable<? extends TransactionOuterClass.Transaction> transactions) {
    blockPayload.addAllTransactions(transactions);
    return this;
  }

  public BlockOuterClass.Block build() {
    blockPayload.setHeight(1 /* genesis block has height:1 */);
    blockPayload.setCreatedTime(Instant.now().toEpochMilli());
    blockPayload.setTxNumber(blockPayload.getTransactionsCount());

    return BlockOuterClass.Block.newBuilder()
        .setPayload(blockPayload.build())
        .build();
  }
}
