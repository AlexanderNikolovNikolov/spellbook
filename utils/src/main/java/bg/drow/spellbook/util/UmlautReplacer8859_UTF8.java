package bg.drow.spellbook.util;

import java.io.*;
import java.util.*;

/**
 * @author ikkari
 *         Date: May 12, 2010
 *         Time: 9:48:06 PM
 */
public class UmlautReplacer8859_UTF8 {

    static Map<Integer, Integer> hexMap = new HashMap<Integer, Integer>();

    static {
        hexMap.put(0xc4, 0x07);
        hexMap.put(0xd6, 0x01);
        hexMap.put(0xdc, 0x02);

        hexMap.put(0xe4, 0x03);
        hexMap.put(0xf6, 0x04);
        hexMap.put(0xfc, 0x05);
        hexMap.put(0xdf, 0x06);
    }

    static int byte0 = -1;
    static int byte1 = -1;

    public static void main(String[] args) throws IOException {
//      InputStream in = new BufferedInputStream(new FileInputStream("/home/ikkari/Desktop/german/Bulgarian_German8859"));
//        OutputStream fout = new BufferedOutputStream(new FileOutputStream("/home/ikkari/Desktop/german/Bulgarian_German8859Replaced"));
//        replaceUmlautsIn8859(in, fout);

//         the output file from replaceUmlautsIn8859 should be converted to UTF8 before invoking replaceUmlautsInUTF8

//        InputStream in = new BufferedInputStream(new FileInputStream("/home/ikkari/Desktop/german/Bulgarian_German8859Replaced"));
//        OutputStream fout = new BufferedOutputStream(new FileOutputStream("/home/ikkari/Desktop/german/Bulgarian_German8859ReplacedUTF8"));
//        replaceUmlautsInUTF8(in, fout);
    }

    private static void replaceUmlautsIn8859(InputStream in, OutputStream out) throws IOException {
        try {
            int bytesRead = read2bytes(in);
            if (bytesRead < 2) return;

            boolean hasWritten2bytes = false;
            while (true) {
                if (hexMap.keySet().contains(byte0) && ((byte1 >= 0x41) && ((byte1 <= 0x7a)))) {
                    out.write(hexMap.get(byte0));
                    out.write(byte1);
                    hasWritten2bytes = true;
                } else if (hexMap.keySet().contains(byte1) && ((byte0 >= 0x41) && ((byte0 <= 0x7a)))) {
                    out.write(byte0);
                    out.write(hexMap.get(byte1));
                    hasWritten2bytes = true;
                } else {
                    out.write(byte0);
                    byte0 = byte1;
                }
                if (hasWritten2bytes) {
                    bytesRead = read2bytes(in);
                    hasWritten2bytes = false;
                    if (bytesRead == 0) return;
                    if (bytesRead < 2) {
                        out.write(byte0);
                        return;
                    }
                } else {
                    if ((byte1 = in.read()) == -1) return;
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        finally {
            in.close();
            out.close();
        }
    }

    private static int read2bytes(InputStream in) throws IOException {
        int i = 0;
        if ((byte0 = in.read()) != -1) {
            ++i;
        }

        if ((byte1 = in.read()) != -1) {
            ++i;
        }
        return i;
    }

    private static void replaceUmlautsInUTF8(InputStream in, OutputStream out) throws IOException {
        int byte0 = -1;

        try {
            if ((byte0 = in.read()) == -1) {
                return;
            }


            while ((byte0 = in.read()) != -1) {
                if (((byte0 & 0xff) == 0x03)) {
                    out.write(0xc3);
                    out.write(0xa4);
                    byte0 = -1;

                } else if (((byte0 & 0xff) == 0x04)) {
                    out.write(0xc3);
                    out.write(0xb6);
                    byte0 = -1;

                } else if (((byte0 & 0xff) == 0x05)) {
                    out.write(0xc3);
                    out.write(0xbc);
                    byte0 = -1;

                } else if (((byte0 & 0xff) == 0x06)) {
                    out.write(0xc3);
                    out.write(0x9f);
                    byte0 = -1;

                } else if (((byte0 & 0xff) == 0x07)) {
                    out.write(0xc3);
                    out.write(0x84);
                    byte0 = -1;

                } else if (((byte0 & 0xff) == 0x01)) {
                    out.write(0xc3);
                    out.write(0x96);
                    byte0 = -1;

                } else if (((byte0 & 0xff) == 0x02)) {
                    out.write(0xc3);
                    out.write(0x9c);
                    byte0 = -1;

                } else {
                    out.write(byte0);

                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        finally {
            in.close();
            out.close();
        }
    }

}
