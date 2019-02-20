package com.hstar.exeo.server.repos;

import com.hstar.exeo.objects.db.ExeoUser;
import com.hstar.exeo.objects.db.Friend;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Access to rdbms friend objects
 * Created by Saswat on 8/16/2015.
 */
@Repository
public interface FriendRepository extends CrudRepository<Friend, Long> {

    List<Friend> findByUser(ExeoUser user);

}
