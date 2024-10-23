package eu.xfsc.train.tspa.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import eu.xfsc.train.tspa.services.VCServiceImpl;
import eu.xfsc.train.tspa.utils.VCUtil;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class TrustListPublicationControllerTest {

	@Autowired
	private MockMvc mvc;
	
	@MockBean
	VCServiceImpl vcServiceImpl;
	
	@MockBean
	VCUtil vcUtil;
	

	@Test
	@WithMockUser(authorities = "enrolltf")
	@Order(1)
	public void initXMLTrustListPub() throws Exception {
		
		
		// Arrange
		String pathVariable = "junit-test-gaia-x.testtrain.trust-scheme.de";
		String xmlTL = IOUtils.toString(this.getClass().getResourceAsStream("/templates/trust-list.xml"), "UTF-8");



		// Act
		int resultStatusCode = mvc
				.perform(MockMvcRequestBuilders.put("/tspa/v1/init/xml/" + pathVariable + "/trust-list")
						.contentType(MediaType.APPLICATION_XML_VALUE).content(xmlTL))
				.andReturn().getResponse().getStatus();

		// Assert
		assertEquals(HttpStatus.CREATED.value(), resultStatusCode);

	}

	@Test
	@WithMockUser(authorities = "enrolltf")
	@Order(2)
	public void initJSONTrustListPub() throws Exception {

		// Arrange
		String pathVariable = "gxfs2-prep.train.trust-scheme.de";

		String jsonTL = IOUtils.toString(this.getClass().getResourceAsStream("/templates/trust-list.json"), "UTF-8");
		// Act
		int resultStatusCode = mvc
				.perform(MockMvcRequestBuilders.put("/tspa/v1/init/json/" + pathVariable + "/trust-list")
						.contentType(MediaType.APPLICATION_JSON_VALUE).content(jsonTL))
				.andReturn().getResponse().getStatus();

		// Assert
		assertEquals(HttpStatus.CREATED.value(), resultStatusCode);

	}

	@Test
	@WithMockUser(authorities = "enrolltf")
	@Order(3)
	public void tspPublication() throws Exception {
		// Arrange
		String pathVariable = "junit-test-gaia-x.testtrain.trust-scheme.de";

		String tsp = IOUtils.toString(this.getClass().getResourceAsStream("/templates/TSP.json"), "UTF-8");
		// Act
		int resultStatusCode = mvc
				.perform(MockMvcRequestBuilders.put("/tspa/v1/" + pathVariable + "/trust-list/tsp")
						.contentType(MediaType.APPLICATION_JSON_VALUE).content(tsp))
				.andReturn().getResponse().getStatus();
		// Assert
		assertEquals(HttpStatus.CREATED.value(), resultStatusCode);
	}

	@Test
	@WithMockUser(authorities = "enrolltf")
	@Order(4)
	public void tspsPublication() throws Exception {
		// Arrange
		String pathVariable = "gxfs2-prep.train.trust-scheme.de";

		String tsps = IOUtils.toString(this.getClass().getResourceAsStream("/templates/TSPsArray.json"), "UTF-8");
		// Act
		int resultStatusCode = mvc
				.perform(MockMvcRequestBuilders.put("/tspa/v1/" + pathVariable + "/trust-list/tsp")
						.contentType(MediaType.APPLICATION_JSON_VALUE).content(tsps))
				.andReturn().getResponse().getStatus();
		// Assert
		assertEquals(HttpStatus.CREATED.value(), resultStatusCode);
	}

	@Test
	@WithMockUser(authorities = "enrolltf")
	@Order(5)
	public void updateTSP() throws Exception {
		// Arrange
		String pathVariableFrameWork = "gxfs2-prep.train.trust-scheme.de";
		String pathVariableUUIDString = "8271fcbf-0622-4415-b8b1-34ad74215dc7";

		String tsps = IOUtils.toString(this.getClass().getResourceAsStream("/templates/UpdateTSP.json"), "UTF-8");
		// Act
		int resultStatusCode = mvc
				.perform(MockMvcRequestBuilders
						.patch("/tspa/v1/" + pathVariableFrameWork + "/trust-list/tsp/" + pathVariableUUIDString)
						.contentType(MediaType.APPLICATION_JSON_VALUE).content(tsps))
				.andReturn().getResponse().getStatus();
		// Assert
		assertEquals(HttpStatus.OK.value(), resultStatusCode);
	}

	@Test
	@WithMockUser(authorities = "enrolltf")
	@Order(6)
	public void deleteTSP() throws Exception {
		// Arrange
		String pathVariableFrameWork = "gxfs2-prep.train.trust-scheme.de";
		String pathVariableUUIDString = "8271fcbf-0622-4415-b8b1-34ad74215dc8";

		// Act
		int resultStatusCode = mvc
				.perform(MockMvcRequestBuilders.delete(
						"/tspa/v1/" + pathVariableFrameWork + "/trust-list/tsp/" + pathVariableUUIDString))
				.andReturn().getResponse().getStatus();

		// Assert
		assertEquals(HttpStatus.OK.value(), resultStatusCode);
	}

	@Test
	@WithMockUser(authorities = "enrolltf")
	@Order(7)
	public void deleteTrustList() throws Exception {
		// Arrange
		String pathVariableFrameWork = "gxfs2-prep.train.trust-scheme.de";

		// Act
		int resultStatusCode = mvc
				.perform(MockMvcRequestBuilders.delete("/tspa/v1/" + pathVariableFrameWork + "/trust-list"))
				.andReturn().getResponse().getStatus();

		// Assert
		assertEquals(HttpStatus.OK.value(), resultStatusCode);
	}

	@Test
	@Order(8)
	public void getTrustList() throws Exception {
		// Arrange
		String pathVariableFrameWork = "junit-test-gaia-x.testtrain.trust-scheme.de";

		// Act
		int resultStatusCode = mvc
				.perform(MockMvcRequestBuilders.get("/tspa/v1/" + pathVariableFrameWork + "/trust-list"))
				.andReturn().getResponse().getStatus();

		// Assert
		assertEquals(HttpStatus.OK.value(), resultStatusCode);
	}

	@Test
	@Order(9)
	public void getVC() throws Exception {
		// Arrange
		String pathVariableFrameWork = "junit-test-gaia-x.testtrain.trust-scheme.de";

		// Act
		int resultStatusCode = mvc
				.perform(MockMvcRequestBuilders.get("/tspa/v1/" + pathVariableFrameWork + "/vc/trust-list"))
				.andReturn().getResponse().getStatus();

		// Assert
		assertEquals(HttpStatus.OK.value(), resultStatusCode);
	}

	@Test
	@WithMockUser(authorities = "enrolltf")
	@Order(10)
	public void deleteTrustList2() throws Exception {
		// Arrange
		String pathVariableFrameWork = "junit-test-gaia-x.testtrain.trust-scheme.de";

		// Act
		int resultStatusCode = mvc
				.perform(MockMvcRequestBuilders.delete("/tspa/v1/" + pathVariableFrameWork + "/trust-list"))
				.andReturn().getResponse().getStatus();

		// Assert
		assertEquals(HttpStatus.OK.value(), resultStatusCode);
	}
}
