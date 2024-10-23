import express from "express";
import "dotenv/config";
import axios from "axios";
import jwt from "jsonwebtoken";
import { getBearerToken, setBearerToken, getRefreshToken, setRefreshToken } from "./auth.js"; 

const router = express();

// Generates authentication url to redirect the user to IdP login page. Called when user clicks "access"
router.post("/authenticate", (req, res) => {
  const params = new URLSearchParams();
  params.append("client_id", process.env.CLIENT_ID);
  params.append("redirect_uri", process.env.REDIRECT_URL);
  params.append("scope", "openid");
  params.append("response_type", "code");

  const authenticateURL = new URL(
    `${process.env.OIDC_ISSUER_URL}/protocol/openid-connect/auth`
  );
  authenticateURL.search = params;
  console.log(
    "generating url to transfer the user (to IdP) for login: " +
      authenticateURL.href
  );
  res.json({ authUrl: authenticateURL.href });
});

// Get access/refresh tokens from IdP to authorize user. It's the callback from IdP
router.post("/authorize", (req, res) => {
  const authorizationCode = req.body.code;
  if (authorizationCode === undefined) {
    res.json({
      message: "No auth code provided",
    });
  }
  const tokenEndpoint = `${process.env.OIDC_ISSUER_URL}/protocol/openid-connect/token`;
  const redirectUri = process.env.REDIRECT_URL;

  const data = new URLSearchParams();
  data.append("grant_type", "authorization_code");
  data.append("client_id", process.env.CLIENT_ID);
  data.append("client_secret", process.env.CLIENT_SECRET);
  data.append("code", authorizationCode);
  data.append("scope", "openid");
  data.append("redirect_uri", redirectUri);
  let allowed;

  axios
    .post(tokenEndpoint, data, {
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
    })
    .then((response) => {
      const decodedToken = jwt.decode(response.data.access_token);
      setBearerToken(response.data.access_token)
      setRefreshToken(response.data.refresh_token);
      allowed = decodedToken.realm_access.roles.includes(process.env.ROLE_ALLOWED);
      res.json({ allowed });
    })
    .catch((error) => {
      console.error("Error:", error.response);
    });
});

router.post("/logout", (req, res) => {
  const logoutURL = new URL(
    `${process.env.OIDC_ISSUER_URL}/protocol/openid-connect/logout`
  );
  const params = new URLSearchParams();
  params.append("client_id", process.env.CLIENT_ID);
  params.append("client_secret", process.env.CLIENT_SECRET);
  params.append("refresh_token", getRefreshToken());

  axios
    .post(logoutURL, params, {
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
    })
    .then(() => {
      setBearerToken("")
      setRefreshToken("")
      res.json({});
    })
    .catch((error) => {
      console.error("Error:", error.response);
    });
});

router.post("/addtf", (req, res) => {
  console.log("adding a tf...");
  const tfPointers = req.body.tfPointers;
  const tfName = req.body.tfName;

  const tfUrl = `${process.env.TSPA_BASE_URL}/tspa-service/tspa/v1/trustframework/${tfName}`;
  axios
    .put(
      tfUrl,
      { schemes: tfPointers },
      {
        headers: {
          Authorization: `Bearer ${getBearerToken()}`,
        },
      }
    )
    .then((response) => {
      console.log("200 adding a tf successfully");
      res.status(response.status).json(response.data);
    })
    .catch((error) => {
      if (error.response) {
        const error_data = {error};
        res.status(error.response.status).json(error_data);
        console.error(">>> Error publishing TF: " + error.response.status);
      } else {
        console.error(">>> TSPA server error no response. ", error);
        const error_data = {message: error.message, response: error};
        res.status(500).json(error_data);
      }
    });
});

router.post("/add-did", (req, res) => {
  console.log("addint a did...");
  const did = req.body.did;
  const tfNameForDID = req.body.tfNameForDID;

  const didUrl = `${process.env.TSPA_BASE_URL}/tspa-service/tspa/v1/${tfNameForDID}/did`;
  axios
    .put(
      didUrl,
      { did },
      {
        headers: {
          Authorization: `Bearer ${getBearerToken()}`,
        },
      }
    )
    .then((response) => {
      console.log("200 adding a DID");
      res.status(response.status).json(response.data);
    })
    .catch((error) => {
      if (error.response) {
        res.status(error.response.status).json({error_data:error.response.data});
      } else {
        const error_data = error.response;
        console.error(">>> Error publishing DID: ", error_data);
        res.status(500).json({ error_data });
      }
    });
});


export default router;
