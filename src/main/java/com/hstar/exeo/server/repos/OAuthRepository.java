package com.hstar.exeo.server.repos;

import com.hstar.exeo.objects.db.OAuth;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Access to rdbms oauth objects
 * Created by Saswat on 8/5/2015.
 */
@Repository
public interface OAuthRepository extends CrudRepository<OAuth, Long> {

    OAuth findByProviderAndProviderUserId(String provider, String provider_user_id);
}
