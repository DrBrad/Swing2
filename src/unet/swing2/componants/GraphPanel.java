package unet.swing2.componants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;

public class GraphPanel extends JPanel {

    private ArrayList<Double> scores = new ArrayList<>();
    private Rectangle graphBounds = new Rectangle(0, 0, 110, 30);
    private Color graphColor = Color.decode("#0e1118"),
            graphLineColor = Color.decode("#5a5f6d"),
            hoverColor = Color.decode("#bbbbbb"),
            positiveColor = Color.decode("#44bd22"),
            negativeColor = Color.decode("#fa652d");

    private int gridWidth = 100, gridHeight = 50, maxSize = 100;
    private int time = 5, digits = 5;
    private String timeValue = "Secs";

    private boolean hovering;
    private int mouseX = 0, mouseY = 0;

    public GraphPanel(){
        init();
    }

    public GraphPanel(ArrayList<Double> scores, int digits){
        this.scores = scores;
        this.digits = digits;

        if(maxSize < scores.size()){
            scores.subList(0, scores.size()-maxSize);
        }

        init();
    }

    private void init(){
        setForeground(Color.decode("#5a5f6d"));
        setBackground(Color.decode("#0e1118"));

        addMouseMotionListener(new MouseMotionListener(){
            @Override
            public void mouseDragged(MouseEvent e){
            }

            @Override
            public void mouseMoved(MouseEvent e){
                int graphWidth = getWidth()-graphBounds.width, graphHeight = getHeight()-graphBounds.height;
                mouseX = e.getX();
                mouseY = e.getY();

                if(mouseX > 0 && mouseX < graphWidth && mouseY > 0 && mouseY < graphHeight){
                    hovering = true;
                    setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                }else{
                    hovering = false;
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                repaint();
            }
        });

        addMouseListener(new MouseAdapter(){
            @Override
            public void mouseExited(MouseEvent e){
                super.mouseExited(e);
                hovering = false;
                repaint();
            }
        });
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

        int graphWidth = getWidth()-graphBounds.width, graphHeight = getHeight()-graphBounds.height;

        g2.setColor(graphColor);
        g2.setStroke(new BasicStroke(0.5f));
        g2.fillRect(graphBounds.x, graphBounds.y, graphWidth-graphBounds.x, graphHeight-graphBounds.y);

        g2.setColor(graphLineColor);
        g2.drawLine(graphWidth, graphBounds.y, graphWidth, graphHeight);
        g2.drawLine(graphBounds.x, graphHeight, graphWidth, graphHeight);

        for(int i = 1; i < ((graphWidth-graphBounds.x)/gridWidth)+1; i++){
            g2.setColor(graphLineColor);
            g2.drawLine(graphWidth-(i*gridWidth), graphBounds.y, graphWidth-(i*gridWidth), graphHeight+3);

            g2.setColor(getForeground());
            String xLabel = (i*time)+" "+timeValue;
            FontMetrics metrics = g2.getFontMetrics();
            int labelWidth = metrics.stringWidth(xLabel);
            g2.drawString(xLabel, graphWidth-(i*gridWidth)-labelWidth/2, graphHeight+metrics.getHeight()+3);
        }

        if(scores.size() > 1){
            double waveHeight = getMax()/(graphHeight/gridHeight);

            for(int i = 1; i < ((graphHeight-graphBounds.y)/gridHeight)+1; i++){
                g2.setColor(graphLineColor);
                g2.drawLine(graphBounds.x, graphHeight-(i*gridHeight), graphWidth+3, graphHeight-(i*gridHeight));

                g2.setColor(getForeground());
                String yLabel = String.format("%."+digits+"f", waveHeight*i);
                FontMetrics metrics = g2.getFontMetrics();
                g2.drawString(yLabel, graphWidth+5, graphHeight-(i*gridHeight)+(metrics.getHeight()/2)-3);
            }

            int totalHeight = (graphHeight/gridHeight)*gridHeight;

            ArrayList<Point> points = new ArrayList<>();
            for(int i = scores.size(); i > 0; i--){
                int x = graphWidth-(i*gridWidth);
                int y = graphHeight-(int) (((scores.get(scores.size()-i))/getMax())*totalHeight);
                points.add(new Point(x, y));
            }

            g2.setStroke(new BasicStroke(2));

            for(int i = 0; i < points.size()-1; i++){
                if(i+1 < points.size()){
                    if(points.get(i).y >= points.get(i+1).y){
                        g2.setColor(positiveColor);
                    }else{
                        g2.setColor(negativeColor);
                    }
                }else{
                    g2.setColor(positiveColor);
                }

                g2.drawLine(points.get(i).x, points.get(i).y, points.get(i+1).x, points.get(i+1).y);
            }

            String yLabel = String.format("%."+digits+"f", scores.get(scores.size()-1));
            FontMetrics metrics = g2.getFontMetrics();

            if(scores.get(scores.size()-2) >= scores.get(scores.size()-1)){
                g2.setColor(positiveColor);
            }else{
                g2.setColor(negativeColor);
            }

            g2.fillRoundRect(graphWidth, points.get(points.size()-1).y-(metrics.getHeight()/2)-3, graphBounds.width-5, metrics.getHeight()+6, 5, 5);

            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0));
            g2.drawLine(graphBounds.x, points.get(points.size()-1).y-(metrics.getHeight()/2)+((metrics.getHeight()+4)/2)-1, graphWidth, points.get(points.size()-1).y-(metrics.getHeight()/2)+((metrics.getHeight()+4)/2)-1);

            g2.setColor(Color.decode("#ffffff"));
            g2.drawLine(graphWidth, points.get(points.size()-1).y-(metrics.getHeight()/2)+((metrics.getHeight()+4)/2)-1, graphWidth+3, points.get(points.size()-1).y-(metrics.getHeight()/2)+((metrics.getHeight()+4)/2)-1);

            g2.drawString(yLabel, graphWidth+5,points.get(points.size()-1).y+(metrics.getHeight()/2)-2);

            if(hovering){
                double additionScore = ((graphHeight/gridHeight)*waveHeight)+((double)(graphHeight-totalHeight)/gridHeight)*waveHeight;
                double additionTime = ((graphWidth/gridWidth)*time)+((double)(graphWidth-((graphWidth/gridWidth)*gridWidth))/gridWidth)*time;

                g2.setColor(hoverColor);
                g2.drawLine(mouseX, graphBounds.y, mouseX, graphHeight);
                g2.drawLine(graphBounds.x, mouseY, graphWidth, mouseY);

                g2.setColor(graphLineColor);
                g2.fillRoundRect(graphWidth, mouseY-(metrics.getHeight()/2)-3, graphBounds.width-5, metrics.getHeight()+6, 5, 5);


                String label = String.format("%.2f", additionTime-additionTime*(((double)mouseX/(double)graphWidth)))+" "+timeValue;
                metrics = g2.getFontMetrics();
                int labelWidth = metrics.stringWidth(label);
                g2.fillRoundRect(mouseX-(labelWidth/2)-5, graphHeight, labelWidth+10, metrics.getHeight()+7, 5, 5);

                g2.setColor(Color.decode("#ffffff"));
                g2.drawLine(mouseX, graphHeight, mouseX, graphHeight+3);
                g2.drawString(label, mouseX-(labelWidth/2), graphHeight+metrics.getHeight());

                label = String.format("%."+digits+"f", additionScore-additionScore*((((double)mouseY/(double)graphHeight))));
                metrics = g2.getFontMetrics();

                g2.drawLine(graphWidth, mouseY, graphWidth+3, mouseY);
                g2.drawString(label, graphWidth+5, mouseY+(metrics.getHeight()/2)-2);
            }
        }
    }

    public void add(double score){
        scores.add(score);

        if(maxSize < scores.size()){
            scores.remove(0);
        }

        invalidate();
        repaint();
    }

    public void setTimeMultiplier(int time, String timeValue){
        this.time = time;
        this.timeValue = timeValue;
        repaint();
    }

    public double getMax(){
        return Collections.max(scores);
    }

    public double getMin(){
        return Collections.min(scores);
    }

    public void setMaxSize(int maxSize){
        this.maxSize = maxSize;
    }

    public void setPositiveColor(Color positiveColor){
        this.positiveColor = positiveColor;
    }

    public void setNegativeColor(Color negativeColor){
        this.negativeColor = negativeColor;
    }

    public void setGraphLineColor(Color graphLineColor){
        this.graphLineColor = graphLineColor;
    }

    public void setGraphColor(Color graphColor){
        this.graphColor = graphColor;
    }

    public void setHoverColor(Color graphColor){
        this.graphColor = graphColor;
    }
}
