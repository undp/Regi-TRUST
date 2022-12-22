package com.train.tspagccn.api;

import com.train.tspagccn.api.exceptions.UnknownInterfaceVersion;
import com.train.tspagccn.api.v1.factory.publication.v1PublicationManagerFactory;
import com.train.tspagccn.api.v1.factory.zone.v1ZoneManagerFactory;
import com.train.tspagccn.interfaces.publication.IPublicationManager;
import com.train.tspagccn.interfaces.zone.IZoneManager;

public class VersionFactory {

  public static IZoneManager makeZoneManagerFactory(String vType)
    throws UnknownInterfaceVersion {
    if (vType.compareTo("v1") == 0) {
      return new v1ZoneManagerFactory().newZoneManager();
    } else {
      throw new UnknownInterfaceVersion(vType);
    }
  }

  public static IPublicationManager makePublicationManagerFactory(String vType)
    throws UnknownInterfaceVersion {
    if (vType.compareTo("v1") == 0) {
      return new v1PublicationManagerFactory().newPublicationManager();
    } else {
      throw new UnknownInterfaceVersion(vType);
    }
  }
}
