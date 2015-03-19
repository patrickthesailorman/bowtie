/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.kenzan.ribbonproxy.cache;

import java.util.Optional;

public interface RestCache {

    public Optional<Object> get(String key);

    public void set(String key, Object value);
}
