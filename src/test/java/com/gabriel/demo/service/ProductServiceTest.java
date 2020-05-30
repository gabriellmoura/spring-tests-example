package com.gabriel.demo.service;

import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.gabriel.demo.entity.ProductEntity;
import com.gabriel.demo.repository.ProductRepository;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ProductServiceTest {

	@Autowired
	private ProductService service;

	@MockBean
	private ProductRepository repository;

	@Test
	@DisplayName("Test findById Success")
	public void findByIdSuccess() {
		ProductEntity mockProduct = new ProductEntity(1, "Product Name", 10, 1);
		doReturn(Optional.of(mockProduct)).when(repository).findById(1);

		Optional<ProductEntity> returnedProduct = service.findById(1);

		Assertions.assertTrue(returnedProduct.isPresent(), "Product is found.");
		Assertions.assertSame(mockProduct, returnedProduct.get(), "Product still the same.");
	}

	@Test
	@DisplayName("Test findById Not Found")
	public void findByIdNotFound() {
		doReturn(Optional.empty()).when(repository).findById(1);

		Optional<ProductEntity> returnedProduct = service.findById(1);

		Assertions.assertFalse(returnedProduct.isPresent(), "Product was not found.");
	}

	@Test
	@DisplayName("Test findAll Success")
	public void findAllSuccess() {
		ProductEntity mockProduct = new ProductEntity(1, "Product Name", 10, 1);
		ProductEntity mockProduct2 = new ProductEntity(2, "Product Name 2", 5, 1);

		doReturn(List.of(mockProduct, mockProduct2)).when(repository).findAll();
		Iterable<ProductEntity> returnedProducts = service.findAll();

		Assertions.assertNotNull(returnedProducts.iterator(), "Checking return is not null");
	}

	@Test
	@DisplayName("Test save successfully")
	public void testSave() {
		ProductEntity mockProduct = new ProductEntity(1, "Product Name", 10, 1);
		doReturn(mockProduct).when(repository).save(Mockito.any());

		ProductEntity returnedProduct = service.saveOrUpdate(mockProduct);

		Assertions.assertNotNull(returnedProduct, "Product is found.");
		Assertions.assertEquals(1, returnedProduct.getVersion().intValue(), "Product version is 1.");
	}
}
