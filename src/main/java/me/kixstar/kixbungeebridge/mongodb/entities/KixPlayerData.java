package me.kixstar.kixbungeebridge.mongodb.entities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.*;
/*@Entity
public class Article {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String articleId;

    private String articleTitle;

    @ManyToOne
    private Author author;

    // constructors, getters and setters...
}
*/
@Entity
public class KixPlayerData {

    @Id
    private String playerUUID;

    @Nullable
    private String nickname;

    @Nullable
    @OneToMany
    private Set<Home> homes;

    private int balance;

    //UNIX epoch time in seconds when the player first joined the server
    @Column(name = "first-logged-in")
    private Long firstLoggedIn;

    //UNIX epoch time in seconds when the player last joined the server
    @Column(name = "last-logged-in")
    private Long lastLoggedIn;

    //UNIX epoch time in seconds when the player last left the server
    @Column(name = "last-logged-out")
    private Long lastLoggedOut;

    private KixPlayerData() {}

    public KixPlayerData(
            @NotNull String playerUUID
    ) {
        this.playerUUID = playerUUID;
    }

    @Nullable
    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @NotNull
    public List<Home> getHomes() {
        if(this.homes == null) return new ArrayList<>();
        return new ArrayList<Home>(this.homes);
    }

    public void setHomes(@Nullable List<Home> homes) {
        //todo: fix :)
        if (homes == null) {
            this.homes = null;
        } else {
            this.homes = new HashSet<>(homes);
        }
    }

    public int getBalance() {
        return this.balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
