package comp1206.sushi;

import comp1206.sushi.common.User;

//this class helps initialise the Client with all the server data
public class Comms {

    private boolean initClientRequest;
    private boolean loginRequest;
    private User user;

    public Comms(User user){
        initClientRequest = false;
        loginRequest = false;
        this.user = user;
    }

    public Comms(){
    }

    public boolean isInitClientRequest() {
        return initClientRequest;
    }

    public void setInitClientRequest(boolean initClientRequest) {
        this.initClientRequest = initClientRequest;
    }

    public boolean isLoginRequest() {
        return loginRequest;
    }

    public void setLoginRequest(boolean loginRequest) {
        this.loginRequest = loginRequest;
    }

    public User getUser() {
        return user;
    }
}
