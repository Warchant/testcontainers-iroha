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
import lombok.val;

@NoArgsConstructor
public class GenesisBlockBuilder {

  // this one must be equal to the name passed to iroha entrypoint.sh
  public static final String defaultGenesisBlockName = "genesis.block";
  public static final String defaultRoleName = "default";
  public static final String defaultDomainName = "test";
  public static final String defaultAccountName = "test";
  public static final String defaultAccountId = String
      .format("%s@%s", defaultAccountName, defaultDomainName);

  public static final KeyPair defaultKeyPair = Ed25519Sha3.keyPairFromBytes(
      parseHexBinary("0000000000000000000000000000000000000000000000000000000000000000"),
      parseHexBinary("43eeb17f0bab10dd51ab70983c25200a1742d31b3b7b54c38c34d7b827b26eed")
  );

  private BlockOuterClass.Block.Payload.Builder blockPayload = BlockOuterClass.Block.Payload
      .newBuilder();

  /**
   * Add default (test) transaction to genesis block. Useful for testing.
   *
   * @apiNote should be used only for single peer.
   */
  public GenesisBlockBuilder addDefaultTransaction() {
    // by default we add 1 peer (self) to let single-peer setup work
    return addDefaultTransaction(true);
  }

  /**
   * Add default (test) transaction to genesis block. Useful for testing.
   *
   * @param withDefaultPeer flag to indicate whether method needs to add self as peer (true) or not
   * (false)
   */
  public GenesisBlockBuilder addDefaultTransaction(boolean withDefaultPeer) {
    val builder = Transaction.builder(null);

    if (withDefaultPeer) {
      // for multi-peer environment it is possible to disable default peer inclusion
      builder.addPeer("0.0.0.0:10001", defaultKeyPair.getPublic().getEncoded());
    }

    builder
        // give all permissions
        .createRole(
            defaultRoleName,
            IntStream.range(0, 43 /* check RolePermission numbers */)
                .boxed()
                .map(RolePermission::forNumber)
                .collect(Collectors.toList())
        )
        // create domain "test"
        .createDomain(defaultDomainName, defaultRoleName)
        // create account test@test with default pubkey
        .createAccount(defaultAccountName, defaultDomainName, defaultKeyPair.getPublic());

    addTransaction(
        builder
            .sign(defaultKeyPair)
            .build() // protobuf Transaction
    );

    return this;
  }

  /**
   * Add transaction to genesis block.
   */
  public GenesisBlockBuilder addTransaction(
      @NonNull TransactionOuterClass.Transaction transaction) {
    blockPayload.addTransactions(transaction);
    return this;
  }

  /**
   * Add list of transactions to genesis block.
   */
  public GenesisBlockBuilder addAllTransactions(
      @NonNull Iterable<? extends TransactionOuterClass.Transaction> transactions) {
    blockPayload.addAllTransactions(transactions);
    return this;
  }

  /**
   * Generate protobuf block.
   */
  public BlockOuterClass.Block build() {
    blockPayload.setHeight(1 /* genesis block has height:1 */);
    blockPayload.setCreatedTime(Instant.now().toEpochMilli());
    blockPayload.setTxNumber(blockPayload.getTransactionsCount());

    return BlockOuterClass.Block.newBuilder()
        .setPayload(blockPayload.build())
        .build();
  }
}
