import {useEffect, useState } from 'react'

import Row from 'react-bootstrap/Row'
import Col from 'react-bootstrap/Col'
import Tabs from 'react-bootstrap/Tabs'
import Tab from 'react-bootstrap/Tab'
import Accordion from 'react-bootstrap/Accordion'
import Table from 'react-bootstrap/Table'
import Spinner from 'react-bootstrap/Spinner'

function ZoneDataView() {
  // TODO: Data must be received from backend dynamically.
  const [isLoading, setLoading] = useState(true)
  const [zoneDataError, setZoneDataError] = useState(false)
  const [errorMsg, setErrorMsg] = useState('')
  const [zoneData, setZoneData] = useState({ zones: [] })

  useEffect(() => {
    setLoading(true)
      console.log(`Loading data from backend.`)
      const basePath = window.location.pathname
      console.log('Detected basePath: ' + basePath)
      fetch(`${basePath === '/' ? '' : basePath}/api/zonedata`, {
        redirect: 'manual'
      })
        .then(async (res) => {
          if (res.type === 'opaqueredirect') {
            window.location.href = res.url
          } else if (res.status === 200) {
            const responseData = await res.json()
            console.log("Received axios response: ")
            console.log(responseData)
            setZoneData(responseData)
            console.log('Unsetting loading')
          } else {
            console.log('Error from zoneData Route!')
            console.log(res)
            const errorMessage = await res.text()
            console.log('Parsed error msg as: ' + errorMessage)
            setErrorMsg(`Error getting zone data:
            -> ${errorMessage}`)
            setZoneDataError(true)
          }
          setLoading(false)  
        })
  }, [])

  if (isLoading) {
    return (
      <>
        <Row className="align-items-center">
          <Col className="text-center">
            <Spinner animation="border" role="status">
              <span className='visually-hidden'>Loading...</span>
            </Spinner>
          </Col>
        </Row>
      </>
    )
  } else if (zoneDataError) {
    return (
      <>
        <Row className="align-items-center">
          <Col className="text-center">
            <span>{errorMsg}</span>
          </Col>
        </Row>
      </>
    )
  } else {
    return (
      <>
        <Row className="align-items-center">
          <Col>
            <Tabs
              defaultActiveKey={zoneData.zones[0].id}
              id="zoneTabs"
              className='mb-4'
              fill
              justify
              style={{ backgroundColor: "#e3f9ff" }}
            >
              {zoneData.zones.map((zone, index) => (
                <Tab eventKey={zone.id} title={`${zone.id} - (${zone.apex})`} key={index}>
                  <Row>
                    <Accordion>
                      {zone.schemes.map((scheme, index) => (
                        <Accordion.Item eventKey={index} key={index}>
                          <Accordion.Header>
                            {scheme.name}
                          </Accordion.Header>
                          <Accordion.Body>
                            <Table striped bordered>
                              <thead>
                                <tr>
                                  <th>#</th>
                                  <th>Subscheme Name</th>
                                  <th>Trustlist DiD</th>
                                </tr>
                              </thead>
                              <tbody>
                                {scheme.subSchemes.map((scheme, index) => (
                                  <tr key={index}>
                                    <td>{index + 1}</td>
                                    <td>{scheme.subscheme}</td>
                                    <td>{scheme.trustListDid}</td>
                                  </tr>
                                ))}
                              </tbody>
                            </Table>
                          </Accordion.Body>
                        </Accordion.Item>
                      ))
                      }
                    </Accordion>
                  </Row>
                </Tab>
              ))

              }
            </Tabs>
          </Col>
        </Row>
      </>)
  }
}

export default ZoneDataView
