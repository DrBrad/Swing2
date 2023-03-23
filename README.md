Swing2
========

This is a better version of Java Swing, it allows you to create fragments and activities similar to android. This also comes with some of its own components such as TileViews and LineGraphs.

Library
-----
The JAR for this library can be found here: [Swing2 JAR](https://github.com/DrBrad/Swing2/blob/main/out/artifacts/Swing2_jar/Swing2.jar?raw=true)

Usage
-----
Here are some examples of how you can use this library, more can be found here: [Sample.java](https://github.com/DrBrad/Swing2/blob/main/src/unet/swing2/Sample.java)

**Creating an Activity and Fragments**
```Java
    public static void main(String[] args){
        Activity activity = new Activity(new MyFrag());
        activity.setVisible(true);
    }

    public static class MyFrag extends Fragment {

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
```

**LineGraph usage**
```Java
ArrayList<Double> d = new ArrayList<>();
d.add(1.0);
d.add(50.0);
d.add(0.0);
d.add(25.0);

int digitsDouble = 2;
GraphPanel graph = new GraphPanel(d, digitsDouble);
```

**TileView usage**
```Java
//TILE FOLER MUST HAVE THIS USAGE X-Y.png
TileView tile = new TileView(new File("/Location/To/Tiles"));
tile.addMarker(500, 500);
```

**RelativeLayout**
```Java
JPanel pane = new JPanel();
pane.setLayout(new RelativeLayout());

JLabel label = new JLabel();
label.setText("asdasd");
pane.add(label, new RelativeConstraints().centerInParent());
```

Components
-----
**TileView**
![TileView](https://raw.githubusercontent.com/DrBrad/Swing2/main/imgs/tile-view.png)

**LineGraph**
![LineGraph](https://raw.githubusercontent.com/DrBrad/Swing2/main/imgs/line-graph.png)
