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

import org.jboss.logging.Logger;
import org.keycloak.WebAuthnConstants;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.browser.WebAuthnPasswordlessAuthenticator;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.models.KeycloakSession;
import org.keycloak.utils.StringUtil;
import org.keycloak.models.UserModel;
import org.keycloak.credential.CredentialModel;
import org.keycloak.util.JsonSerialization;
import org.keycloak.models.credential.dto.WebAuthnCredentialData;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RegiTRUSTAuthenticator overrides WebAuthnPasswordlessAuthenticator to enforce RegiTRUST policies.
 * @author Abdul Farooqui
 */

public class RegiTRUSTAuthenticator extends WebAuthnPasswordlessAuthenticator {

  private static final Logger LOG = Logger.getLogger(RegiTRUSTAuthenticator.class);
  private static final String WEBAUTHN_CREDENTIAL_ID = "WebauthnCredentialID";
  private static final String WEBAUTHN_CRED = "webauthn-passwordless";

  public RegiTRUSTAuthenticator(KeycloakSession session) {
    super(session);
  }

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    super.authenticate(context);
  }

  @Override
  public void action(AuthenticationFlowContext context) {
    super.action(context);
    UserModel user = context.getUser();
    if (context.getUser() != null) {
      LOG.debugf("User Name %s", user.getUsername());
      String webAuthnCredID = user.getFirstAttribute(WEBAUTHN_CREDENTIAL_ID);
      if (StringUtil.isNullOrEmpty(webAuthnCredID)) {
        // First time - Set the user attribute
        List<CredentialModel> cmodelList = user.credentialManager().getStoredCredentialsByTypeStream(WEBAUTHN_CRED)
            .collect(Collectors.toList());
        if (cmodelList.isEmpty()) {
          LOG.debugf("Credential %s list is empty for %s", WEBAUTHN_CRED, user.getUsername());
          String errorMessage = "There is no Passkey registered for this account.";
          Response challenge = context.form().setError(errorMessage).createErrorPage(Response.Status.UNAUTHORIZED);
          context.failure(AuthenticationFlowError.GENERIC_AUTHENTICATION_ERROR, challenge, "Passkey Not Registered",
              errorMessage);
          return;
        } else {
          LOG.debugf("Credential %s exist for %s", WEBAUTHN_CRED, user.getUsername());
          CredentialModel cmodel = cmodelList.get(0);
          try {
            WebAuthnCredentialData data = JsonSerialization.readValue(cmodel.getCredentialData(),
                WebAuthnCredentialData.class);
            LOG.debugf("Credential ID %s", data.getCredentialId());
            LOG.debugf("Credential Public Key %s", data.getCredentialPublicKey());
            String credIdStr = StringUtil.removeSuffix(data.getCredentialId(), "==");
            user.setSingleAttribute(WEBAUTHN_CREDENTIAL_ID, credIdStr);
            context.success();
          } catch (Exception e) {
            String errorMessage = e.getMessage();
            Response challenge = context.form().setError(errorMessage).createErrorPage(Response.Status.UNAUTHORIZED);
            context.failure(AuthenticationFlowError.GENERIC_AUTHENTICATION_ERROR, challenge, "Internal Error",
                errorMessage);
            return;
          }
        }
      } else {
        MultivaluedMap<String, String> params = context.getHttpRequest().getDecodedFormParameters();
        String credentialIdParam = params.getFirst(WebAuthnConstants.CREDENTIAL_ID).replaceAll("-", "+");
        //
        // Is the credential id from key same as what we have in user attribute?
        // Yes : It is the same key
        // No : User has registered a new key - WHO does not like it. Go away user.
        LOG.debugf("StoredCredential ID is:   %s", webAuthnCredID);
        LOG.debugf("Credential ID from param: %s", credentialIdParam);
        if (credentialIdParam.equals(webAuthnCredID)) {
          context.success();
        } else {
          String errorMessage = "Please login using the RegiTRUST issued Yubikey.";
          Response challenge = context.form().setError(errorMessage).createErrorPage(Response.Status.UNAUTHORIZED);
          context.failure(AuthenticationFlowError.GENERIC_AUTHENTICATION_ERROR, challenge, "Unofficial Key",
              errorMessage);
        }
      }
    }
  }

  @Override
  public boolean requiresUser() {
    return true;
  }
}
