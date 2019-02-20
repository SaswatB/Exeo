package com.hstar.exeo.objects.db;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 *
 * Created by Saswat on 8/16/2015.
 */
@Entity
@Table(name = "friends")
public class Friend {

    @EmbeddedId
    private FriendPK key;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable=false)
    private ExeoUser user;

    @ManyToOne
    @JoinColumn(name = "friend_user_id", insertable = false, updatable=false)
    private ExeoUser friend;

    @Column(name = "friend_active", columnDefinition = "TINYINT", length = 1)
    private boolean friendActive;

    @Column(name = "friend_relation")
    private String relation;

    @Column(name = "date_created")
    private Timestamp dateCreated;

    protected Friend() {}

    public Friend(ExeoUser user, ExeoUser friend, boolean friendActive, String relation) {
        this.key = new FriendPK(user, friend);
        this.user = user;
        this.friend = friend;
        this.friendActive = friendActive;
        this.relation = relation;

        dateCreated = new Timestamp(System.currentTimeMillis());
    }

    public FriendPK getKey() {
        return key;
    }

    public void setKey(FriendPK key) {
        this.key = key;
    }

    public boolean isFriendActive() {
        return friendActive;
    }

    public void setFriendActive(boolean friendActive) {
        this.friendActive = friendActive;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

}
