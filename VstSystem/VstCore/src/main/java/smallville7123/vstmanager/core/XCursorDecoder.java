package smallville7123.vstmanager.core;

public class XCursorDecoder {

    String toHexString(byte b) {
        return "0x" + Integer.toHexString(b);
    }

    String toHexString(int b) {
        return "0x" + Integer.toHexString(b);
    }

    String toHexString(byte[] bytes, int amount, int offset) {
        String s = new String("");
        for (int i = offset; i < amount+offset; i++) {
            if (i == offset) s = s + toHexString(bytes[i]);
            else s = s + ", " + toHexString(bytes[i]);
        }
        return s;
    }

    String toHexString(byte[] bytes, int amount) {
        return toHexString(bytes, amount, 0);
    }

    String toHexString(byte[][] bytes, int amount1, int offset1, int amount2, int offset2) {
        String s = new String("");
        for (int i = offset1; i < amount1+offset1; i++) {
            if (i == offset1) s = s + toHexString(bytes[i], amount2, offset2);
            else s = s + ", " + toHexString(bytes[i], amount2, offset2);
        }
        return s;
    }

    String toHexString(byte[][] bytes, int amount1, int amount2) {
        return toHexString(bytes, amount1, 0, amount2, 0);
    }

    class ByteDump {

        int groupSize = 4;
        int counterA = 0;
        int counterB = 0;

        String dump(byte[] bytes, int amount, int offset, boolean displayCounter, boolean emmitNewLines) {
            String s = new String();
            ByteReader x = new ByteReader();
            for (int i = offset; i < amount+offset; i += groupSize) {
                if (emmitNewLines) if (counterA != 0 && (i % groupSize == 0)) s += "\n";
                if (displayCounter) s += counterA + ": ";
                s += toHexString(x.read(bytes, groupSize, 0), groupSize);
                counterA+=groupSize;
            }
            return s;
        }

        String dump(byte[] bytes, int amount, int offset) {
            return "byte dump (" + amount + " bytes):\n" + dump(bytes, amount, offset, true, true);
        }

        String dump(byte[][] bytes, int amount1, int offset1, int amount2, int offset2) {
            // TODO: find correct byte count
            //     amount1 is incorrect
            //     (amount1*amount2) is incorrect
            String s = new String("byte dump (" + amount1 + " bytes):\n");

            for (int i = offset1; i < amount1+offset1; i += amount2) {
                if (counterB != 0 && (i % groupSize == 0)) s += "\n" + counterB + ": ";
                else if (counterB == 0) s += counterB + ": ";
                else s += ", ";
                int groupSize_ = groupSize;
                groupSize = amount2;
                s += dump(bytes[i], amount2, offset2, false, false);
                groupSize = groupSize_;
                counterB+=groupSize;
            }

            return s;
        }

        String dump(byte[][] bytes, int amount1, int amount2) {
            return dump(bytes, amount1, 0, amount2, 0);
        }

    }

    class ByteReader {
        int position = 0;

        byte[] read(byte[] bytes, int size, int offset) {
            position += offset;

            int start = position;
            int end = position+size;

            byte[] x = new byte[size];

            for (int i = start; i < end; i++) {
                x[i-start] = bytes[i];
                position++;
            }

            return x;
        }

        void seek(int position) {
            this.position = position;
        }
    }

    ByteReader x = null;

    class TOC_chunk {
        boolean isComment = false;
        boolean isImage = false;

        byte[] header;
        byte[] type;
        byte[] subtype;
        byte[] version;

        byte[] length;
        byte[][] string;

        int width;
        int height;
        int xhot;
        int yhot;
        int delay;
        byte[][] pixels;

        void readCommonHeader(byte[] f) {
            header = x.read(f, 4, 0);
            type = x.read(f, 4, 0);
            subtype = x.read(f, 4, 0);
            version = x.read(f, 4, 0);
        }

        void readComment(byte[] f) {
            isComment = true;
            isImage = false;
            readCommonHeader(f);
            length = x.read(f, 4, 0);

            //   string: LISTofCARD8 UTF-8 string

            int l = ByteTools.readIntRetainPosition(length, 1, 0)[0];

            string = new byte[l][];
            for (int i = 0; i < l; i++) string[i] = x.read(f, 1, 0);

        }

        void readImage(byte[] f) {
            isComment = false;
            isImage = true;
            readCommonHeader(f);
            width = ByteTools.readIntRetainPosition(x.read(f, 4, 0), 1, 0)[0];
            height = ByteTools.readIntRetainPosition(x.read(f, 4, 0), 1, 0)[0];
            xhot = ByteTools.readIntRetainPosition(x.read(f, 4, 0), 1, 0)[0];
            yhot = ByteTools.readIntRetainPosition(x.read(f, 4, 0), 1, 0)[0];
            delay = ByteTools.readIntRetainPosition(x.read(f, 4, 0), 1, 0)[0];

            //   pixels: LISTofCARD32 Packed ARGB format pixels

            int l =  height*width;

            pixels = new byte[l][];
            for (int i = 0; i < l; i++) pixels[i] = x.read(f, 4, 0);

        }

