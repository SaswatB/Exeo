package com.hstar.exeo.server.api;

import com.hstar.exeo.objects.db.ExeoUser;
import com.hstar.exeo.objects.rest.LoginRequest;
import com.hstar.exeo.objects.rest.LoginResult;
import com.hstar.exeo.server.repos.ExeoUserRepository;
import com.hstar.exeo.server.security.PasswordUtils;
import com.hstar.exeo.server.security.jwt.JWTAuthenticationProvider;
import com.hstar.exeo.server.security.jwt.JWTSigningKey;
import com.nimbusds.jose.JOSEException;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class LoginRESTController {

    @Autowired private ExeoUserRepository userRepository;
    @Autowired private JWTSigningKey jwtSigningKey;

    @RequestMapping(value = "/api/login", method = RequestMethod.POST, headers = {"Content-type=application/json"})
    public LoginResult login(@RequestBody LoginRequest loginRequest) throws LoginFailed, JOSEException {
        return login(loginRequest.getUsername(), loginRequest.getPassword());
    }

    @RequestMapping(value = "/api/login", method = RequestMethod.POST)
    public LoginResult login(@RequestParam("username") String username, @RequestParam("password") String password) throws LoginFailed, JOSEException {

        if(!EmailValidator.getInstance(false).isValid(username)) {
            throw new LoginFailed("Invalid username");
        }

        //check credentials against user database
        ExeoUser u = userRepository.findByEmailIgnoreCase(username);
        if(u == null) {
            //no such username
            throw new LoginFailed("Bad credentials");
        }
        if(!u.getLocalLogin()) {
            throw new LoginFailed("Local login not enabled");
        }
        if(!PasswordUtils.validatePassword(password, u.getPassword())) {
            //bad password
            throw new LoginFailed("Bad credentials");
        }

        //login passed, construct the jwt token and return a successful login with the token
        return new LoginResult(true, JWTAuthenticationProvider.createJWTToken(u, jwtSigningKey));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public LoginResult handleException(Exception e) {
        e.printStackTrace();
        return new LoginResult(false, null);
    }

    private class LoginFailed extends Exception {
        LoginFailed(String message) {
            super(message);
        }
    }

}