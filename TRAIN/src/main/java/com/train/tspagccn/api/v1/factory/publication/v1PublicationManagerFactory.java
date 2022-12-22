package com.train.tspagccn.api.v1.factory.publication;

import com.train.tspagccn.api.v1.publication.*; //FIXME: IntelliJ does not want me to put PublicationManager instead of the *
import com.train.tspagccn.interfaces.publication.IPublicationManager;

public class v1PublicationManagerFactory {

  public static IPublicationManager newPublicationManager() {
    return new PublicationManager();
  }
}
