import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

public class ClientHandler {
    private MyServer myServer;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String name;

    public String getname() {
        return name;
    }

    public ClientHandler(MyServer myServer, final Socket socket) {
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.name = "";

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //почему-то таймер запускается только на шаге readMsg, а во время authentication времени неограничено
                        socket.setSoTimeout(1000);
                        authentication();
                        socket.setSoTimeout(0);
                        readMsg();
                    } catch (SocketException e) {
                        sendMsg("/end");
                    } finally {
                        closeConnection();
                    }

                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void authentication() {
        while (true) {
            try {
                String str = in.readUTF();
                if(str.startsWith("/")) {
                    if(str.startsWith("/reg")) {
                        String[] parts = str.split(" ");
                        if(myServer.getAuthService().reg(parts[1], parts[2], parts[3])) {
                            sendMsg("/reg_ok");
                        } else {
                            sendMsg("/reg_not");
                        }
                    }
                    if (str.startsWith("/auth")) {
                        String[] parts = str.split(" ");
                        String nick = myServer.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                        if (nick != null) {
                            if (!myServer.isNickBusy(nick)) {
                                sendMsg("/authok " + nick);
                                name = nick;
                                myServer.broadcastMsg(name + " зашел в чат");
                                myServer.subscribe(this);
                                return;
                            } else {
                                sendMsg("Учетная запись уже используется");
                            }
                        } else {
                            sendMsg("Неверные логин/пароль");
                        }
                    }
                }

            }  catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readMsg() {
        try {
            while (true) {
                String strFromServer = in.readUTF();
                System.out.println(name+": "+strFromServer);
                if(strFromServer.equals("/end")) {
                    sendMsg(strFromServer);
                    return;
                }
                if(strFromServer.startsWith("/w")) {
                    myServer.privateMsg(this, strFromServer);
                } else {
                    myServer.broadcastMsg(name + ": " + strFromServer);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        myServer.unsubscribe(this);
        myServer.broadcastMsg(name + " вышел из чата");
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}