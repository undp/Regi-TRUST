import { useState } from "react";
import "./App.css";
import Login from "./components/Login/Login";
import Tool from "./components/Tool/Tool";

const App = () => {
  const [isLoggedIn, setLoggedIn] = useState(false);

  const handleLogin = (loginConfimed) => {
    setLoggedIn(loginConfimed);
    if (!loginConfimed) {
      var url = new URL(window.location.href);
      url.search = "";
      window.history.replaceState({}, document.title, url.href);
    }
  };

  return (
    <div className="App">
      {isLoggedIn ? (
        <Tool onLogin={handleLogin} />
      ) : (
        <Login onLogin={handleLogin} />
      )}
    </div>
  );
};

export default App;
