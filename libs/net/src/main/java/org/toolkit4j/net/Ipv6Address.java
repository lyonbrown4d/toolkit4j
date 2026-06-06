package org.toolkit4j.net;

import java.math.BigInteger;
import java.net.InetAddress;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/** IPv6 地址 */
@EqualsAndHashCode
public final class Ipv6Address implements IpAddress {
  private final BigInteger address; // 128位

  private Ipv6Address(@NotNull BigInteger address) {
    if (address.signum() < 0 || address.bitLength() > 128) {
      throw new IllegalArgumentException("Invalid IPv6 value");
    }
    this.address = address;
  }

  @Contract("_ -> new")
  public static @NotNull Ipv6Address of(@NonNull String ip) {
    val parts = ip.split(":", -1);
    if (parts.length < 3 || parts.length > 8) {
      throw new IllegalArgumentException("Invalid IPv6 address: " + ip);
    }

    // 使用标准 Java 方法解析简化
    InetAddress inet;
    try {
      inet = java.net.InetAddress.getByName(ip);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid IPv6 address: " + ip, e);
    }
    val bytes = inet.getAddress();
    if (bytes.length != 16) {
      throw new IllegalArgumentException("Not a valid IPv6 address: " + ip);
    }
    return new Ipv6Address(new BigInteger(1, bytes));
  }

  static @NotNull Ipv6Address of(@NotNull BigInteger address) {
    return new Ipv6Address(address);
  }

  @Override
  public byte @NotNull [] bytes() {
    val arr = address.toByteArray();
    if (arr.length == 16) return arr;
    // 补零到16字节
    val res = new byte[16];
    System.arraycopy(arr, 0, res, 16 - arr.length, arr.length);
    return res;
  }

  @Override
  public boolean isLoopback() {
    return address.equals(BigInteger.ONE);
  }

  @Override
  public boolean isPrivate() {
    // IPv6 私网: fc00::/7
    val prefix = address.shiftRight(121); // 128-7=121
    return prefix.intValue() == 0b1111110; // fc00::/7
  }

  @SneakyThrows
  @Override
  public String toString() {
    val inet = InetAddress.getByAddress(bytes());
    return inet.getHostAddress();
  }
}
