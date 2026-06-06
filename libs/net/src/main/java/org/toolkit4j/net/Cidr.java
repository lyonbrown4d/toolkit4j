package org.toolkit4j.net;

import java.math.BigInteger;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

/** CIDR 网段（支持 IPv4 / IPv6） */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Cidr {
  @EqualsAndHashCode.Include private final IpAddress network;
  @EqualsAndHashCode.Include @Getter private final int prefixLength;
  private final BigInteger networkValue;
  private final BigInteger maskValue;

  private Cidr(IpAddress network, int prefixLength) {
    if (prefixLength < 0
        || (network instanceof Ipv4Address ipv4 && prefixLength > 32)
        || (network instanceof Ipv6Address ipv6 && prefixLength > 128)) {
      throw new IllegalArgumentException("Invalid prefix length: " + prefixLength);
    }
    this.prefixLength = prefixLength;
    val bytes = network.bytes();
    val rawNetworkValue = new BigInteger(1, bytes);

    val bits = bytes.length * 8;
    this.maskValue =
        prefixLength == 0
            ? BigInteger.ZERO
            : BigInteger.ONE
                .shiftLeft(bits)
                .subtract(BigInteger.ONE)
                .shiftRight(prefixLength)
                .not()
                .and(BigInteger.ONE.shiftLeft(bits).subtract(BigInteger.ONE));
    this.networkValue = rawNetworkValue.and(maskValue);
    this.network = normalizeNetwork(network, networkValue);
  }

  @Contract("_ -> new")
  public static @NotNull Cidr of(@NonNull String cidrStr) {
    val parts = cidrStr.split("/", 2);
    if (parts.length != 2) throw new IllegalArgumentException("Invalid CIDR: " + cidrStr);

    IpAddress ip;
    try {
      ip = Ipv4Address.of(parts[0]);
    } catch (IllegalArgumentException e1) {
      ip = Ipv6Address.of(parts[0]);
    }

    val prefix = Integer.parseInt(parts[1]);
    return new Cidr(ip, prefix);
  }

  public boolean contains(@NotNull IpAddress ip) {
    if (!network.getClass().equals(ip.getClass())) {
      return false;
    }
    val ipVal = new BigInteger(1, ip.bytes());
    return ipVal.and(maskValue).equals(networkValue);
  }

  @Contract("null, _ -> fail")
  private static @NotNull @Unmodifiable IpAddress normalizeNetwork(IpAddress network, BigInteger normalizedValue) {
    if (network instanceof Ipv4Address) {
      return Ipv4Address.of(normalizedValue.intValue());
    }
    if (network instanceof Ipv6Address) {
      return Ipv6Address.of(normalizedValue);
    }
    throw new IllegalArgumentException(
        "Unsupported IP address type: " + network.getClass().getName());
  }

  @Override
  public String toString() {
    return network + "/" + prefixLength;
  }

}
