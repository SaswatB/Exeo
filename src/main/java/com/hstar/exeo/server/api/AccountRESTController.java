package com.hstar.exeo.server.api;

import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Saswat on 8/1/2015.
 */
@RestController
public class AccountRESTController {

    /*@Autowired ProfileRepository profileRepository;

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/account/me", method = RequestMethod.GET)
    public UserSummary me() {
        ExeoUser user = ((JWTToken)SecurityContextHolder.getContext().getAuthentication()).getUser();
        Profile p = profileRepository.findByUserId(user.getId());

        return new UserSummary(user.getUuid(), p.getFirstname(), p.getLastname(), p.getBirthday(), user.getEmail());
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/account/me", method = RequestMethod.POST)
    public UserSummary me(@RequestParam(value="firstname", required=false) Optional<String> firstname,
                          @RequestParam(value="lastname", required=false) Optional<String> lastname,
                          @RequestParam(value="birthday", required=false) Optional<String> birthday) throws ParseException {
        ExeoUser user = ((JWTToken)SecurityContextHolder.getContext().getAuthentication()).getUser();
        Profile p = profileRepository.findByUserId(user.getId());

        if(firstname.isPresent())p.setFirstname(firstname.get());
        if(lastname.isPresent())p.setLastname(lastname.get());
        if(birthday.isPresent())p.setBirthday(new Date(new SimpleDateFormat("MM/dd/yyyy").parse(birthday.get()).getTime()));

        p = profileRepository.save(p);

        return new UserSummary(user.getUuid(), p.getFirstname(), p.getLastname(), p.getBirthday(), user.getEmail());
    }*/

}
