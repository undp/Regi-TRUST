import { useState } from "react";
import axios from "axios";
import PropTypes from "prop-types";

const Tool = ({ onLogin }) => {
  const [publicationResponse, setPublicationResponse] = useState("");
  const [tfName, setTfName] = useState("");
  const [otherTfName, setOtherTfName] = useState("");
  const [tfNameForDID, setTfNameForDID] = useState("");
  const [did, setDID] = useState("");

  const handleAddTF = async () => {
    if (tfName === "") {
      return;
    }
    const tfPointers = [tfName];
    if (otherTfName !== "") {
      tfPointers.push(otherTfName);
    }
    axios
      .post(`/api/addtf`, {
        tfName,
        tfPointers
      })
      .then((res) => {
        const displayResponse = `The following Trust Frameworks were added successfully:
          ${tfPointers}
          See console log for details
          
          `;
        console.log(`>>> TRUST FRAMEWORK addition successful:
          ${res.data}`);
        setPublicationResponse(displayResponse);
      })
      .catch((error) => {
        setPublicationResponse(
          `Error during TF publication. See console log for details.`
        );
        if (error.response) {
          // server responded with something different from 2xx
          console.error('Error in response from server:', error.response.data);
        } else if (error.request) {
          console.error('No response from server:', error.request);
        } else {
          console.error('Request error:', error.message);
        }        
      });
  };

  const hanldeAddDID = async () => {
    if ((tfNameForDID === "") | (did === "")) {
      return;
    }
    console.log("calling publish did");

    axios
      .post(
        `/api/add-did`,
        { did, tfNameForDID },
      )
      .then((res) => {
        const displayResponse = `${publicationResponse} 
          
        DID ${did} added succesfully to ${tfNameForDID}
        See console log for details `
        console.log(`>>> DID successfully addded:
        ${res.data}`);
        setPublicationResponse(displayResponse);
      })
      .catch((error) => {
        setPublicationResponse(`${publicationResponse} 
          
          DID ${did} could not be added to ${tfNameForDID}
          See console log for details `);
        console.error(">>> Adding DID rcould not be done: ", error);
      });
  };

  const handleInputChange = (e) => {
    switch (e.target.name) {
      case "publishTF-tfName":
        setTfName(e.target.value);
        break;
      case "publishTF-otherTFname":
        setOtherTfName(e.target.value);
        break;
      case "publishDID-tfName":
        setTfNameForDID(e.target.value);
        break;
      case "publishDID-DID":
        setDID(e.target.value);
        break;
      default:
        break;
    }
  };

  const handleLogout = () => {
    onLogin(false);
    console.log("logging out to: " + `/api/logout`);
    axios
      .post(`/api/logout`)
      .then(() => {
        console.log("loggedout successfully");
        onLogin(false);
      })
      .catch((error) => {
        console.error("Error calling logout backend", error);
      });
  };

  return (
    <div className="container-tool">
      <div className="column form centered-content ">
        {/* <img src="/images/logo.svg" /> */}
        <h5>XFSC Train Trust Framework Manager</h5>
        <h1 className="login-hero-title layout">Trust Framework setup</h1>
        <div className="custom-input-container full-width">
          <label htmlFor="">name</label>
          <input
            type="text"
            id="publishTF-tfName"
            name="publishTF-tfName"
            placeholder="Trust Framework name"
            onChange={handleInputChange}
            title="e.g. federation.example.com"
          />
        </div>
        <p>
          Optionally, add pointers to self and other trusted Trust Frameworks
        </p>
        <div className="custom-input-container full-width">
          <label htmlFor="">name</label>
          <input
            type="text"
            id="publishTF-otherTFname"
            name="publishTF-otherTFname"
            placeholder="Other trusted Trust Framework name"
            onChange={handleInputChange}
            title="for multiple additional TF use commas as separator. e.g. org1.example.com,org2.example"
          />
        </div>
        <button className="outline-button" onClick={handleAddTF}>
          Add Trust Frameworks
        </button>
        <div className="custom-input-container full-width">
          <label htmlFor="">name</label>
          <input
            type="text"
            id="publishDID-tfName"
            name="publishDID-tfName"
            placeholder="Trust Framework name"
            onChange={handleInputChange}
          />
        </div>
        <div className="custom-input-container full-width">
          <label htmlFor="">DID</label>
          <input
            type="text"
            id="publishDID-DID"
            name="publishDID-DID"
            placeholder="DID"
            onChange={handleInputChange}
          />
        </div>
        <button className="outline-button" onClick={hanldeAddDID}>
          Publish DID
        </button>
      </div>
      <div className="column form centered-content">
        <label htmlFor="">Trust Framework configuration result</label>
        <div className="configuration-result">{publicationResponse}</div>
        <button onClick={handleLogout}>logout</button>
      </div>
    </div>
  );
};

Tool.propTypes = {
  onLogin: PropTypes.func.isRequired,
};

export default Tool;
