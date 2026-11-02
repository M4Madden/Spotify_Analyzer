package spotifylogin;

public class MainApp {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SpotifyUI().setVisible(true); // Must say true!
            }
        });

    }

}
