// auth.js
let bearerToken = null;
let refreshToken = null;

export function getBearerToken() {
  return bearerToken;
}

export function setBearerToken(token) {
  bearerToken = token;
}

export function getRefreshToken() {
  return refreshToken;
}

export function setRefreshToken(token) {
    refreshToken = token;
}