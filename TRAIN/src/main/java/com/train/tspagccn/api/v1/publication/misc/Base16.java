package com.train.tspagccn.api.v1.publication.misc;

public class Base16 {
  private static final char[] HEXCHARS = new char[] {
    '0',
    '1',
    '2',
    '3',
    '4',
    '5',
    '6',
    '7',
    '8',
    '9',
    'A',
    'B',
    'C',
    'D',
    'E',
    'F'
  };

  private static int toNumber(int d) {
    if (d >= '0' && d <= '9') d -= '0'; else if (d >= 'A' && d <= 'F') d -= 'A' - 10;
    return d;
  }

  public static byte[] decode(String s) {
    int l = s.length();

    byte[] r = new byte[l / 2];

    for (int i = 0; i < r.length; ++i) {
      int d1 = s.charAt(i * 2);
      int d2 = s.charAt(i * 2 + 1);

      d1 = toNumber(d1);
      d2 = toNumber(d2);

      r[i] = (byte) ((d1 << 4) + d2);
    }

    return r;
  }

  public static String encode(byte[] bytes) {
    StringBuffer buffer = new StringBuffer(bytes.length * 2);
    for (int i = 0; i < bytes.length; ++i) {
      for (int j = 1; j >= 0; --j) {
        buffer.append(HEXCHARS[(bytes[i] >> (j * 4)) & 0x0F]);
      }
    }
    return buffer.toString();
  }
}
