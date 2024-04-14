package main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Mouse extends MouseAdapter{
    public int x,y;
    public boolean clicked;
    public boolean dragged; // New variable to track if the mouse is being dragged
    public boolean released; // New variable to track if the mouse has been released

    @Override
    public void mouseClicked(MouseEvent e) {
        x = e.getX();
        y = e.getY();
        clicked = true;
        dragged = false; // Set dragged to false when the mouse is clicked
        released = false; // Set released to false when the mouse is clicked
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        x = e.getX();
        y = e.getY();
        released = true;
        clicked = false; // Set clicked to false when the mouse is released
        dragged = false; // Set dragged to false when the mouse is released
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        x = e.getX();
        y = e.getY();
        dragged = true; // Set dragged to true when the mouse is dragged
        clicked = false; // Set clicked to false when the mouse is dragged
        released = false; // Set released to false when the mouse is dragged
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        x = e.getX();
        y = e.getY();
    }
}
