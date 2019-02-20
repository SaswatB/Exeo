package com.hstar.exeo.objects.db;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Primary key for Friend object
 * Created by Saswat on 8/16/2015.
 */
@Embeddable
public class FriendPK implements Serializable {

    @Column(name = "user_id")
    private long userId;

    @Column(name = "friend_user_id")
    private long friendId;

    protected FriendPK() {
    }

    public FriendPK(ExeoUser user, ExeoUser friend) {
        this.userId = user.getId();
        this.friendId = friend.getId();
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getFriendId() {
        return friendId;
    }

    public void setFriendId(long friendId) {
        this.friendId = friendId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FriendPK friendPK = (FriendPK) o;

        return userId == friendPK.userId && friendId == friendPK.friendId;

    }

    @Override
    public int hashCode() {
        int result = (int) (userId ^ (userId >>> 32));
        result = 31 * result + (int) (friendId ^ (friendId >>> 32));
        return result;
    }
}
