/*
 * IdeaView.java
 *
 * Created on August 24, 2006, 11:39 AM
 *
 * Copyright (c) 2006, David Griffiths
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the David Griffiths nor the names of his contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package dg.hipster.view;

import dg.hipster.model.Idea;
import dg.hipster.model.IdeaEvent;
import dg.hipster.model.IdeaListener;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author davidg
 */
public class IdeaView implements IdeaListener, MapComponent {
    private static Color[] COLOURS = {new Color(255, 20, 20), Color.ORANGE,
    new Color(20, 200, 20), Color.CYAN};
    private double length;
    private double angle;
    private double v;
    private Vector<IdeaView> subViews = new Vector<IdeaView>();
    private Idea idea;
    private boolean root;
    private boolean selected;
    int ROOT_RADIUS_X = 70;
    int ROOT_RADIUS_Y = 40;
    private MapComponent parent;
    
    public IdeaView() {
        this(null);
    }
    
    public IdeaView(Idea anIdea) {
        this(anIdea, true);
    }
    
    private IdeaView(Idea anIdea, boolean whetherIsRoot) {
        root = whetherIsRoot;
        setIdea(anIdea);
    }
    
    public double getLength() {
        return length;
    }
    
    public void setLength(double length) {
        this.length = length;
    }
    
    public double getAngle() {
        return angle;
    }
    
    public double getMinSubAngle() {
        double minAngle = Math.PI;
        for (IdeaView subView: subViews) {
            if (subView.getAngle() < minAngle) {
                minAngle = subView.getAngle();
            }
        }
        return minAngle;
    }
    
    public double getMaxSubAngle() {
        double maxAngle = -Math.PI;
        for (IdeaView subView: subViews) {
            if (subView.getAngle() > maxAngle) {
                maxAngle = subView.getAngle();
            }
        }
        return maxAngle;
    }
    
    public void setAngle(double angle) {
        this.angle = angle;
    }
    public void ideaChanged(IdeaEvent fe) {
        String cmd = fe.getCommand();
        if ("ADDED".equals(cmd)) {
            Idea subIdea = (Idea)fe.getParas()[0];
            int pos = (Integer)fe.getParas()[1];
            IdeaView subIdeaView = new IdeaView(subIdea, false);
            double maxAngle = getMaxSubAngle();
            subIdeaView.setAngle((maxAngle + Math.PI) / 2.0);
            add(pos, subIdeaView);
        } else if ("REMOVED".equals(cmd)) {
            Idea subIdea = (Idea)fe.getParas()[0];
            for (int i = 0; i < subViews.size(); i++) {
                Idea idea = (Idea)subViews.get(i).getIdea();
                if (idea.equals(subIdea)) {
                    subViews.remove(i);
                    break;
                }
            }
        }
        repaintRequired();
    }
    
    public IdeaView getPreviousSibling() {
        return getSibling(-1);
    }
    
    public IdeaView getNextSibling() {
        return getSibling(+1);
    }
    
    public IdeaView getSibling(int difference) {
        MapComponent parent = getParent();
        if (!(parent instanceof IdeaView)) {
            return null;
        }
        IdeaView parentView = (IdeaView)parent;
        int pos = parentView.getSubViews().indexOf(this);
        int subCount = parentView.getSubViews().size();
        int diff = difference % subCount;
        int siblingPos = pos + diff;
        if ((diff == 0) || (siblingPos < 0) || (siblingPos > (subCount - 1))) {
            return null;
        }
        siblingPos = (siblingPos + subCount) % subCount;
        return parentView.getSubViews().get(siblingPos);
    }
    
    public synchronized void add(IdeaView subView) {
        subView.parent = this;
        subViews.add(subView);
    }
    
    public synchronized void add(int pos, IdeaView subView) {
        subView.parent = this;
        subViews.add(pos, subView);
    }
    
    public synchronized void remove(IdeaView subView) {
        subView.parent = null;
        subViews.remove(subView);
    }
    
    public synchronized Vector<IdeaView> getSubViews() {
        return (Vector<IdeaView>)subViews.clone();
    }
    
    public double getV() {
        return v;
    }
    
    public void setV(double v) {
        this.v = v;
    }
    
    public Idea getIdea() {
        return idea;
    }
    
    public void setIdea(Idea newIdea) {
        this.idea = newIdea;
        subViews.clear();
        if (newIdea != null) {
            this.setLength(15 * newIdea.getText().length() + 10);
            int subNum = newIdea.getSubIdeas().size();
            int i = 0;
            for (Idea subIdea: newIdea.getSubIdeas()) {
                IdeaView subView = new IdeaView(subIdea, false);
                double divAngle = 2 * Math.PI / subNum;
                double mult = i * divAngle;
                double subAngle = mult - (divAngle * (subNum - 1) / 2.0);
                if (!root) {
                    subAngle /= 2.0;
                }
                subView.setAngle(subAngle);
                add(subView);
                i++;
            }
            newIdea.addIdeaListener(this);
        } else {
            setLength(0);
        }
    }
    
    public void setSelected(boolean isSelected) {
        this.selected = isSelected;
        repaintRequired();
    }
    
    public boolean isSelected() {
        return this.selected;
    }
    
    public void paint(Graphics g) {
        paint(g, new Point(0, 0), this, getAngle(), 0);
    }
    