        String toString(boolean printPixels) {
            String s;
            s = "        header:                 " + toHexString(header, 4);
            s = s + "\n        type =                  " + toHexString(type, 4) + " (" + (isImage ? "Image" : isComment ? "Comment" : "None") + ")";
            s = s + "\n        subtype =               " + toHexString(subtype, 4);
            s = s + "\n        version =               " + toHexString(version, 4);
            if (isImage) {
                s = s + "\n        width =                 " + width;
                s = s + "\n        height =                " + height;
                s = s + "\n        xhot =                  " + xhot;
                s = s + "\n        yhot =                  " + yhot;
                s = s + "\n        delay =                 " + delay;
                if (printPixels) {
                    ByteDump bt = new ByteDump();
                    bt.groupSize = 8;
                    s = s + "\n        pixels =                " + bt.dump(pixels, width * height, 4);
                }
            } else if (isComment) {
                s = s + "\n        length =                " + toHexString(length, 4);
                ByteDump bt = new ByteDump();
                bt.groupSize = 1;
                s = s + "\n        string =                " + bt.dump(string, ByteTools.readIntRetainPosition(length, 1, 0)[0], 1);
            }
            return s;
        }
    }

    class TOC {
        byte[] type;
        byte[] subtype;
        byte[] position;
        TOC_chunk chunk;

        void read(byte[] f) {
            type = x.read(f, 4, 0);
            subtype = x.read(f, 4, 0);
            position = x.read(f, 4, 0);
            chunk = new TOC_chunk();
            int pos = ByteTools.readIntRetainPosition(position, 1, 0)[0];
            if (type[1] == 0x0 && type[3] == 0xffffffff) {
                if (type[0] == 0x1 && type[2] == 0xfffffffe) {
                    x.seek(pos);
                    chunk.readComment(f);
                } else if (type[0] == 0x2 && type[2] == 0xfffffffd) {
                    x.seek(pos);
                    chunk.readImage(f);
                }
            }
        }

        String toString(boolean printPixels) {
            String s;
            s = "    type:                   " + toHexString(type, 4);
            s = s + "\n    subtype =               " + toHexString(subtype, 4);
            s = s + "\n    position =              " + toHexString(position, 4);
            s = s + "\n    TOC chunk:\n" + chunk.toString(printPixels);
            return s;
        }

    }

    class Header {
        byte[] magic;
        byte[] header;
        byte[] version;
        byte[] ntoc;
        TOC[] toc;

        void read(byte[] f) {
            int s = x.position;
            magic = x.read(f, 4, 0);
            header= x.read(f, 4, 0);
            version = x.read(f, 4, 0);
            ntoc = x.read(f, 4, 0);
            x.seek(s+header[0]);
            toc = new TOC[1];
            for (int i = 0; i < ntoc[0]; i++) {
                toc[i] = new TOC();
                toc[i].read(f); //<>// //<>//
            }
        }

        boolean isValid(byte[] f) {
            return f[0] == 0x58 && f[1] == 0x63 && f[2] == 0x75 && f[3] == 0x72;
        }

        String toString(boolean printPixels) {
            String s;
            s = "magic:                  " + toHexString(magic, 4);
            s = s + "\nheader =                " + toHexString(header, 4);
            s = s + "\nversion =               " + toHexString(version, 4);
            s = s + "\nntoc =                  " + toHexString(ntoc, 4);
            for (int i = 0; i < ntoc[0]; i++) {
                s = s + "\ntoc " + i + ":\n" + toc[i].toString(printPixels);
            }
            return s;
        }

    }

    class CursorData {
//        PImage image = null;
        int width = 0;
        int height = 0;
        int xhot = 0;
        int yhot = 0;
    }

    CursorData getImage(Header header, int index) {
        int index_ = 0;
        for (TOC toc : header.toc) {
            TOC_chunk chunk = toc.chunk;
            if (chunk.isImage) {
                if (index == index_) {
                    CursorData cd = new CursorData();
                    cd.width = chunk.width;
                    cd.xhot = chunk.xhot;
                    cd.height = chunk.height;
                    cd.yhot = chunk.yhot;
//                    cd.image = createImage(cd.width, cd.height, ARGB);
//                    cd.image.loadPixels();
//                    for (int i = 0; i < cd.image.pixels.length; i++) {
//                        cd.image.pixels[i] = ByteTools.readIntRetainPosition(chunk.pixels[i], 1, 0)[0];
//                    }
//                    cd.image.updatePixels();
                    return cd;
                } else {
                    index_++;
                }
            }
        }
        return null;
    }

//    File f = null;
    byte[] byteFile = null;
    Header header = null;

    boolean load(String file) {
//        f = null;
        byteFile = null;
        header = null;
//        f = dataFile(file);
//        if (!f.exists()) {
//            f = null;
//            println("error: " + file + " does not exist");
//            return false;
//        }
//        byteFile = loadBytes(f);
        header = new Header();
        if (header.isValid(byteFile)) {
            x = new ByteReader();
            header.read(byteFile);
            return true;
        } else {
            header = null;
//            println(file + " is an invalid XCursor");
//            println("expected magic: 0x58, 0x63, 0x75, 0x72");
//            println("got magic:      " + toHexString(byteFile, 4));
        }
        return false;
    }

    String info(boolean printPixels) {
        return header != null ? header.toString(printPixels) : "";
    }

    CursorData decode() {
        return header != null ? getImage(header, 0) : null;
    }

    CursorData loadAndDecode(String file) {
        return load(file) ? decode() : null;
    }
}