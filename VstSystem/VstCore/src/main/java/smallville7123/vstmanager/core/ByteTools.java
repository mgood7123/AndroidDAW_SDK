package smallville7123.vstmanager.core;

public class ByteTools {
    int position = 0;

    int[] readInt(byte[] bytes, int size, int offset) {
        position += offset;

        int start = position;
        int end = position+(size*4);

        int[] x = new int[size];

        for (int i = start; i < end; i+=4) {
            x[i-start] = (
                    (bytes[i+0] & 0xFF) << 0
            ) | (
                    (bytes[i+1] & 0xFF) << 8
            ) | (
                    (bytes[i+2] & 0xFF) << 16
            ) | (
                    (bytes[i+3] & 0xFF) << 24
            );
            position+=4;
        }

        return x;
    }

    static int[] readIntRetainPosition(byte[] bytes, int size, int offset) {
        int start = offset;
        int end = offset+(size*4);

        int[] x = new int[size];

        for (int i = start; i < end; i+=4) {
            x[i-start] = (
                    (bytes[i+0] & 0xFF) << 0
            ) | (
                    (bytes[i+1] & 0xFF) << 8
            ) | (
                    (bytes[i+2] & 0xFF) << 16
            ) | (
                    (bytes[i+3] & 0xFF) << 24
            );
        }

        return x;
    }

    int[] readIntBigEndian(byte[] bytes, int size, int offset) {
        position += offset;

        int start = position;
        int end = position+(size*4);

        int[] x = new int[size];

        for (int i = start; i < end; i+=4) {
            x[i-start] = (
                    (bytes[i+3] & 0xFF) << 0
            ) | (
                    (bytes[i+2] & 0xFF) << 8
            ) | (
                    (bytes[i+1] & 0xFF) << 16
            ) | (
                    (bytes[i+0] & 0xFF) << 24
            );
            position+=4;
        }

        return x;
    }

    static int[] readIntBigEndianRetainPosition(byte[] bytes, int size, int offset) {
        int start = offset;
        int end = offset+(size*4);

        int[] x = new int[size];

        for (int i = start; i < end; i+=4) {
            x[i-start] = (
                    (bytes[i+3] & 0xFF) << 0
            ) | (
                    (bytes[i+2] & 0xFF) << 8
            ) | (
                    (bytes[i+1] & 0xFF) << 16
            ) | (
                    (bytes[i+0] & 0xFF) << 24
            );
        }

        return x;
    }

    void seek(int position) {
        this.position = position;
    }
}