# General Operations:
For general operation instructions such as Backups, Logs etc, check the general operations manual: [Operations Manual](https://gitlab.eclipse.org/eclipse/xfsc/train/TRAIN-Documentation/-/tree/main/concepts/operation?ref_type=heads)

# TDZM Specific Operations information

The TDZM Component generally does not require much related to operations. It is mainly managed through the TFM component and should not be used interactively by a human operator. There is one detail to watch out for:

## DNS Key Rollover:

To rollover the DNSSEC Keys it is necessary to completely remove the existing TDZM deployment. 
To do this follow the steps in the general operations manual: [DNS Key Rollover instructions](https://gitlab.eclipse.org/eclipse/xfsc/train/TRAIN-Documentation/-/tree/main/concepts/operation?ref_type=heads#tdzm-specific-operation-actions)

To know more about the reasons for a required DNS Key rollover check the Security Documentation here: [Security Concept for TDZM](https://gitlab.eclipse.org/eclipse/xfsc/train/TRAIN-Documentation/-/tree/main/concepts/security?ref_type=heads#zone-manager-tspa)

# REST API
The Rest API is documented in [ZM_swagger.yaml](./ZM_swagger.yaml)