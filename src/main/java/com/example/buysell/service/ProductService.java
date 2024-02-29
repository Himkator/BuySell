package com.example.buysell.service;

import com.example.buysell.models.Image;
import com.example.buysell.models.Product;
import com.example.buysell.models.User;
import com.example.buysell.repositories.ProductRepository;
import com.example.buysell.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<Product> listProducts(String title){
        if(title!=null) return productRepository.findByTitle(title);
        return productRepository.findAll();
    }

    public void saveProduct(Principal principal,Product product, MultipartFile file1, MultipartFile file2, MultipartFile file3){
        product.setUser(getUserByPrincipal(principal));
        Image image1;
        Image image2;
        Image image3;
        if(file1.getSize() !=0) {
            try {
                image1=toImageEntity(file1);
                image1.setPreviewImage(true);
                product.addImage(image1);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(file2.getSize() !=0) {
            try {
                image2=toImageEntity(file2);
                product.addImage(image2);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(file3.getSize() !=0) {
            try {
                image3=toImageEntity(file3);
                product.addImage(image3);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        log.info("Saving new Product. Title {}; Author email: {}", product.getTitle(), product.getUser().getEmail());
        Product productFromDb=productRepository.save(product);
        productFromDb.setPreviewImageId(productFromDb.getImages().get(0).getId());
        productRepository.save(product);
    }

    public User getUserByPrincipal(Principal principal) {
        if(principal==null)return new User();
        return userRepository.findByEmail(principal.getName());
    }

    private Image toImageEntity(MultipartFile file1) throws IOException {
        Image image=new Image();
        image.setName(file1.getName());
        image.setOriginalFileName(file1.getOriginalFilename());
        image.setContentType(file1.getContentType());
        image.setSize(file1.getSize());
        image.setBytes(file1.getBytes());
        return image;
    }

    public void deleteProduct(User user,long id){
        Product product = productRepository.findById(id)
                .orElse(null);
        if (product != null) {
            if (product.getUser().getId().equals(user.getId())) {
                productRepository.delete(product);
                log.info("Product with id = {} was deleted", id);
            } else {
                log.error("User: {} haven't this product with id = {}", user.getEmail(), id);
            }
        } else {
            log.error("Product with id = {} is not found", id);
        }
    }
    public Product getProductById(Long id){
        return productRepository.findById(id).orElse(null);
    }
}
