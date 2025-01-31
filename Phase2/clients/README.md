# Web Front End
_Last updated: January 2025_

The web front end provides a User Interface for managing the Trust List.

## Functionality Supported
- **Enrollment**: Provides a form to allow prospective TSP represntatives to submit an enrollment request.  The onboarding staff at TSPA can review and acknowledge or defer the requests.
- **Network Entry Managment**: Authrized TSP staff can submit Trust List entries with multiple services.  The entries can be reviewed, approved and published by TSP Reviewers.
- **Versioning of Trust List**: System provides ability to edit Trust List entries.

## Tech Stack
At the time of this commit Node v.22.5.1 is used.

## Installation and Deployment
Node application deployment for the chosen platform.
In test environment is is running as a systemd service, exposed to internet via an nginx server.

## Folder Structure
```
/c:/[Project Directory]
├── README.md
├── /Config/
    └── config.json           **Configurations and constants for other services i.e. reCaptcha, email system, etc
    └── keycloak.json         **Configurations for keycloak
├── /Auth/
    └── keycloak.json         *Keycloak API implementations
├── /data/
    └── formFields/           *Schema for form building (mapped using ./views/forms/includes/formFieldsRenderer.pug)
    └── MongoDB/              *Mongo configs and schema definitions
    └── submissionFormatting/ *Form submission helpers
    └── submissions/          
    └── TRAIN/                *TRAIN API implementations
├── /notifications/           * Holds logic for sending email notifications and the email templates to use
├── /routes/                  * Controller. Holds directories of main paths and thier child endpoints
├── /Views/                   
```

## Important Notes

### Enrollment
- Enrollment requires reCAPTCHA Configuration and therefore requires a few more steps to set up.

## Contact / Acknowledgments
 [**Symsoft Solutions**](https://symsoftsolutions.com). For any queries or support, please contact our team.

### Contributors

- [Abdul Farooqui](https://github.com/abdulFarooqui)
- [John Walker](https://github.com/jtwalker2000)
- [Savita Farooqui](https://github.com/SavitaFarooqui)