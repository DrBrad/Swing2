package unet.swing2.componants;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class TileView extends JPanel implements ImageObserver {//extends JComponent implements ImageObserver {

    private int minX = 0, maxX = 10, minY = 0, maxY = 5, tileWidth = 400, tileHeight = 400;
    private int currentDownX = 0, currentDownY = 0;

    private Rectangle windowPort;

    private HashMap<String, BufferedImage> cached = new HashMap<>();

    private Color markerSelectedColor = new Color(0, 133, 119, 70), markerLockedColor = new Color(0, 170, 0, 70);
    private Camera camera;
    private Cursor openHand, closedHand, pointingHand;

    private BufferedImage markerBitmap, selectedMarkerBitmap;
    private ArrayList<Marker> markers = new ArrayList<>();
    private Marker selectedMarker;
    private ArrayList<MarkerClickListener> markerClickListeners = new ArrayList<>();
    private ArrayList<MarkerLoseFocusListener> markerLoseFocusListeners = new ArrayList<>();
    private boolean locked;

    //TRY SAVING BITMAP OF CURRENT POSITION THEN LOADING THAT UNTIL MOVED OVER

    private File tileFolder;

    public TileView(File tileFolder){
        super();
        if(tileFolder.exists()){
            this.tileFolder = tileFolder;
            camera = new Camera(0, 0, 0);

            try{
                BufferedImage icon = ImageIO.read(getClass().getResource("/cursors/open_hand.png"));
                openHand = Toolkit.getDefaultToolkit().createCustomCursor(icon , new Point(5, 10), "openhand");
                icon = ImageIO.read(getClass().getResource("/cursors/closed_hand.png"));
                closedHand = Toolkit.getDefaultToolkit().createCustomCursor(icon , new Point(5, 10), "closedhand");
                icon = ImageIO.read(getClass().getResource("/cursors/pointing_hand.png"));
                pointingHand = Toolkit.getDefaultToolkit().createCustomCursor(icon , new Point(5, 5), "pointinghand");

                markerBitmap = ImageIO.read(getClass().getResource("/marker.png"));
                selectedMarkerBitmap = ImageIO.read(getClass().getResource("/marker_selected.png"));
            }catch(Exception e){
                e.printStackTrace();
            }

            setCursor(openHand);

            cacheNewTiles();

            addMouseMotionListener(new MouseMotionListener(){
                @Override
                public void mouseDragged(MouseEvent e){
                    camera.addXY(e.getX()-currentDownX, e.getY()-currentDownY);

                    currentDownX = e.getX();
                    currentDownY = e.getY();

                    repaint();
                }

                @Override
                public void mouseMoved(MouseEvent e){
                    if(!locked){
                        for(Marker marker : markers){
                            int xs = (int) (markerBitmap.getWidth());
                            int ys = (int) (markerBitmap.getHeight());
                            int x = (marker.getX()+camera.getX())-((tileWidth/2)+(xs/2)), y = (marker.getY()+camera.getY())-((tileHeight/2)+ys);

                            if(new Rectangle(x, y, xs, ys).contains(e.getX(), e.getY())){
                                setCursor(pointingHand);
                                return;
                            }
                        }
                    }
                    setCursor(openHand);
                }
            });

            addMouseListener(new MouseListener(){
                @Override
                public void mouseClicked(MouseEvent e){
                    if(!locked){
                        Marker tmpMarker = selectedMarker;
                        selectedMarker = null;

                        for(Marker marker : markers){
                            int xs = (int) (markerBitmap.getWidth());
                            int ys = (int) (markerBitmap.getHeight());
                            int x = (marker.getX()+camera.getX())-((tileWidth/2)+(xs/2)), y = (marker.getY()+camera.getY())-((tileHeight/2)+ys);

                            if(new Rectangle(x, y, xs, ys).contains(e.getX(), e.getY())){
                                selectedMarker = marker;
                                if(!markerClickListeners.isEmpty()){
                                    for(MarkerClickListener l : markerClickListeners){
                                        l.onMarkerClick(TileView.this, marker);
                                    }
                                }
                                moveCamera(marker.getX(), marker.getY());
                                break;
                            }
                        }

                        if(selectedMarker == null && tmpMarker != null){
                            if(!markerLoseFocusListeners.isEmpty()){
                                for(MarkerLoseFocusListener l : markerLoseFocusListeners){
                                    l.onMarkerLoseFocus(TileView.this, tmpMarker);
                                }
                            }
                        }

                        repaint();
                    }
                }

                @Override
                public void mousePressed(MouseEvent e){
                    currentDownX = e.getX();
                    currentDownY = e.getY();
                    setCursor(closedHand);
                }

                @Override
                public void mouseReleased(MouseEvent e){
                    setCursor(openHand);
                }

                @Override
                public void mouseEntered(MouseEvent e){
                }

                @Override
                public void mouseExited(MouseEvent e){
                }
            });

            addComponentListener(new ComponentAdapter(){
                @Override
                public void componentResized(ComponentEvent e){
                    super.componentResized(e);

                    camera.setViewPort(new Rectangle(getBounds().x, getBounds().y, ((getBounds().width/tileWidth)*tileWidth)+(tileWidth*3), ((getBounds().height/tileHeight)*tileHeight)+(tileHeight*3)));
                    windowPort = new Rectangle(-tileWidth, -tileHeight, camera.getViewPort().width-tileWidth, camera.getViewPort().height-tileHeight);

                    repaint();
                }
            });
        }else{
            throw new NullPointerException("You must specify the tile location.");
        }
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g2d.setColor(Color.decode("#111111"));
        g2d.fillRect(getBounds().x, getBounds().y, getBounds().width, getBounds().height);

        try{
            for(int x = 0; x < camera.getMatrixWidth(); x++){
                for(int y = 0; y < camera.getMatrixHeight(); y++){
                    int ox = ((x*tileWidth)+(camera.getX()%tileWidth))-(tileWidth/2),
                            oy = ((y*tileHeight)+(camera.getY()%tileHeight))-(tileHeight/2);
                    if(cached.containsKey((x+camera.getMatrixX())+"|"+(y+camera.getMatrixY()))){
                        g2d.drawImage(cached.get((x+camera.getMatrixX())+"|"+(y+camera.getMatrixY())), ox, oy, this);
                    }
                }
            }

            for(Marker marker : markers){
                int xs = (int) (markerBitmap.getWidth());
                int ys = (int) (markerBitmap.getHeight());
                int x = (marker.getX()+camera.getX())-((tileWidth/2)+(xs/2)), y = (marker.getY()+camera.getY())-((tileHeight/2)+ys);

                if(windowPort.contains(x, y, xs, ys)){
                    if(marker == selectedMarker){
                        if(locked){
                            g2d.setColor(markerLockedColor);
                            g2d.fillOval(x-(xs/2), y-(ys/2), xs*2, ys*2);

                            g2d.setStroke(new BasicStroke(2));
                            g2d.setColor(Color.decode("#00aa00"));
                            g2d.drawOval(x-(xs/2), y-(ys/2), xs*2, ys*2);
                            g2d.drawImage(selectedMarkerBitmap, x, y, this);

                        }else{
                            g2d.setColor(markerSelectedColor);
                            g2d.fillOval(x-(xs/2), y-(ys/2), xs*2, ys*2);

                            g2d.setStroke(new BasicStroke(2));
                            g2d.setColor(Color.decode("#008577"));
                            g2d.drawOval(x-(xs/2), y-(ys/2), xs*2, ys*2);
                            g2d.drawImage(markerBitmap, x, y, this);
                        }
                    }else{

                        g2d.drawImage(markerBitmap, x, y, this);
                    }
                }
            }
        }catch(Exception e){
        }
    }

    private void cacheNewTiles(){
        try{
            for(int x = 0; x < 10; x++){
                for(int y = 0; y < 5; y++){
                    if(x <= maxX && x >= minX && y <= maxY && y >= minY){
                        if(!cached.containsKey(x+"|"+y)){
                            if(new File(tileFolder.getPath()+"/"+x+"-"+y+".png").exists()){// != null){
                                cached.put(x+"|"+y, ImageIO.read(new File(tileFolder.getPath()+"/"+x+"-"+y+".png")));
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void moveCamera(int x, int y){
        x = (-x+(tileWidth/2))+(getWidth()/2);
        y = (-y+(tileHeight/2))+(getHeight()/2);

        ValueAnimator xanimator = new ValueAnimator(camera.x, x);
        xanimator.setDuration(500);

        xanimator.addUpdateListener(new ValueAnimator.AnimationUpdateListener(){
            @Override
            public void onAnimationUpdate(int value){
                camera.setX(value);
                repaint();
            }
        });

        ValueAnimator yanimator = new ValueAnimator(camera.y, y);
        yanimator.setDuration(500);

        yanimator.addUpdateListener(new ValueAnimator.AnimationUpdateListener(){
            @Override
            public void onAnimationUpdate(int value){
                camera.setY(value);
                repaint();
            }
        });

        xanimator.start();
        yanimator.start();
    }

    public void selectMarker(Marker marker){
        if(!locked){
            selectedMarker = marker;
            if(!markerClickListeners.isEmpty()){
                for(MarkerClickListener l : markerClickListeners){
                    l.onMarkerClick(TileView.this, marker);
                }
            }
            repaint();
            moveCamera(marker.getX(), marker.getY());
        }
    }

    public void addOnMarkerClickListener(MarkerClickListener markerClickListener){
        markerClickListeners.add(markerClickListener);
    }

    public void removeOnMarkerClickListener(MarkerClickListener markerClickListener){
        markerClickListeners.remove(markerClickListener);
    }

    public void addOnMarkerLoseFocusListener(MarkerLoseFocusListener markerLoseFocusListener){
        markerLoseFocusListeners.add(markerLoseFocusListener);
    }

    public void removeOnMarkerLoseFocusListener(MarkerLoseFocusListener markerLoseFocusListener){
        markerLoseFocusListeners.remove(markerLoseFocusListener);
    }

    public Marker addMarker(int x, int y){
        Marker marker = new Marker(x, y);
        markers.add(marker);
        return marker;
    }

    public void removeMarker(Marker marker){
        markers.remove(marker);
    }

    public void clearMarkerFocus(){
        selectedMarker = null;
        super.invalidate();
    }

    public void lockMarker(boolean locked){
        this.locked = locked;
        repaint();
    }

    public void setOpenHandCursor(Cursor openHand){
        this.openHand = openHand;
    }

    public void setClosedHandCursor(Cursor closedHand){
        this.closedHand = closedHand;
    }

    public void setPointingHandCursor(Cursor pointingHand){
        this.pointingHand = pointingHand;
    }

    public void setMarkerBitmap(BufferedImage markerBitmap){
        this.markerBitmap = markerBitmap;
    }

    public void setSelectedMarkerBitmap(BufferedImage selectedMarkerBitmap){
        this.selectedMarkerBitmap = selectedMarkerBitmap;
    }

    public void setMarkerSelectedColor(Color markerSelectedColor){
        this.markerSelectedColor = markerSelectedColor;
    }

    public void setMarkerLockedColor(Color markerLockedColor){
        this.markerLockedColor = markerLockedColor;
    }

    public boolean isLocked(){
        return locked;
    }

    private class Camera {

        private int x, y, z;
        private Rectangle viewPort, matrixPort;

        public Camera(int x, int y, int z){
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public void setXY(int x, int y){
            this.x = x;
            this.y = y;
        }

        public void addXY(int x, int y){
            this.x += x;
            this.y += y;
        }

        public void setX(int x){
            this.x = x;
        }

        public void setY(int y){
            this.y = y;
        }

        public void setViewPort(Rectangle viewPort){
            this.viewPort = viewPort;
            this.matrixPort = new Rectangle(0, 0, viewPort.width/tileWidth, viewPort.height/tileHeight);
        }

        public int getX(){
            return x;
        }

        public int getY(){
            return y;
        }

        public int getMatrixX(){
            return -x/tileWidth;
        }

        public int getMatrixY(){
            return -y/tileHeight;
        }

        public Rectangle getViewPort(){
            return viewPort;
        }

        public Rectangle getMatrixPort(){
            return matrixPort;
        }

        public int getMatrixWidth(){
            return matrixPort.width;
        }

        public int getMatrixHeight(){
            return matrixPort.height;
        }
    }

    public static class ValueAnimator {

        private int to, from;
        private long duration = 2000;
        private AnimationUpdateListener animationUpdateListener;
        private Timer timer;

        public ValueAnimator(int to, int from){
            this.to = to;
            this.from = from;
        }

        public void setDuration(long duration){
            this.duration = duration;
        }

        public void addUpdateListener(AnimationUpdateListener animationUpdateListener){
            this.animationUpdateListener = animationUpdateListener;
        }

        public void start(){
            int totalDistance = to-from;
            int sleep = (int) (duration/40);
            int distanceMultiplier = totalDistance/sleep;

            timer = new Timer(sleep, new ActionListener(){
                private int currentValue = to, counter = 1;

                public void actionPerformed(ActionEvent e){
                    if(counter > sleep){
                        //currentValue = from;
                        animationUpdateListener.onAnimationUpdate(from);
                        stop();
                    }else{
                        currentValue -= distanceMultiplier;
                        animationUpdateListener.onAnimationUpdate(currentValue);
                        counter++;
                    }
                }
            });
            timer.start();

            /*
            int totalDistance = to-from;
            int sleep = (int) (duration/40);
            int distanceMultiplier = totalDistance/sleep;

            new Thread(new Runnable(){
                @Override
                public void run(){
                    int currentValue = to;

                    try{
                        for(int i = 0; i < sleep; i++){
                            currentValue -= distanceMultiplier;
                            animationUpdateListener.onAnimationUpdate(currentValue);
                            Thread.sleep(sleep);
                        }

                        animationUpdateListener.onAnimationUpdate(from);
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                }
            }).start();
            */
        }

        public void stop(){
            if(timer != null && timer.isRunning()){
                timer.stop();
            }
        }

        public interface AnimationUpdateListener {
            void onAnimationUpdate(int value);
        }
    }
}

interface MarkerClickListener {
    void onMarkerClick(TileView v, Marker marker);
}

interface MarkerLoseFocusListener {
    void onMarkerLoseFocus(TileView v, Marker marker);
}

class Marker {

    private int x, y;

    public Marker(int x, int y){
        this.x = x;
        this.y = y;
    }

    //SET BITMAP FOR MARKER

    public void setX(int x){
        this.x = x;
    }

    public void setY(int y){
        this.y = y;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }
}
