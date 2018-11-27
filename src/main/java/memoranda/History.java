/**
 * History.java
 * Created on 23.02.2003, 0:27:33 Alex
 * Package: net.sf.memoranda
 * 
 * @author Alex V. Alishevskikh, alex@openmechanics.net
 * Copyright (c) 2003 Memoranda Team. http://memoranda.sf.net
 */
package main.java.memoranda;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.util.Vector;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import main.java.memoranda.interfaces.IHistoryListener;
import main.java.memoranda.interfaces.IProject;
import main.java.memoranda.util.Local;
/**
 * 
 */
/*$Id: History.java,v 1.7 2006/10/31 15:34:14 hglas Exp $*/
public class History {

    static Vector _list = new Vector();
    // TASK 2-1 SMELL WITHIN A CLASS
    static int prevIndex = -1;
    static Vector historyListeners = new Vector();
    static Object next = null;
    static Object prev = null;     
    
    public static void add(HistoryItem item) {
        if (prev != null)   
            if (item.equals(prev)) return;
        if (prevIndex < _list.size() - 1)
            _list.setSize(prevIndex + 1);
        _list.add(item);
        prevIndex = _list.size() - 1;
        if (prevIndex > 0)   
            prev = _list.get(prevIndex-1);
        else
            prev = null;
        next = null;
        historyBackAction.update();
        historyForwardAction.update();  
        /*System.out.println();
        for (int i = 0; i < _list.size(); i++)
            System.out.println(((HistoryItem)_list.get(i)).getDate().toString());
        System.out.println(item.getDate().toShortString()+ " added");*/
        if (_list.size() > 99)
            _list.remove(0);     
    }

    public static HistoryItem rollBack() {        
        Object n = prev;        
        if (prevIndex > 1) {                          
            prevIndex--;
            prev = _list.get(prevIndex-1);
        } 
        else if (prevIndex > 0) {
            prevIndex--;
            prev = null;
        }
        else 
            prev = null;      
        if (prevIndex < _list.size() - 1)
            next = _list.get(prevIndex+1);
        else
            next = null;         
        return (HistoryItem)n;
    }

    public static HistoryItem rollForward() {
        Object n = next;        
        if (prevIndex < _list.size() - 1) {
            prevIndex++;
            if (prevIndex == 1) 
                prevIndex++;
            next = _list.get(prevIndex);            
        }        
        else
            next = null;
        if (prevIndex > 0)
            prev = _list.get(prevIndex-1);
        else 
            prev = null;
        return (HistoryItem)n;    
    }

    public static boolean canRollBack() {
        return prev != null;
    }

    public static boolean canRollForward() {
        return next != null;
    }

    public static void addHistoryListener(IHistoryListener hl) {
        historyListeners.add(hl);
    }
    
    public static void removeProjectHistory(IProject prj) {
        Vector list = new Vector();
        String id;
        
        for (int i = 0; i < _list.size(); i++) {
            id = (((HistoryItem) _list.elementAt(i)).getProject()).getID();
            if (id.equals(prj.getID())) {
                list.add(_list.elementAt(i));
                prevIndex--;
                if (_list.elementAt(i).equals(prev)) {
                    if (prevIndex > 0) prev = _list.get(prevIndex - 1);
                    else prev = null;
                }
            }
        }
        if (!list.isEmpty()) {
            _list.removeAll(list);
            if (prevIndex < 0) {
                prevIndex = 0;
            }
            _list.setSize(prevIndex);
            next = null;
            historyBackAction.update();
            historyForwardAction.update();
        }
    }

    private static void notifyListeners(HistoryItem n) {
        for (int i = 0; i < historyListeners.size(); i++)            
                 ((IHistoryListener) historyListeners.get(i)).historyWasRolledTo(n);
    }

    public static HistoryBackAction historyBackAction = new HistoryBackAction();
    public static HistoryForwardAction historyForwardAction = new HistoryForwardAction();

    static class HistoryBackAction extends AbstractAction {

        public HistoryBackAction() {
            super(Local.getString("History back"), 
            new ImageIcon(main.java.memoranda.ui.AppFrame.class.getResource("/ui/icons/hist_back.png")));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_MASK));
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {            
            notifyListeners(rollBack());
            update();
            historyForwardAction.update();
        }

        /*public boolean isEnabled() {
            return canRollBack();
        }*/

        void update() {
            if (canRollBack()) {
                setEnabled(true);

		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf = (SimpleDateFormat)DateFormat.getDateInstance(DateFormat.SHORT);
		Date date = ((HistoryItem) prev).getDate().getDate();
		    putValue(
                    Action.SHORT_DESCRIPTION,
		   Local.getString("Back to") + " " + sdf.format(date));

//                putValue(Action.SHORT_DESCRIPTION, Local.getString("Back to") + " " + ((HistoryItem) prev).getDate().toString());
            }
            else {
                setEnabled(false);
                putValue(Action.SHORT_DESCRIPTION, Local.getString("Back"));
            }
        }
    }

    static class HistoryForwardAction extends AbstractAction {

        public HistoryForwardAction() {
            super(Local.getString("History forward"), 
            new ImageIcon(main.java.memoranda.ui.AppFrame.class.getResource("/ui/icons/hist_forward.png")));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_MASK));
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {            
            notifyListeners(rollForward());
            update();
            historyBackAction.update();
        }

        /*public boolean isEnabled() {
            return canRollForward();
        }*/

        void update() {
            if (canRollForward()) {
                setEnabled(true);

		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf = (SimpleDateFormat)DateFormat.getDateInstance(DateFormat.SHORT);
		Date date = ((HistoryItem) next).getDate().getDate();

		    putValue(
                    Action.SHORT_DESCRIPTION,
                   // Local.getString("Forward to") + " " + ((HistoryItem) next).getDate().toString());
		   Local.getString("Forward to") + " " + sdf.format(date));
            }
            else {
                setEnabled(false);
                putValue(Action.SHORT_DESCRIPTION, Local.getString("Forward"));
            }
        }
    }

}
