import Navbar from 'react-bootstrap/Navbar'
import Container from 'react-bootstrap/Container'
import Row from 'react-bootstrap/Row'
import Col from 'react-bootstrap/Col'

function AppBar() {
  return (
    <>
      <Navbar style={{ backgroundImage: "linear-gradient(150deg, #b900ff 5%, #000094 25% 55%, #46daff 95% 100%)"}} fixed="top">
        <Container>
        <Navbar.Brand style={{ color: "white"}}>
          <Row className='align-items-center'>
            <Col>
            <img
              alt=""
              src="/logo.png"
              height="40"
              className="d-inline-block align-top"
              style={{color: "white"}}
            />{' '}
            </Col>
            <Col>
            TRAIN Zone Visualization
            </Col>
          </Row>
        </Navbar.Brand>
        </Container>
      </Navbar>
    </>
  )
}

export default AppBar