    private void paint(final Graphics g, final Point c2,
            final IdeaView aView, final double initAngle, final int depth) {
        List<IdeaView> views = aView.getSubViews();
        Stroke oldStroke = ((Graphics2D)g).getStroke();
        float strokeWidth = 20.0f - (depth * 2);
        if (strokeWidth < 10.0f) {
            strokeWidth = 10.0f;
        }
        Stroke stroke = new BasicStroke(strokeWidth,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
        ((Graphics2D)g).setStroke(stroke);
        synchronized(views) {
            for (IdeaView view: views) {
                Point c = new Point(c2.x, c2.y);
                double a = view.getAngle() + initAngle;
                double len = view.getLength();
                Point2D p = new Point2D.Double(Math.sin(a) * len,
                        Math.cos(a) * len);
                if (aView.isRoot()) {
                    c.x += (int)(Math.sin(a) * ROOT_RADIUS_X);
                    c.y -= (int)(Math.cos(a) * ROOT_RADIUS_Y);
                }
                Point s = getView(c, p);
                paint(g, s, view, a, depth + 1);
                Color colour = COLOURS[depth % COLOURS.length];
                if (view.isSelected()) {
                    colour = invert(colour);
                }
                Color upper = colour.brighter().brighter();
                Color lower = colour.darker().darker();
                g.setColor(lower);
                g.drawLine(c.x, c.y + 1, s.x, s.y + 1);
                g.setColor(upper);
                g.drawLine(c.x, c.y - 1, s.x, s.y - 1);
                g.setColor(colour);
                g.drawLine(c.x, c.y, s.x, s.y);
                if (view.isSelected()) {
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(Color.BLACK);
                }
                Point midp = new Point((c.x + s.x) / 2, (c.y + s.y) / 2);
                double textAngle = (Math.PI / 2.0) - a;
                drawString((Graphics2D)g, view.getIdea().getText(), midp, 4,
                        textAngle);
            }
        }
        ((Graphics2D)g).setStroke(oldStroke);
        if (aView.isRoot()) {
            Color colour = Color.WHITE;
            if (aView.isSelected()) {
                colour = invert(colour);
            }
            g.setColor(colour);
            g.fillOval(-ROOT_RADIUS_X, -ROOT_RADIUS_Y, ROOT_RADIUS_X * 2,
                    ROOT_RADIUS_Y * 2);
            colour = Color.BLACK;
            if (aView.isSelected()) {
                colour = invert(colour);
            }
            g.setColor(colour);
            g.drawOval(-ROOT_RADIUS_X, -ROOT_RADIUS_Y, ROOT_RADIUS_X * 2,
                    ROOT_RADIUS_Y * 2);
            drawString((Graphics2D)g, getIdea().getText(), c2, 4,
                    getAngle());
        }
    }
    
    private static Color invert(Color colour) {
        int red = colour.getRed();
        int green = colour.getGreen();
        int blue = colour.getBlue();
        return new Color(255 - red, 255 - green, 255 - blue);
    }
    
    private Point getView(final Point c, final Point2D p) {
        int sx = c.x + (int)p.getX();
        int sy = c.y - (int)p.getY();
        return new Point(sx, sy);
    }
    
    private void drawString(Graphics2D graphics2d, String string, Point p,
            int alignment, double orientation) {
        double orient = orientation % Math.PI;
        
        if (orient > (Math.PI / 2.0)) {
            orient -= Math.PI;
        }
        if (orient < (-Math.PI / 2.0)) {
            orient += Math.PI;
        }
        
        int xc = p.x;
        int yc = p.y;
        
        AffineTransform transform = graphics2d.getTransform();
        
        transform(graphics2d, orient, xc, yc);
        
        FontMetrics fm = graphics2d.getFontMetrics();
        double realTextWidth = fm.stringWidth(string);
        double realTextHeight = fm.getHeight() - fm.getDescent();
        
        int offsetX = (int) (realTextWidth * (double) (alignment % 3) / 2.0);
        int offsetY = (int) (-realTextHeight * (double) (alignment / 3) / 2.0);
        
        if ((graphics2d != null) && (string != null)) {
            graphics2d.drawString(string, xc - offsetX, yc - offsetY);
        }
        
        graphics2d.setTransform(transform);
    }
    
    private void transform(Graphics2D graphics2d, double orientation, int x,
            int y) {
        graphics2d.transform(
                AffineTransform.getRotateInstance(
                -orientation, x, y
                )
                );
    }
    
    public boolean isRoot() {
        return root;
    }
    
    public void repaintRequired() {
        if (parent != null) {
            parent.repaintRequired();
        }
    }
    
    public MapComponent getParent() {
        return parent;
    }
    
    public void setParent(MapComponent parent) {
        this.parent = parent;
    }
    
    public Point2D getEndPoint() {
        double x = 0.0;
        double y = 0.0;
        double angle = 0.0;
        IdeaView aView = this;
        ArrayList<IdeaView> views = new ArrayList<IdeaView>();
        while(!aView.isRoot()) {
            MapComponent parent = aView.getParent();
            if (!(parent instanceof IdeaView)) {
                break;
            }
            views.add(aView);
            aView = (IdeaView)parent;
        }
        for (int i = views.size() - 1; i >= 0; i--) {
            aView = views.get(i);
            double length = aView.getLength();
            angle += aView.getAngle();
            if (i == (views.size() - 1)) {
                x += (Math.sin(angle) * ROOT_RADIUS_X);
                y -= (Math.cos(angle) * ROOT_RADIUS_Y);
            }
            x += Math.sin(angle) * length;
            y -= Math.cos(angle) * length;
        }
        return new Point2D.Double(x, y);
    }
}
