# Publish Trust Framework
# Example

# Trustframework publication
Step-1: 
    ` PUT-> "http://localhost:16003/tspa-service/tspa/v1/trustframework/gxfs-prep.testtrain.trust-scheme.de" `

    request body: "application/json"
        example: {
                    "schemes": ["gxfs-prep.testtrain.trust-scheme.de"]
                 }

# URI(did) record publication for the framework 'gxfs-prep.testtrain.trust-scheme.de'
Step-2:
    `PUT-> "http://localhost:16003/tspa-service/tspa/v1/gxfs-prep.testtrain.trust-scheme.de/did"`

    request body: "application/json"
        example: {
                    "did":"did:web:essif.iao.fraunhofer.de"   
                 }

        Note: We can publish all did methods that are mantioned in the 'application.yml',for did:web method well-known varification is also performed.

# Delete URI(did) record for the framework 'gxfs-prep.testtrain.trust-scheme.de'
Step-3:
    `DELETE-> "http://localhost:16003/tspa-service/tspa/v1/gxfs-prep.testtrain.trust-scheme.de/did"`

# Delete Trustframework
Step-4:
    `DELETE-> "http://localhost:16003/tspa-service/tspa/v1/trustframework/gxfs-prep.testtrain.trust-scheme.de"`
