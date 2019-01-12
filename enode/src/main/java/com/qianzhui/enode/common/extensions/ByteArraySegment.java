package com.qianzhui.enode.common.extensions;

import com.qianzhui.enode.common.utilities.Ensure;

public class ByteArraySegment {

    private byte[] array;
    private int offset;
    private int count;

    public ByteArraySegment(byte[] array) {
        Ensure.notNull(array, "array");
        this.array = array;
        this.offset = 0;
        this.count = array.length;
    }

    public ByteArraySegment(byte[] array, int offset, int count) {
        Ensure.notNull(array, "array");
        if (offset >= array.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        if (count + offset > array.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        this.array = array;
        this.offset = offset;
        this.count = count;
    }

    public byte[] getArray() {
        return array;
    }

    public int getOffset() {
        return offset;
    }

    public int getCount() {
        return count;
    }
}
