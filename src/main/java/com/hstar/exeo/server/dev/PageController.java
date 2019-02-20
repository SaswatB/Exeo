package com.hstar.exeo.server.dev;

import com.hstar.exeo.server.ExeoApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MVC Controller that shows static assets.
 * Replaced by NGINX in production
 *
 * Created by Saswat on 8/6/2016.
 */
@Profile(ExeoApplication.DEVELOPMENT_PROFILE)
@Controller
public class PageController {
}
