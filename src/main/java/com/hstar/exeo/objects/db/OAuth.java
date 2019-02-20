package com.hstar.exeo.objects.db;

import javax.persistence.*;

/**
 * Created by Saswat on 8/5/2015.
 */
@Entity
@Table(name = "oauth")
public class OAuth {

    public static final String PROVIDER_FACEBOOK = "facebook";
    public static final String PROVIDER_GOOGLE = "google";
    public static final String PROVIDER_MICROSOFT = "microsoft";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "oauth_id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private ExeoUser user;

    @Column(name = "oauth_provider")
    private String provider;

    @Column(name = "oauth_provider_user_id")
    private String providerUserId;

    protected OAuth() {}

    public OAuth(ExeoUser user, String provider, String providerUserId) {
        this.user = user;
        this.provider = provider;
        this.providerUserId = providerUserId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ExeoUser getUser() {
        return user;
    }

    public void setUser(ExeoUser user) {
        this.user = user;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }
}
