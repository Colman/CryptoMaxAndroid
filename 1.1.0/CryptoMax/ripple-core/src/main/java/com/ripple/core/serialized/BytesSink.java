package com.ripple.core.serialized;

public interface BytesSink {
    void add(byte aByte); //add(new byte[] {aByte});
    void add(byte[] bytes);
}
