package unet.swing2;

import unet.swing2.activity.Activity;
import unet.swing2.activity.Bundle;
import unet.swing2.activity.Fragment;
import unet.swing2.componants.GraphPanel;
import unet.swing2.layouts.RelativeConstraints;
import unet.swing2.layouts.RelativeLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class Sample {

    public static void main(String[] args){
        Activity activity = new Activity(new MainView());
        activity.setTitle("Hello World");
        activity.setVisible(true);
    }

    public static class MainView extends Fragment {

        @Override
        public void onCreate(Bundle bundle){
            JLabel label = new JLabel();
            label.setText("Click to next pane");
            getRoot().setLayout(new RelativeLayout());
            getRoot().add(label, new RelativeConstraints().setSize(new Dimension(RelativeConstraints.MATCH_PARENT, RelativeConstraints.MATCH_PARENT)));

            label.addMouseListener(new MouseListener(){
                @Override
                public void mouseClicked(MouseEvent e){
                    startFragment(new GraphView());
                }

                @Override
                public void mousePressed(MouseEvent e){
                }

                @Override
                public void mouseReleased(MouseEvent e){
                }

                @Override
                public void mouseEntered(MouseEvent e){
                }

                @Override
                public void mouseExited(MouseEvent e){
                }
            });
        }

        @Override
        public void onResume(){
            super.onResume();
        }

        @Override
        public void onPause(){
            super.onPause();
        }

        @Override
        public void onStop(){
            super.onStop();
        }
    }

    public static class GraphView extends Fragment {

        @Override
        public void onCreate(Bundle bundle){
            getRoot().setLayout(new RelativeLayout());

            ArrayList<Double> d = new ArrayList<>();
            d.add(1.0);
            d.add(50.0);
            d.add(0.0);
            d.add(25.0);
            GraphPanel graph = new GraphPanel(d, 2);

            getRoot().add(graph, new RelativeConstraints().setSize(new Dimension(RelativeConstraints.MATCH_PARENT, RelativeConstraints.MATCH_PARENT)));
        }

        @Override
        public void onResume(){
            super.onResume();
        }

        @Override
        public void onPause(){
            super.onPause();
        }

        @Override
        public void onStop(){
            super.onStop();
        }
    }
}
