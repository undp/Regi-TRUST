import Container from 'react-bootstrap/Container'
import AppBar from './components/AppBar'
import ZoneDataView from './views/ZoneDataView'
import './App.css'

function App() {

  return (
    <>
      <AppBar></AppBar>
      <Container style={{ height: "85vh", marginTop:"15vh" }}>
        <ZoneDataView/>
      </Container>
    </>
  )
}

export default App