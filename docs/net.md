# Net

Artifact: `io.github.lyonbrown4d:net:0.0.6`

## What it provides

- IPv4 model: `Ipv4Address`
- IPv6 model: `Ipv6Address`
- CIDR utility: `Cidr`
- IP metadata carrier: `IpInfo`

## Minimal examples

```java
import org.toolkit4j.net.Cidr;
import org.toolkit4j.net.Ipv4Address;
import org.toolkit4j.net.IpInfo;
import org.toolkit4j.net.IpVersion;

var ip = Ipv4Address.of("192.168.1.10");
var cidr = Cidr.of("192.168.1.0/24");
var contains = cidr.contains(ip); // true

var info = new IpInfo("192.168.1.10", IpVersion.IPV4);
```

## Notes

- Supports both IPv4 and IPv6 under the same `IpAddress` abstraction.
- `Cidr` normalizes host bits in the network part, so `192.168.1.99/24` is treated as `192.168.1.0/24`.
- `Cidr.contains(...)` only matches addresses from the same IP family.
