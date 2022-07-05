package com.nashtech.ecommerce.service.impl;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.nashtech.ecommerce.dto.request.RequestProductDto;
import com.nashtech.ecommerce.dto.response.ResponseListProduct;
import com.nashtech.ecommerce.dto.response.ResponseMessageDto;
import com.nashtech.ecommerce.dto.response.ResponseProductDto;
import com.nashtech.ecommerce.entity.Product;
import com.nashtech.ecommerce.exception.ResourceNotFoundException;
import com.nashtech.ecommerce.repository.ProductRepository;
import com.nashtech.ecommerce.repository.RatingRepository;
import com.nashtech.ecommerce.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

	private ProductRepository productRepository;
	private ModelMapper modelMapper;
	private RatingRepository ratingRepository;

	@Autowired
	public ProductServiceImpl(ProductRepository productRepository,
			ModelMapper modelMapper,RatingRepository ratingRepository) {
		super();
		this.productRepository = productRepository;
		this.modelMapper = modelMapper;
		this.ratingRepository=ratingRepository;
	}

	@Override
	public ResponseListProduct findAllProduct( String productName,int status,int page, int size) {
		Pageable pageable = PageRequest.of(page-1, size);
		Page<Product> productPage =null;
		if (status==-1) {
			if (StringUtils.hasText(productName)) {
				productPage=productRepository.findByProductNameContaining(productName, pageable);
			}else {
				 productPage = productRepository.findAll(pageable);
			}
		}else if(status==1||status==0){
			if (StringUtils.hasText(productName)) {
				productPage=productRepository.findByStatusAndProductNameContaining(productName, status, pageable);
			}else {
				 productPage = productRepository.findByStatus(status, pageable);
			}
			
		}
		List<Product> products=productPage.getContent();
		List<ResponseProductDto> responseProductDtos= modelMapper.map(products,
				new TypeToken<List<ResponseProductDto>>() {
				}.getType());
		responseProductDtos.forEach(responseProductDto -> {
			responseProductDto.setAvgScores(ratingRepository.findAVGRatingOfProduct(responseProductDto.getId()).orElse(0f));
		});
		
		return ResponseListProduct.builder().totalProduct(productPage.getTotalElements())
				.perPage(productPage.getNumberOfElements())
				.currentPage(productPage.getNumber()+1)
				.lastPage(productPage.getTotalPages())
				.productDtos(responseProductDtos)
				.build();
	}

	@Override
	public ResponseProductDto findProductDtoById(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Did not find product with id = " + id));
		ResponseProductDto responseProductDto=modelMapper.map(product, ResponseProductDto.class);
		responseProductDto.setAvgScores(ratingRepository.findAVGRatingOfProduct(responseProductDto.getId()).orElse(0f));
		return responseProductDto;
	}

	@Override
	public ResponseProductDto createProduct(RequestProductDto productDto) {
		productDto.setCreatedAt(new Date());
		productDto.setModifiedAt(new Date());
		productDto.setStatus(1);
		Product product = productRepository
				.save(modelMapper.map(productDto, Product.class));
		return modelMapper.map(product, ResponseProductDto.class);
	}

	@Override
	public ResponseProductDto updateProduct(RequestProductDto productDto) {
		Product product = productRepository.findById(productDto.getId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Did not find product has id = " + productDto.getId()));
		modelMapper.map(productDto, product);
		product = productRepository.save(product);
		return modelMapper.map(product, ResponseProductDto.class);
	}

	@Override
	public ResponseMessageDto deleteProduct(Long id) {
		productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
				"Did not find product with id = " + id));
		productRepository.deleteById(id);
		return new ResponseMessageDto(HttpStatus.ACCEPTED,
				"Deleted product with id= " + id, LocalDateTime.now());
	}

}
