package server;

import java.nio.channels.SocketChannel;
import java.util.Objects;

public class Auth implements Runnable{

    SocketChannel user;
    Server server;

    Player player;

    enum AuthState {
        AUTHENTICATION,
        LOGIN,
        REGISTER,
        DISCONNECT,
        SUCCESS
    }

    AuthState clientState = AuthState.AUTHENTICATION;


    public Auth(SocketChannel user, Server server) {
        this.user = user;
        this.server = server;
    }

    @Override
    public void run() {
        do {
            try {
                nextState();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while (clientState != AuthState.DISCONNECT);
    }

    public void nextState() throws InterruptedException {
        switch (clientState) {
            case AUTHENTICATION:
                authentication();
                break;
            case LOGIN:
                login();
                break;
            case REGISTER:
                register();
                break;
            case SUCCESS:
                server.addToQueue(player);
                clientState = AuthState.DISCONNECT;
                Utils.writeToSocket(user,"SUCCESSFUL");
                break;
            case DISCONNECT:
                break;
        }
    }
    public void authentication() {
        Utils.writeToSocket(user,"AUTHENTICATION");
        String answer = waitInput();
        if(Objects.equals(answer,"1")) {
            clientState = AuthState.REGISTER;
        }
        else if(Objects.equals(answer,"2")) {
            clientState = AuthState.LOGIN;
        }
        else if(Objects.equals(answer,"null") || Objects.equals(answer,"3") ) {
            clientState = AuthState.DISCONNECT;
        }
    }

    public void register() {
        Utils.writeToSocket(user,"REGISTER");
        String username;
        username = waitInput();
        String pass = waitInput();
        System.out.println(username);
        System.out.println(pass);

        if(Utils.doesUsernameExist(username)) {
            clientState = AuthState.REGISTER;
        }
        else {
            Utils.writeToSocket(user,"Successful register!");
            Utils.addUser(username,pass);
            this.player = new Player(user,username, 500);
            clientState = AuthState.SUCCESS;
        }
    }
    public void login() {
        Utils.writeToSocket(user,"LOGIN");
        String username = waitInput();
        String pass = waitInput();
        System.out.println(username);
        System.out.println(pass);
        int valid_user = Utils.isCredentialsValid(username,pass);
        if(Utils.isCredentialsValid(username,pass) > 0) {
            Utils.writeToSocket(user,"Successful login!");
            this.player = new Player(user,username, valid_user);
            clientState = AuthState.SUCCESS;
        }
        else {
            clientState = AuthState.LOGIN;
        }
    }


    public String waitInput()  {
        String answer = Utils.readFromSocket(user);
        while (Objects.equals(answer, "nada")) {
            answer = Utils.readFromSocket(user);
        }
        return answer;
    }
}
