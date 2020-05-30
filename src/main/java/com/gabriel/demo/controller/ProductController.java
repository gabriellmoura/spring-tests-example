package com.gabriel.demo.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.gabriel.demo.entity.ProductEntity;
import com.gabriel.demo.service.ProductService;
@RestController
public class ProductController {

	private static final Logger logger = LogManager.getLogger(ProductController.class);

	private ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping("/product/{id}")
	public ResponseEntity<?> getProduct(@PathVariable Integer id) {
		return productService.findById(id).map(product -> {
			return findProductById(product);
		}).orElse(ResponseEntity.notFound().build());

	}

	private ResponseEntity<?> findProductById(ProductEntity product) {
		try {
			return ResponseEntity.ok().eTag(Integer.toString(product.getVersion()))
					.location(new URI("/product/" + product.getId())).body(product);
		} catch (URISyntaxException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/products")
	public Iterable<ProductEntity> getProducts() {
		return productService.findAll();
	}

	@PostMapping("/product")
	public ResponseEntity<?> createProduct(@RequestBody ProductEntity product) {
		logger.info("Creating new product {}", product);
		
		ProductEntity newProduct = productService.saveOrUpdate(product);
		try {
			return ResponseEntity.created(new URI("/product/" + newProduct.getId()))
					.eTag(Integer.toString(newProduct.getVersion())).body(newProduct);
		} catch (URISyntaxException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PutMapping("/product/{id}")
	public ResponseEntity<?> updateProduct(@PathVariable Integer id,
			@RequestBody ProductEntity product,
			@RequestHeader("If-Match") Integer ifMatch) {
		logger.info("Updating Product {} with id {}", product, id);

		Optional<ProductEntity> existingProduct = productService.findById(id);

		return existingProduct.map(p -> {
			logger.info("Product with id {} has a version {}. Update is for If-Match {}", id, p.getVersion(), ifMatch);
			if (!p.getVersion().equals(ifMatch)) {
				return ResponseEntity.status(HttpStatus.CONFLICT).build();
			}

			p.setName(product.getName());
			p.setQuantity(product.getQuantity());
			p.setVersion(p.getVersion() + 1);

			logger.info("Updating Product {}", p);

			if (productService.saveOrUpdate(p) != null) {
				return findProductById(p);
			} else {
				return ResponseEntity.notFound().build();
			}
		}).orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/product/{id}")
	public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {

		logger.info("Deleting the Product with ID {}", id);

		Optional<ProductEntity> existingProduct = productService.findById(id);

		return existingProduct.map(p -> {
			try {
				productService.deleteById(id);
				return ResponseEntity.ok().build();
			} catch (Exception e) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}
		}).orElse(ResponseEntity.notFound().build());
	}
}
