package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.product.ProductRequest;
import com.ecommerce.ecommerce_backend.dto.product.ProductResponse;
import com.ecommerce.ecommerce_backend.exception.CategoryNotFoundException;
import com.ecommerce.ecommerce_backend.exception.ProductNotFoundException;
import com.ecommerce.ecommerce_backend.model.Category;
import com.ecommerce.ecommerce_backend.model.Product;
import com.ecommerce.ecommerce_backend.repository.CategoryRepository;
import com.ecommerce.ecommerce_backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {

        category = new Category(
                "Electronics",
                "Electronic products"
        );

        category.setId(1L);

        product = new Product(
        "Laptop",
        "LAP-001",
        new BigDecimal("150000.00"),
        10,
        "Professional laptop"
);

        product.setId(1L);
        product.setSku("LAP-001");
        product.setActive(true);
        product.setCategory(category);
    }

    @Test
    void getProductById_shouldReturnProduct() {

        // Arrange
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        // Act
        ProductResponse response =
                productService.getProductById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Laptop", response.getName());
        assertEquals("LAP-001", response.getSku());
        assertEquals(
                new BigDecimal("150000.00"),
                response.getPrice()
        );
        assertEquals(10, response.getStock());
        assertTrue(response.getActive());
        assertEquals(1L, response.getCategoryId());
        assertEquals("Electronics", response.getCategoryName());

        verify(productRepository, times(1))
                .findById(1L);
    }

    @Test
    void getProductById_shouldThrowExceptionWhenProductNotFound() {

        // Arrange
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductById(999L)
        );

        verify(productRepository, times(1))
                .findById(999L);
    }

    @Test
    void createProduct_shouldCreateProductSuccessfully() {

        // Arrange
        ProductRequest request = new ProductRequest(
                "Gaming Laptop",
                "GAM-001",
                "High performance gaming laptop",
                new BigDecimal("250000.00"),
                5,
                true,
                1L
        );

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(productRepository.existsBySkuIgnoreCase("GAM-001"))
                .thenReturn(false);

        Product savedProduct = new Product(
        "Gaming Laptop",
        "GAM-001",
        new BigDecimal("250000.00"),
        5,
        "High performance gaming laptop"
);

        savedProduct.setId(2L);
        savedProduct.setSku("GAM-001");
        savedProduct.setActive(true);
        savedProduct.setCategory(category);

        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        // Act
        ProductResponse response =
                productService.createProduct(request);

        // Assert
        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("Gaming Laptop", response.getName());
        assertEquals("GAM-001", response.getSku());
        assertEquals(
                new BigDecimal("250000.00"),
                response.getPrice()
        );
        assertEquals(5, response.getStock());
        assertTrue(response.getActive());
        assertEquals(1L, response.getCategoryId());
        assertEquals("Electronics", response.getCategoryName());

        verify(categoryRepository, times(1))
                .findById(1L);

        verify(productRepository, times(1))
                .existsBySkuIgnoreCase("GAM-001");

        verify(productRepository, times(1))
                .save(any(Product.class));
    }

    @Test
    void createProduct_shouldThrowExceptionWhenCategoryNotFound() {

        // Arrange
        ProductRequest request = new ProductRequest(
                "Gaming Laptop",
                "GAM-002",
                "High performance gaming laptop",
                new BigDecimal("250000.00"),
                5,
                true,
                999L
        );

        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                CategoryNotFoundException.class,
                () -> productService.createProduct(request)
        );

        verify(categoryRepository, times(1))
                .findById(999L);

        verify(productRepository, times(0))
                .existsBySkuIgnoreCase("GAM-002");

        verify(productRepository, times(0))
                .save(any(Product.class));
    }

    @Test
    void createProduct_shouldThrowExceptionWhenSkuAlreadyExists() {

        // Arrange
        ProductRequest request = new ProductRequest(
                "Another Laptop",
                "LAP-001",
                "Another professional laptop",
                new BigDecimal("180000.00"),
                8,
                true,
                1L
        );

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(productRepository.existsBySkuIgnoreCase("LAP-001"))
                .thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> productService.createProduct(request)
                );

        assertEquals(
                "Product already exists with SKU: LAP-001",
                exception.getMessage()
        );

        verify(categoryRepository, times(1))
                .findById(1L);

        verify(productRepository, times(1))
                .existsBySkuIgnoreCase("LAP-001");

        verify(productRepository, times(0))
                .save(any(Product.class));
    }

    @Test
    void updateProduct_shouldUpdateProductSuccessfully() {

        // Arrange
        ProductRequest request = new ProductRequest(
                "Updated Laptop",
                "LAP-001-UPDATED",
                "Updated professional laptop",
                new BigDecimal("175000.00"),
                15,
                true,
                1L
        );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(productRepository.existsBySkuIgnoreCaseAndIdNot(
                "LAP-001-UPDATED",
                1L
        )).thenReturn(false);

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductResponse response =
                productService.updateProduct(1L, request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Updated Laptop", response.getName());
        assertEquals("LAP-001-UPDATED", response.getSku());
        assertEquals(
                new BigDecimal("175000.00"),
                response.getPrice()
        );
        assertEquals(15, response.getStock());
        assertTrue(response.getActive());
        assertEquals(1L, response.getCategoryId());
        assertEquals("Electronics", response.getCategoryName());

        verify(productRepository, times(1))
                .findById(1L);

        verify(categoryRepository, times(1))
                .findById(1L);

        verify(productRepository, times(1))
                .existsBySkuIgnoreCaseAndIdNot(
                        "LAP-001-UPDATED",
                        1L
                );

        verify(productRepository, times(1))
                .save(any(Product.class));
    }

    @Test
    void updateProduct_shouldThrowExceptionWhenProductNotFound() {

        // Arrange
        ProductRequest request = new ProductRequest(
                "Updated Laptop",
                "LAP-001-UPDATED",
                "Updated professional laptop",
                new BigDecimal("175000.00"),
                15,
                true,
                1L
        );

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductNotFoundException.class,
                () -> productService.updateProduct(999L, request)
        );

        verify(productRepository, times(1))
                .findById(999L);

        verify(categoryRepository, times(0))
                .findById(1L);

        verify(productRepository, times(0))
                .existsBySkuIgnoreCaseAndIdNot(
                        "LAP-001-UPDATED",
                        999L
                );

        verify(productRepository, times(0))
                .save(any(Product.class));
    }

    @Test
    void updateProduct_shouldThrowExceptionWhenCategoryNotFound() {

        // Arrange
        ProductRequest request = new ProductRequest(
                "Updated Laptop",
                "LAP-001-UPDATED",
                "Updated professional laptop",
                new BigDecimal("175000.00"),
                15,
                true,
                999L
        );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                CategoryNotFoundException.class,
                () -> productService.updateProduct(1L, request)
        );

        verify(productRepository, times(1))
                .findById(1L);

        verify(categoryRepository, times(1))
                .findById(999L);

        verify(productRepository, times(0))
                .existsBySkuIgnoreCaseAndIdNot(
                        "LAP-001-UPDATED",
                        1L
                );

        verify(productRepository, times(0))
                .save(any(Product.class));
    }

}
