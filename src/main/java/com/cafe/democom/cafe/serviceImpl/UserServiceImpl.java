package com.cafe.democom.cafe.serviceImpl;

import com.cafe.democom.cafe.JWT.CustomerUsersDetailsService;
import com.cafe.democom.cafe.JWT.JwtFilter;
import com.cafe.democom.cafe.JWT.JwtUtil;
import com.cafe.democom.cafe.POJO.User;
import com.cafe.democom.cafe.constants.CafeConstants;
import com.cafe.democom.cafe.dao.UserDao;
import com.cafe.democom.cafe.service.UserService;
import com.cafe.democom.cafe.utils.CafeUtils;
import com.cafe.democom.cafe.utils.EmailUtils;
import com.cafe.democom.cafe.wrapper.UserWrapper;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;


@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserDao userDao;

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    CustomerUsersDetailsService customerUsersDetailsService;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    EmailUtils emailUtils;
    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
     log.info("inside signup {}", requestMap);
     try {
         if(validateSignUpMap(requestMap)){
             User user = userDao.findByEmailId(requestMap.get("email"));
             if(Objects.isNull(user)) {
                 userDao.save(getUserFromMap(requestMap));
                 return CafeUtils.getResponseEntity("successfully registered", HttpStatus.OK);
             } else {
                 return CafeUtils.getResponseEntity("email already exists", HttpStatus.BAD_REQUEST);
             }
         }
         else {
             return CafeUtils.getResponseEntity(CafeConstants.Invalid_Data, HttpStatus.BAD_REQUEST);
         }
     } catch (Exception ex) {
         ex.printStackTrace();
        }
   return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);

    }

    private boolean validateSignUpMap(Map<String, String> requestMap) {
    if(requestMap.containsKey("name") && requestMap.containsKey("contactNumber")
            && requestMap.containsKey("email") && requestMap.containsKey("password")) {
        return true;
    }
    return false;
    }

    //extract data from requestmap and return the user before saving
    private User getUserFromMap(Map<String,String> requestMap) {
        User user = new User();
        user.setName(requestMap.get("name"));
        user.setContactNumber(requestMap.get("contactNumber"));
        user.setEmail(requestMap.get("email"));
        user.setPassword(requestMap.get("password"));
        user.setStatus("false");
        user.setRole("user");
        return user;
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("inside login");
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password"))
            );
            if(auth.isAuthenticated()){
                if(customerUsersDetailsService.getUserDetail().getStatus().equalsIgnoreCase("true")) {
                 return new ResponseEntity<String>("{\"token\":\""+jwtUtil.generateToken(customerUsersDetailsService.getUserDetail().getEmail(),
                         customerUsersDetailsService.getUserDetail().getRole()) + "\"}",
                    HttpStatus.OK);
                } else {
                    return new ResponseEntity<String>("{\"message\":\""+"wait for admin approval."+"\"}", HttpStatus.BAD_REQUEST);
                }
            }
        } catch (Exception ex) {
            log.error("{}",ex);
        }
        return new ResponseEntity<String>("{\"message\":\""+"BAD CREDENTIALS."+"\"}", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser() {
        try {
     if(jwtFilter.isAdmin()) {
       return new ResponseEntity<>(userDao.getAllUser(),HttpStatus.OK);
     } else {
         return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
     }
        } catch (Exception ex) {
             ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
       try {
      if(jwtFilter.isAdmin()){
          Optional<User> optional = userDao.findById(Integer.parseInt(requestMap.get("id")));
          if(!optional.isEmpty()) {
            userDao.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
            sendEmailToAllAdmin(requestMap.get("status"), optional.get().getEmail(),userDao.getAllAdmin());
            return CafeUtils.getResponseEntity("user status updated successfully", HttpStatus.OK);
          } else {
             return CafeUtils.getResponseEntity("user id doesnt exists", HttpStatus.OK);
          }
      } else {
          return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS,HttpStatus.UNAUTHORIZED);
      }
       } catch (Exception ex) {
           ex.printStackTrace();
       }
       return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void sendEmailToAllAdmin(String status, String user, List<String> allAdmin) {
        allAdmin.remove(jwtFilter.getCurrentUser());
        if(status!=null && status.equalsIgnoreCase("true")) {
         emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(),"account approved", "USER:- "+user+" \n is approved by \nADMIN:-"+jwtFilter.getCurrentUser(),allAdmin);
        } else {
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(),"account disabled", "USER:- "+user+" \n is disables by \nADMIN:-"+jwtFilter.getCurrentUser(),allAdmin);

        }
    }

    @Override
    public ResponseEntity<String> checkToken() {

           return CafeUtils.getResponseEntity("true", HttpStatus.OK);


    }

    @Override
    public ResponseEntity<String> changePassword(Map<String,String> requestMap) {
        try {
            User userObj = userDao.findByEmail(jwtFilter.getCurrentUser());

            if (!userObj.equals(null)) {
              if(userObj.getPassword().equals(requestMap.get("oldPassword"))) {
                 userObj.setPassword(requestMap.get("newPassword"));
                 userDao.save(userObj);
                 return CafeUtils.getResponseEntity("Password actualizada correctamente", HttpStatus.OK);
              }
              return CafeUtils.getResponseEntity("incorrect old password", HttpStatus.BAD_REQUEST);
            }

            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        try {
        User user = userDao.findByEmail(requestMap.get("email"));
        if(!Objects.isNull(user) && !Strings.isNullOrEmpty(user.getEmail()))
           emailUtils.forgotMail(user.getEmail(),"credentials by cafe managment system", user.getPassword());
          return CafeUtils.getResponseEntity("Checkea tu email para ver las credenciales", HttpStatus.OK);

        } catch (Exception ex) {
           ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }


}
