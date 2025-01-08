/*
 * Copyright 2024 SymSoft Solutions, LLC and its affilitates axyom.co
 * The code is based on Keycloak Code Originally Copyrighted by:
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.axyom.keycloak.RegiTRUSTAuthenticator;

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.browser.WebAuthnAuthenticatorFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.credential.WebAuthnCredentialModel;

/**
 * RegiTRUSTAuthenticatorFactory overrides WebAuthnAuthenticatorFactory to 
 * facilitate keycloak to create instances of RegiTRUSTAuthenticator.
 * @author Abdul Farooqui
 */
public class RegiTRUSTAuthenticatorFactory extends WebAuthnAuthenticatorFactory {

    public static final String PROVIDER_ID = "Webauthn Passwordless (RegiTRUST)";

    @Override
    public String getReferenceCategory() {
        return WebAuthnCredentialModel.TYPE_PASSWORDLESS;
    }

    @Override
    public String getDisplayType() {
        return "Webauthn Passwordless (RegiTRUST)";
    }

    @Override
    public String getHelpText() {
        return "Webauthn Passwordless Authenticator tailored for RegiTRUST";
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new RegiTRUSTAuthenticator(session);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }
}
