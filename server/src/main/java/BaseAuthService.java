import java.util.ArrayList;
import java.util.List;

public class BaseAuthService implements AuthService{
    public class Entry {
        private String login;
        private String pass;
        private String nick;

        public Entry(String login, String pass, String nick) {
            this.login = login;
            this.pass = pass;
            this.nick = nick;
        }
    }

    private List<Entry> entries;

    public BaseAuthService() {
        entries = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            entries.add(new Entry("login"+i, "pass"+i, "nick"+i));
        }
    }

    @Override
    public void start() {
        System.out.println("Сервис аутенфикации запущен");
    }
    @Override
    public void stop() {
        System.out.println("Сервис аутенфикации остановлен");
    }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (Entry o: entries
        ) {
            if(o.login.equals(login) && o.pass.equals(pass)) return o.nick;
        }
        return null;
    }

    @Override
    public boolean reg(String login, String pass, String nick) {
        for (Entry o: entries
        ) {
            if(o.login.equals(login) || o.nick.equals(pass)) {
                return false;
            }
        }
        entries.add(new Entry(login, pass, nick));
        return true;
    }
}

