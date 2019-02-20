package com.hstar.exeo.server.api;

import com.hstar.exeo.objects.db.ExeoUser;
import com.hstar.exeo.objects.db.Profile;
import com.hstar.exeo.objects.rest.LoginRequest;
import com.hstar.exeo.objects.rest.RegisterResult;
import com.hstar.exeo.server.repos.ExeoUserRepository;
import com.hstar.exeo.server.repos.ProfileRepository;
import com.hstar.exeo.server.security.PasswordUtils;
import com.hstar.exeo.server.security.jwt.JWTAuthenticationProvider;
import com.hstar.exeo.server.security.jwt.JWTSigningKey;
import com.nimbusds.jose.JOSEException;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

/**
 *
 * Created by Saswat on 8/9/2015.
 */
@RestController
public class RegisterRESTController {

    @Autowired private ExeoUserRepository userRepository;
    @Autowired private JWTSigningKey jwtSigningKey;
    @Autowired private ProfileRepository profileRepository;

    @RequestMapping(value = "/api/register", method = RequestMethod.POST, headers = {"Content-type=application/json"})
    public ResponseEntity<RegisterResult> register(@RequestBody LoginRequest loginRequest) throws JOSEException {
        return register(loginRequest.getFirstname(), loginRequest.getLastname(), loginRequest.getUsername(), loginRequest.getPassword());
    }

    @RequestMapping(value = "/api/register", method = RequestMethod.POST)
    public ResponseEntity<RegisterResult> register(@RequestParam("firstname") String firstname, @RequestParam("lastname") String lastname, @RequestParam("username") String username, @RequestParam("password") String password) throws JOSEException {
        if(firstname == null || lastname == null || username == null || password == null) {
            throw new IllegalArgumentException("Null parameters");
        }

        if(!EmailValidator.getInstance(false).isValid(username)) {
            return new ResponseEntity<>(new RegisterResult(RegisterResult.INVALID_USERNAME_ERROR, "Invalid username"), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if(userRepository.findByEmailIgnoreCase(username) != null) {
            return new ResponseEntity<>(new RegisterResult(RegisterResult.USERNAME_TAKEN_ERROR, "Username taken"), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        ExeoUser eu = userRepository.save(new ExeoUser(username, PasswordUtils.generatePasswordHash(password)));
        System.out.println(eu);
        profileRepository.save(new Profile(eu, firstname, lastname, null));

        return new ResponseEntity<>(new RegisterResult(JWTAuthenticationProvider.createJWTToken(eu, jwtSigningKey)), HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RegisterResult> handleException(Exception e) {
        e.printStackTrace();
        if (e instanceof MissingServletRequestParameterException || e instanceof IllegalArgumentException) {
            return new ResponseEntity<>(new RegisterResult(RegisterResult.REQUIRED_PARAMETERS_MISSING_ERROR, "Missing parameters"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new RegisterResult(RegisterResult.UNKNOWN_EXCEPTION_ERROR, e.getClass().getName()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
