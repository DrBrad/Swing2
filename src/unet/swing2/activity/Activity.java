package unet.swing2.activity;

import javax.swing.*;
import java.awt.*;

public class Activity {

    private JFrame frame;

    public Activity(Fragment fragment){
        System.setProperty("sun.java2d.opengl", "true");

        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(1000, 600));

        fragment.setParent(frame);
        fragment.onCreate(null);
    }

    public JFrame getFrame(){
        return frame;
    }

    public void setVisible(boolean visible){
        frame.setVisible(visible);
    }

    public void setTitle(String title){
        frame.setTitle(title);
    }

    public void setSize(Dimension dimension){
        frame.setSize(dimension);
    }

    public void setMinimumSize(Dimension dimension){
        frame.setMinimumSize(dimension);
    }

    public void setDefaultCloseOperation(int operation){
        frame.setDefaultCloseOperation(operation);
    }
}
