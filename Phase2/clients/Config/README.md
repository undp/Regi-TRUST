# Configuration

## Overview

When an enrollment request is approved, the Realm Administrator should receive an email notification of the approval as a notice to create an account for this TSP (Third-Party Service Provider). The Realm Administrator will be a role or group in Keycloak. For now, this role/group is represented by the email address specified in `/Config/config.json` within the `Admin.email` field.

## reCAPTCHA Configuration

To ensure proper submission of the enrollment form, reCAPTCHA verification is required. The client hostname must not be `localhost` and needs to be configured in the reCAPTCHA admin console. Since we are now using a new domain, we will need to configure Keycloak to use this domain as well.

### Steps to Deploy with a Temporary ngrok Site

1. **Install Dependencies**

   If you haven't already, install the necessary dependencies:

   ```bash
   npm install

2. **Start the Project**

   Start the project using

   ```bash
   npm start

3. **Run ngrok**

   Open another terminal and run:

   ```bash
   ngrok http http://localhost:1337

   ngrok will deploy the site to a temporary domain. Find this domain in the terminal output and open it to view the deployed project.

4. **Configure reCAPTCHA**

   Go to the reCAPTCHA admin console.
   Create a new project.
   Configure the domain to ngrok's temp domain output (without the protocol).
   Copy and paste the reCAPTCHA site key and secret into the reCAPTCHA configuration within `/Config/config.json.`

5. **Configure Keycloak**

   Go to the Keycloak admin console.
   Navigate to Clients > GCCN
   Enter Ngrok's temp endpoint into the 'Root URL'
   Add this temp endpoint as a 'Web Origin' as well.

## Email Notifications
Upon approval of an enrollment request, the Realm Administrator will receive an email notification. For testing purposes for now, this email will just be sent to the address specified in the Admin.email field of `/Config/config.json`

## Summary
Ensure reCAPTCHA and Keycloak is properly configured with the ngrok temporary domain.
Notifications for enrollment request approvals are sent to the email address specified in Admin.email for testing.
