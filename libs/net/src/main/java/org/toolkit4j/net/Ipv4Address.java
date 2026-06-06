package org.toolkit4j.net;

import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/*
 * IPv4 地址实现
 */
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Ipv4Address implements IpAddress {
  private final int address; // IPv4 4字节整数表示

  /** 构造方法：从字符串解析，例如 "192.168.1.1" */
  @Contract("_ -> new")
  public static @NotNull Ipv4Address of(@NonNull String ip) {
    val parts = ip.split("\\.");
    if (parts.length != 4) {
      throw new IllegalArgumentException("Invalid IPv4 address: " + ip);
    }
    val address =
        IntStream.range(0, parts.length)
            .map(index -> parseOctet(ip, parts[index]) << ((3 - index) * 8))
            .reduce(0, (left, right) -> left | right);
    return new Ipv4Address(address);
  }

  /** 构造方法：从整数表示 */
  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Ipv4Address of(int address) {
    return new Ipv4Address(address);
  }

  @Contract(value = " -> new", pure = true)
  @Override
  public byte @NotNull [] bytes() {
    return new byte[] {
      (byte) (address >> 24), (byte) (address >> 16), (byte) (address >> 8), (byte) address
    };
  }

  @Override
  public boolean isLoopback() {
    return (address >>> 24) == 127;
  }

  @Override
  public boolean isPrivate() {
    val first = (address >>> 24) & 0xFF;
    val second = (address >>> 16) & 0xFF;
    return (first == 10)
        || (first == 172 && second >= 16 && second <= 31)
        || (first == 192 && second == 168);
  }

  /** 转换成标准点分十进制字符串 */
  @Override
  public String toString() {
    return ((address >> 24) & 0xFF)
        + "."
        + ((address >> 16) & 0xFF)
        + "."
        + ((address >> 8) & 0xFF)
        + "."
        + (address & 0xFF);
  }

  /** 内部方法：返回整数形式 */
  int toInt() {
    return address;
  }

  private static int parseOctet(String ip, String part) {
    int octet;
    try {
      octet = Integer.parseInt(part);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid IPv4 address: " + ip, e);
    }
    if (octet < 0 || octet > 255) {
      throw new IllegalArgumentException("Invalid IPv4 address: " + ip);
    }
    return octet;
  }
}
