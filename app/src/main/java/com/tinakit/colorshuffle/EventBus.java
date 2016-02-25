package com.tinakit.colorshuffle;

import com.squareup.otto.Bus;

/**
 * Created by Tina on 2/25/2016.
 */
public class EventBus {

    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }
}
