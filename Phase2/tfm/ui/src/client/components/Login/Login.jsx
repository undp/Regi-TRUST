import { useEffect } from "react";
import axios from "axios";
import PropTypes from "prop-types";

const Login = ({ onLogin }) => {

  useEffect(() => {
    const urlSearchParams = new URLSearchParams(window.location.search);
    const code = urlSearchParams.get("code");
    if (code) {
      axios
        .post(`/api/authorize`, { code })
        .then((response) => {
          onLogin(response.data.allowed);
        })
        .catch((error) => {
          console.error("Error calling backend", error);
        });
    }
  });

  const handleAccessClick = async () => {
    try {
      axios
        .post(`/api/authenticate`)
        .then((res) => {
          const redirectUrl = res.data.authUrl;
          if (redirectUrl) {
            window.location.href = redirectUrl;
          }
        })
        .catch((error) => {
          console.error("Error calling backend: ", error);
        });
    } catch (error) {
      console.error("Error fetching redirect URL", error);
    }
  };

  return (
    <div className="container-login">
      <div className="column-picture">
        <img src="/images/background_waves.png" className="full-width" />
      </div>
      <div className="column form centered-content">
        {/* <img src="/images/logo.svg" /> */}
        <h5>XFSC Train Trust Framework Manager</h5>
        <h1 className="login-hero-title layout">
          Welcome to the Trust Framework configuration tool
        </h1>
        <button className="outline-button" onClick={handleAccessClick}>
          access
        </button>
      </div>
    </div>
  );
};

Login.propTypes = {
  onLogin: PropTypes.func.isRequired,
};

export default Login;
