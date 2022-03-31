/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sd4.service;

import com.sd4.model.Category;
import com.sd4.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author sagarkandel
 */
@Service
public class CategoryService {
      @Autowired
    private CategoryRepository CategoryRepo;
 
     public Optional<Category> findOne(Long id) {
        return CategoryRepo.findById(id);
    }

    public List<Category> findAll() {
        return (List<Category>) CategoryRepo.findAll();
    }

    public long count() {
        return CategoryRepo.count();
    }

    public void deleteByID(long ID) {
        CategoryRepo.deleteById(ID);
    }

    public void saveBrewery(Category c) {
        CategoryRepo.save(c);
    }

}
