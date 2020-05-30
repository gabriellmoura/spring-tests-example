package com.gabriel.demo.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabriel.demo.entity.ProductEntity;
import com.gabriel.demo.service.ProductService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

	@MockBean
	private ProductService service;

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("GET /product/1 - Found")
	void testGetProductByIdFound() throws Exception {
		ProductEntity mockProduct = new ProductEntity(1, "Product Name", 10, 1);
		doReturn(Optional.of(mockProduct)).when(service).findById(1);

		mockMvc.perform(get("/product/{id}",
				1))
				// validating response OK
				.andExpect(status()
						.isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				// validating header
				.andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
				.andExpect(header().string(HttpHeaders.LOCATION, "/product/1"))
				// validating fields
				.andExpect(jsonPath("$.id", is(mockProduct.getId())))
				.andExpect(jsonPath("$.name", is(mockProduct.getName())))
				.andExpect(jsonPath("$.quantity", is(mockProduct.getQuantity())))
				.andExpect(jsonPath("$.version", is(mockProduct.getVersion())));
	}
	
	@Test
	@DisplayName("GET /product/1 - Not Found")
	public void testGetProductByIdNotFound() throws Exception {
		doReturn(Optional.empty()).when(service).findById(1);
		mockMvc.perform(get("/product/{id}", 1)).andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("POST /product/ - Create")
	public void testCreateProduct() throws Exception {
		ProductEntity postProduct = new ProductEntity(null, "Product Name", 10, 1);
		ProductEntity mockProduct = new ProductEntity(1, "Product Name", 10, 1);

		doReturn(mockProduct).when(service).saveOrUpdate(Mockito.any());

		mockMvc.perform(post("/product").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.IF_MATCH, 1)
				.content(asJsonString(
						postProduct)))
				// validating response Created
				.andExpect(status().isCreated()).andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				// validating header
				.andExpect(header().string(HttpHeaders.ETAG,
						"\"1\""))
				.andExpect(header().string(HttpHeaders.LOCATION, "/product/1"))
				// validating fields
				.andExpect(jsonPath("$.id", is(mockProduct.getId())))
				.andExpect(jsonPath("$.name", is(mockProduct.getName())))
				.andExpect(jsonPath("$.quantity", is(mockProduct.getQuantity())))
				.andExpect(jsonPath("$.version", is(mockProduct.getVersion())));
	}

	@Test
	@DisplayName("PUT /product/1 - Success")
	public void testUpdateProduct() throws Exception {
		ProductEntity putProduct = new ProductEntity(null, "Product Name", 10, null);
		ProductEntity mockProduct = new ProductEntity(1, "Product Name", 10, 1);

		doReturn(Optional.of(mockProduct)).when(service).findById(1);
		doReturn(mockProduct).when(service).saveOrUpdate(Mockito.any());

		mockMvc.perform(put("/product/{id}", 1).contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.IF_MATCH,
						1)
				.content(asJsonString(putProduct)))
				// validating response OK
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				// validating header
				.andExpect(header().string(HttpHeaders.ETAG, "\"2\""))
				.andExpect(header().string(HttpHeaders.LOCATION, "/product/1"))
				// validating fields
				.andExpect(jsonPath("$.id", is(mockProduct.getId())))
				.andExpect(jsonPath("$.name", is(mockProduct.getName())))
				.andExpect(jsonPath("$.quantity", is(mockProduct.getQuantity())))
				.andExpect(jsonPath("$.version", is(2)));
	}

	@Test
	@DisplayName("PUT /product/1 - Conflicted")
	public void testUpdateConflictedProductVersion() throws Exception {
		ProductEntity putProduct = new ProductEntity(null, "Product Name", 10, null);
		ProductEntity mockProduct = new ProductEntity(1, "Product Name", 10, 2);

		doReturn(Optional.of(mockProduct)).when(service).findById(1);
		doReturn(mockProduct).when(service).saveOrUpdate(Mockito.any());

		mockMvc.perform(put("/product/{id}", 1).contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.IF_MATCH, 1)
				.content(asJsonString(putProduct)))
				// validating response Conflict
				.andExpect(status().isConflict());
	}

	@Test
	@DisplayName("PUT /product/1 - Not Found")
	public void testUpdateProductNotFound() throws Exception {
		ProductEntity putProduct = new ProductEntity(null, "Product Name", 10, null);
		doReturn(Optional.empty()).when(service).findById(1);

		mockMvc.perform(put("/product/{id}", 1).contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.IF_MATCH, 1)
				.content(asJsonString(putProduct)))
				// validating response Conflict
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("DELETE /product/1 - Success")
	public void testDeleteSuccess() throws Exception {
		ProductEntity mockProduct = new ProductEntity(1, "Product Name", 10, 1);
		
		doReturn(Optional.of(mockProduct)).when(service).findById(1);
		
		mockMvc.perform(delete("/product/{id}", 1)).andExpect(status().isOk());
		verify(service, times(1)).deleteById(1);
	}

	@Test
	@DisplayName("DELETE /product/1 - Not Found")
	public void testDeleteNotFound() throws Exception {

		doReturn(Optional.empty()).when(service).findById(1);

		mockMvc.perform(delete("/product/{id}", 1)).andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("DELETE /product/1 - Failed")
	public void testDeleteFailed() throws Exception {
		ProductEntity mockProduct = new ProductEntity(1, "Product Name", 10, 1);

		doReturn(Optional.of(mockProduct)).when(service).findById(1);
		doThrow(RuntimeException.class).when(service).deleteById(Mockito.anyInt());

		mockMvc.perform(delete("/product/{id}", 1)).andExpect(status().isInternalServerError());
	}

	private String asJsonString(final Object object) throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(object);
	}
}
