package unet.swing2.activity;

import javax.swing.*;

public class Fragment {

    private JFrame frame;
    private JPanel root;

    public void setParent(JFrame frame){
        this.frame = frame;
        root = new JPanel();
        frame.add(root);
    }

    public void onCreate(Bundle bundle){

    }

    public void onResume(){

    }

    public void onPause(){
        root.setVisible(false);
    }

    public void onStop(){

    }

    public void startFragment(Fragment fragment){
        startFragment(fragment, null);
    }

    public void startFragment(Fragment fragment, Bundle bundle){
        fragment.setParent(frame);
        onPause();
        fragment.onCreate(bundle);
    }

    public JPanel getRoot(){
        return root;
    }
}
