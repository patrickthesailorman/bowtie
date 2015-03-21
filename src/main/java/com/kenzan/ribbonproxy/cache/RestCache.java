/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.kenzan.ribbonproxy.cache;

import java.util.Optional;

public interface RestCache {

    public Optional<byte[]> get(String key);

    public void set(String key, byte[] value);
}
