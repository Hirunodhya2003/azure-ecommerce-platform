package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.category.CategoryRequest;
import com.ecommerce.ecommerce_backend.dto.category.CategoryResponse;
import com.ecommerce.ecommerce_backend.exception.CategoryAlreadyExistsException;
import com.ecommerce.ecommerce_backend.exception.CategoryNotFoundException;
import com.ecommerce.ecommerce_backend.model.Category;
import com.ecommerce.ecommerce_backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;


    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }


    // GET ALL CATEGORIES
    public List<CategoryResponse> getAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // GET CATEGORY BY ID
    public CategoryResponse getCategoryById(Long id) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(
                                () -> new CategoryNotFoundException(
                                        "Category not found with id: " + id
                                )
                        );

        return mapToResponse(category);
    }


    // CREATE CATEGORY
    public CategoryResponse createCategory(CategoryRequest request) {

        if (categoryRepository.existsByNameIgnoreCase(
                request.getName()
        )) {

            throw new CategoryAlreadyExistsException(
                    "Category already exists with name: "
                            + request.getName()
            );
        }


        Category category = new Category();

        category.setName(
                request.getName()
        );

        category.setDescription(
                request.getDescription()
        );

        category.setActive(true);


        return mapToResponse(
                categoryRepository.save(category)
        );
    }


    // UPDATE CATEGORY
    public CategoryResponse updateCategory(
            Long id,
            CategoryRequest request
    ) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(
                                () -> new CategoryNotFoundException(
                                        "Category not found with id: " + id
                                )
                        );


        // Check duplicate category name
        if (categoryRepository.existsByNameIgnoreCase(
                request.getName()
        )
                && !category.getName().equalsIgnoreCase(
                request.getName()
        )) {

            throw new CategoryAlreadyExistsException(
                    "Category already exists with name: "
                            + request.getName()
            );
        }


        category.setName(
                request.getName()
        );

        category.setDescription(
                request.getDescription()
        );


        return mapToResponse(
                categoryRepository.save(category)
        );
    }


    // DELETE CATEGORY
    public void deleteCategory(Long id) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(
                                () -> new CategoryNotFoundException(
                                        "Category not found with id: " + id
                                )
                        );


        categoryRepository.delete(category);
    }


    // ENTITY → RESPONSE DTO
    private CategoryResponse mapToResponse(
            Category category
    ) {

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getActive()
        );
    }
}

