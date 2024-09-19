package com.train.tspagccn.api.v1.factory.zone;

import com.train.tspagccn.api.v1.zone.ZoneManager;
import com.train.tspagccn.interfaces.zone.IZoneManager;

public class v1ZoneManagerFactory {

  public static IZoneManager newZoneManager() {
    return new ZoneManager();
  }
}
