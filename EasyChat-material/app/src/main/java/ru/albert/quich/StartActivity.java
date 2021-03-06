package ru.albert.quich;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import org.glassfish.tyrus.client.ClientManager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.websocket.DeploymentException;
import javax.websocket.Session;

public class StartActivity extends AppCompatActivity {
    public static String host = "ws://192.168.43.132:1111/chat";
    public static Session session;
    public static SharedPreferences loginSettings;
    public static String userName;
    public static SharedPreferences.Editor loginEditor;
    public static String sessionHash;
    public static AppCompatActivity startActivity;
    //todo register: hidden email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity = this;
        loginSettings = getSharedPreferences("loginInfo", MODE_PRIVATE);
        loginEditor = loginSettings.edit();
        boolean isFirstLaunch = loginSettings.getString("loginhash", "").equals("");
        //boolean isFirstLaunch = true;
        class ServerConnector extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ClientManager client = ClientManager.createClient();
                    session = client.connectToServer(ClientEndpoint.class, new URI(host));
                } catch (DeploymentException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        }
        if(!isFirstLaunch){
            ServerConnector connector = new ServerConnector();
            connector.execute();
            userName = loginSettings.getString("username", "");
            sessionHash = loginSettings.getString("loginhash", "");
            class SessionChecker extends AsyncTask<Void, Void, Void> {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        session.getBasicRemote().sendText(new Message("sessionHashCheck", sessionHash).toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);
                }
            }
            SessionChecker checker = new SessionChecker();
            checker.execute();
        }
        else {
            setContentView(R.layout.fragment_container);
            if (savedInstanceState == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.container, new StartFragment())
                        .commit();
            }

            ServerConnector connector = new ServerConnector();
            connector.execute();
        }
    }
}