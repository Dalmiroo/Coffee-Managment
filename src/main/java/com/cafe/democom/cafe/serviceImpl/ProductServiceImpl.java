package com.cafe.democom.cafe.serviceImpl;

import com.cafe.democom.cafe.JWT.JwtFilter;
import com.cafe.democom.cafe.POJO.Category;
import com.cafe.democom.cafe.POJO.Product;
import com.cafe.democom.cafe.constants.CafeConstants;
import com.cafe.democom.cafe.dao.ProductDao;
import com.cafe.democom.cafe.service.ProductService;
import com.cafe.democom.cafe.utils.CafeUtils;
import com.cafe.democom.cafe.wrapper.ProductWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

 @Autowired
 ProductDao productDao;

 @Autowired
 JwtFilter jwtFilter;

    @Override
    public ResponseEntity<String> addNewProduct(Map<String, String> requestMap) {
        try {
           if(jwtFilter.isAdmin()) {
               if(validateProductMap(requestMap, false)) {
                  productDao.save(getProductFromMap(requestMap,false)); //es false porque no es un update
                   return CafeUtils.getResponseEntity("producto agregado correctamente", HttpStatus.OK);
               }
               return CafeUtils.getResponseEntity(CafeConstants.Invalid_Data, HttpStatus.BAD_REQUEST);
           } else {
               return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS,HttpStatus.UNAUTHORIZED);
           }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
         return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }


    private boolean validateProductMap(Map<String, String> requestMap, boolean validateId) {
        if(requestMap.containsKey("name")) {
            if(requestMap.containsKey("id") && validateId) {
                return true;
            } else if(!validateId) {
                return true;
            }

        }
        return false;
    }

    private Product getProductFromMap(Map<String, String> requestMap, boolean isAdd) {
        Category category = new Category();
        category.setId(Integer.parseInt(requestMap.get("categoryId")));
        Product product = new Product();
        if(isAdd) {
            product.setId(Integer.parseInt(requestMap.get("id")));
        } else {
            product.setStatus("true");
        }
        product.setCategory(category);
        product.setName(requestMap.get("name"));
        product.setDescription(requestMap.get("description"));
        product.setPrice(Integer.parseInt(requestMap.get("price")));

        return product;
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getAllProducts() {
        try {
         return new ResponseEntity<>(productDao.getAllProducts(),HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateProduct(Map<String, String> requestMap) {
        try {
           if(jwtFilter.isAdmin()) {
                if(validateProductMap(requestMap,true)) {
                  Optional<Product> optional = productDao.findById(Integer.parseInt(requestMap.get("id")));
                  if(!optional.isEmpty()) {
                      Product product = getProductFromMap(requestMap, true); //takes all requestmap que le pasamos, por eso se actualiza cualquier campo
                      product.setStatus(optional.get().getStatus());
                      productDao.save(product);
                      return CafeUtils.getResponseEntity("product updated sucessfully", HttpStatus.OK);

                  }
                   else {
                       return CafeUtils.getResponseEntity("product id doesnt exist", HttpStatus.BAD_REQUEST);
                  }
                } else {
                    return CafeUtils.getResponseEntity(CafeConstants.Invalid_Data,HttpStatus.BAD_REQUEST);
                }

           }
           else {
               return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
           }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteProduct(Integer id) {
        try {
           if(jwtFilter.isAdmin()) {
             Optional<Product> optional = productDao.findById(id);
             if(!optional.isEmpty()) {
                 productDao.deleteById(id);
                 return CafeUtils.getResponseEntity("product deleted successfully", HttpStatus.OK);
             }
             return CafeUtils.getResponseEntity("product id doesnt exist..",HttpStatus.OK);
           } else {
               return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS,HttpStatus.UNAUTHORIZED);
           }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateStatus(Map<String, String> requestMap) {
       try {
           if(jwtFilter.isAdmin()) {
             Optional<Product> optional = productDao.findById(Integer.parseInt(requestMap.get("id")));
             if(!optional.isEmpty()) {
                   productDao.updateProductStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
                   return CafeUtils.getResponseEntity("product status updated successfully", HttpStatus.OK);
             }
             return CafeUtils.getResponseEntity("product id doesnt exist..", HttpStatus.OK);

           } else {
               return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS,HttpStatus.UNAUTHORIZED);
           }
       } catch (Exception ex) {
           ex.printStackTrace();
       }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getByCategory(Integer id) {
        try {
         return new ResponseEntity<>(productDao.getProductByCategory(id),HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    public ResponseEntity<ProductWrapper> getProductById(Integer id) {
        try {
            return new ResponseEntity<>(productDao.getProductById(id), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new ResponseEntity<>(new ProductWrapper(),HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
