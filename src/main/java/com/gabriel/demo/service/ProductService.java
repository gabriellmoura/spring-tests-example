package com.gabriel.demo.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.gabriel.demo.entity.ProductEntity;
import com.gabriel.demo.repository.ProductRepository;

@Service
public class ProductService {

	private ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public Optional<ProductEntity> findById(Integer id) {
		return productRepository.findById(id);
	}

	public Iterable<ProductEntity> findAll() {
		return productRepository.findAll();
	}

	public ProductEntity saveOrUpdate(ProductEntity productEntity) {
		return productRepository.save(productEntity);
	}

	public void deleteById(Integer id) {
		productRepository.deleteById(id);
	}

}
